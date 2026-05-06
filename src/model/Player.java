package src.model;


public class Player {
    private final int id;

    private int x, y;
    private final int spawnX, spawnY;

    private static final int INITIAL_LIVES = 3;
    private int lives = INITIAL_LIVES;
    private boolean alive = true;

    private int bombCapacity    = 1;
    private int activeBombCount = 0;
    private int bombRange       = 2;

    private long lastMoveTime = 0;
    private int  moveDelay    = 180;
    private static final int MIN_MOVE_DELAY = 60;
    private static final int MAX_MOVE_DELAY = 400;

    private long invincibleUntil = 0;
    private static final int SPAWN_INVINCIBLE_MS = 2000;

    public Player(int id, int startX, int startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.spawnX = startX;
        this.spawnY = startY;
        this.invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
    }

    public boolean canMove() {
        return alive && System.currentTimeMillis() - lastMoveTime >= moveDelay;
    }

    public void updatePosition(int nx, int ny) {
        this.x = nx;
        this.y = ny;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public boolean canPlaceBomb() {
        return alive && activeBombCount < bombCapacity;
    }

    public void onBombPlaced()   { activeBombCount++; 
        
    }
    public void onBombExploded() { if (activeBombCount > 0) activeBombCount--; }

    public enum HitResult {
        HIT_NONE,   // ignoré (invincible ou déjà mort)
        HIT_ALIVE,  // touché, encore des vies → le modèle doit appeler respawn()
        HIT_DEAD    // touché, plus de vies → fin de partie
    }


    public HitResult hit() {
        if (!alive || isInvincible()) return HitResult.HIT_NONE;

        lives--;
        if (lives <= 0) {
            lives = 0;
            alive = false;
            return HitResult.HIT_DEAD;
        }
        invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
        return HitResult.HIT_ALIVE;
    }

    public void respawn() {
        this.x  = spawnX;
        this.y  = spawnY;
        this.lastMoveTime    = 0;
        this.invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
    }

    public void reset() {
        this.x  = spawnX;
        this.y = spawnY;
        this.lives = INITIAL_LIVES;
        this.alive = true;
        this.activeBombCount = 0;
        this.bombCapacity = 1;
        this.bombRange = 2;
        this.moveDelay = 180;
        this.lastMoveTime = 0;
        this.invincibleUntil = System.currentTimeMillis() + SPAWN_INVINCIBLE_MS;
    }

    public void applyItem(ItemType type) {
        switch (type) {
            case BONUS_SPEED -> moveDelay = Math.max(MIN_MOVE_DELAY, moveDelay - 30);
            case MALUS_SLOW -> moveDelay = Math.min(MAX_MOVE_DELAY, moveDelay + 40);
            case BONUS_BOMB_COUNT -> bombCapacity = Math.min(bombCapacity + 1, 8);
            case BONUS_RANGE -> bombRange    = Math.min(bombRange    + 1, 9);
            case NONE -> {}
        }
    }

    public int  getId() { return id; }
    public int getX() { return x; }
    public int getY()  { return y; }
    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }
    public int getLives() { return lives; }
    public boolean isAlive() { return alive; }
    public boolean isInvincible(){ return System.currentTimeMillis() < invincibleUntil; }
    public int getBombRange() { return bombRange; }
    public int getBombCapacity() { return bombCapacity; }
    public int getActiveBombs()  { return activeBombCount; }
    public int getMoveDelay(){ return moveDelay; }

    public int getSpeedLevel() {
        int clamped = Math.max(MIN_MOVE_DELAY, Math.min(MAX_MOVE_DELAY, moveDelay));
        double ratio = (double)(clamped - MIN_MOVE_DELAY) / (MAX_MOVE_DELAY - MIN_MOVE_DELAY);
        return (int) Math.round(1 + (1 - ratio) * 4);
    }

    public void setAlive(boolean alive) { this.alive = alive; }
}
