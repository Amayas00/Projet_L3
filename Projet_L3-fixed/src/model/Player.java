package src.model;

import java.util.List;

public class Player {
    private final int id;

    private float px, py;
    private final float spawnPxX, spawnPxY;

    public static final int TILE_SIZE = 44;

    private static final int INITIAL_LIVES = 3;
    private int lives = INITIAL_LIVES;
    private boolean alive = true;

    private int bombCapacity    = 1;
    private int activeBombCount = 0;
    private int bombRange       = 2;

    private float speed = 3.2f;
    private static final float MIN_SPEED = 1.4f;
    private static final float MAX_SPEED = 4.5f;

    private long invincibleUntil = 0;
    private static final int SPAWN_INVINCIBLE_MS = 2000;

    // Tile sur laquelle le joueur a posé sa dernière bombe.
    // -1,-1 = aucune bombe en cours d'évitement.
    private int ownedBombTileX = -1;// pas de bombe posée initialement, donc pas de tile à éviter
    private int ownedBombTileY = -1;

    public Player(int id, int startTileX, int startTileY) {
        this.id = id;
        this.px = startTileX * TILE_SIZE + TILE_SIZE / 2f;
        this.py = startTileY * TILE_SIZE + TILE_SIZE / 2f;
        this.spawnPxX = this.px;
        this.spawnPxY = this.py;
        this.invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
    }

    public void moveBy(float dx, float dy, TileType[][] grid, List<Bomb> bombs, int gridW, int gridH) {
        if (!alive) return;

        // Dès que le centre du joueur quitte la tile de sa bombe,
        // on réactive la collision avec elle.
        if (ownedBombTileX != -1) {
            int curTileX = (int) Math.floor(px / TILE_SIZE);// le joeur est en pixel, en divisant par la taile d'une case on trouve la num de la case 
            int curTileY = (int) Math.floor(py / TILE_SIZE);
            if (curTileX != ownedBombTileX || curTileY != ownedBombTileY) {
                ownedBombTileX = -1;
                ownedBombTileY = -1;
            }
        }

        float newPx = px + dx * speed;
        float newPy = py + dy * speed;

        boolean canX = canMoveTo(newPx, py,   grid, bombs, gridW, gridH);
        boolean canY = canMoveTo(px,    newPy, grid, bombs, gridW, gridH);

        if (canX && canY) {
            px = newPx;
            py = newPy;
        } else if (canX) {
            px = newPx;
            py = snapAxis(py, dy);
        } else if (canY) {
            py = newPy;
            px = snapAxis(px, dx);
        }
    }

    private float snapAxis(float pos, float dDir) {
        if (dDir != 0) return pos;
        float center = (float) Math.floor(pos / TILE_SIZE) * TILE_SIZE + TILE_SIZE / 2f;
        float diff   = center - pos;
        float step   = speed * 0.5f;
        if (Math.abs(diff) <= step) return center;
        return pos + Math.signum(diff) * step;
    }

    // Hitbox plus petite que la tile pour permettre au joueur de glisser le long
    // des murs sans rester accroché aux coins. Avec TILE_SIZE=44 et padding=7,
    // la hitbox fait 30x30 dans une case de 44x44 (au lieu de 36x36 avant).
    // Cela laisse 7px de jeu de chaque côté au lieu de 4px → traversée des
    // couloirs beaucoup plus fluide, et l'IA (qui raisonne en tiles entiers)
    // ne se coince plus quand son centre est légèrement décalé.
    private static final float HITBOX_PADDING = 7f;

    private boolean canMoveTo(float cx, float cy, TileType[][] grid, List<Bomb> bombs, int gridW, int gridH) {
        float half = TILE_SIZE / 2f - HITBOX_PADDING;

        int tileX1 = (int) Math.floor((cx - half) / TILE_SIZE);
        int tileX2 = (int) Math.floor((cx + half) / TILE_SIZE);
        int tileY1 = (int) Math.floor((cy - half) / TILE_SIZE);
        int tileY2 = (int) Math.floor((cy + half) / TILE_SIZE);

        for (int tx = tileX1; tx <= tileX2; tx++) {
            for (int ty = tileY1; ty <= tileY2; ty++) {
                if (tx < 0 || ty < 0 || tx >= gridW || ty >= gridH) return false;

                TileType t = grid[tx][ty];
                if (t == TileType.WALL || t == TileType.DESTRUCTIBLE_BLOCK) return false;

                // Une bombe bloque sauf si c'est celle que le joueur
                // vient de poser et qu'il est encore sur sa tile.
                for (Bomb b : bombs) {
    if (b.getX() == tx && b.getY() == ty) {
        // Bloque uniquement si ce n'est PAS une bombe du joueur courant.
        if (b.getOwner() != this) return false;
    }
}
            }
        }
        return true;
    }

    // Appelé par BombermanModel.placeBomb() juste après création de la bombe.
    public void setOwnedBombTile(int tileX, int tileY) {
        this.ownedBombTileX = tileX;
        this.ownedBombTileY = tileY;
    }

    public int getX() { return (int) Math.floor(px / TILE_SIZE); }
    public int getY() { return (int) Math.floor(py / TILE_SIZE); }

    public float getPixelX() { return px - TILE_SIZE / 2f; }
    public float getPixelY() { return py - TILE_SIZE / 2f; }

    // Taille visuelle du corps (oval) — alignée sur la hitbox réelle pour que
    // ce que le joueur voit corresponde à ce qui collisionne. GamePanel
    // l'utilise pour dessiner.
    public static int getBodyPadding() { return (int) HITBOX_PADDING; }
    public static int getBodySize()    { return TILE_SIZE - 2 * (int) HITBOX_PADDING; }

    public boolean canMove() { return alive; }

    public void updatePosition(int nx, int ny) {
        this.px = nx * TILE_SIZE + TILE_SIZE / 2f;
        this.py = ny * TILE_SIZE + TILE_SIZE / 2f;
    }

    public boolean canPlaceBomb() { return alive && activeBombCount < bombCapacity; }
    public void onBombPlaced()    { activeBombCount++; }
    public void onBombExploded()  { if (activeBombCount > 0) activeBombCount--; }

    public enum HitResult { HIT_NONE, HIT_ALIVE, HIT_DEAD }

    public HitResult hit() {
        if (!alive || isInvincible()) return HitResult.HIT_NONE;
        lives--;
        if (lives <= 0) { lives = 0; alive = false; return HitResult.HIT_DEAD; }
        invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
        return HitResult.HIT_ALIVE;
    }

    public void respawn() {
        this.px = spawnPxX;
        this.py = spawnPxY;
        this.ownedBombTileX = -1;
        this.ownedBombTileY = -1;
        this.invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
    }

    public void reset() {
        this.px = spawnPxX;
        this.py = spawnPxY;
        this.lives = INITIAL_LIVES;
        this.alive = true;
        this.activeBombCount = 0;
        this.bombCapacity = 1;
        this.bombRange = 2;
        this.speed = 3.2f;
        this.ownedBombTileX = -1;
        this.ownedBombTileY = -1;
        this.invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
    }

    public void applyItem(ItemType type) {
        switch (type) {
            case BONUS_SPEED      -> speed = Math.min(speed + 0.4f, MAX_SPEED);
            case MALUS_SLOW       -> speed = Math.max(speed - 0.5f, MIN_SPEED);
            case BONUS_BOMB_COUNT -> bombCapacity = Math.min(bombCapacity + 1, 8);
            case BONUS_RANGE      -> bombRange    = Math.min(bombRange    + 1, 9);
            case NONE -> {}
        }
    }

    public int     getId()          { return id; }
    public int     getLives()       { return lives; }
    public boolean isAlive()        { return alive; }
    public boolean isInvincible()   { return System.currentTimeMillis() < invincibleUntil; }
    public int     getBombRange()   { return bombRange; }
    public int     getBombCapacity(){ return bombCapacity; }
    public int     getActiveBombs() { return activeBombCount; }
    public int     getSpawnX()      { return (int) Math.floor(spawnPxX / TILE_SIZE); }
    public int     getSpawnY()      { return (int) Math.floor(spawnPxY / TILE_SIZE); }

    public int getSpeedLevel() {
        double ratio = (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
        return (int) Math.round(1 + ratio * 4);
    }

    public int   getMoveDelay() { return 0; }
    public float getSpeed()     { return speed; }

    public void setAlive(boolean alive) { this.alive = alive; }
}