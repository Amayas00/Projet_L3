package src.model;


public class Player {
    public enum HitResult { HIT_ALIVE, HIT_DEAD, HIT_NONE }


    private final int id;
    private int x, y;
    private final int startX, startY; // Needed to remember where to respawn!


    private int bombCapacity = 1;  
    private int activeBombCount = 0;
    private int bombRange = 2;


    private boolean alive = true;
    private int lives = 3;
    private long invulnerableUntil = 0;


    private long lastMoveTime = 0;
    private int moveDelay = 180;
    private static final int MIN_MOVE_DELAY = 60;
    private static final int MAX_MOVE_DELAY = 400;


    public Player(int id, int startX, int startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
    }


    public boolean canMove() {
        return System.currentTimeMillis() - lastMoveTime >= moveDelay;
    }


    public void updatePosition(int nx, int ny) {
        this.x = nx;
        this.y = ny;
        this.lastMoveTime = System.currentTimeMillis();
    }


    public boolean canPlaceBomb() {
        return alive && activeBombCount < bombCapacity;
    }


    public void onBombPlaced(){ activeBombCount++; }


    public void onBombExploded() {
        if (activeBombCount > 0) activeBombCount--;
    }


    // UPDATED: Now returns the HitResult expected by the new model
    public HitResult hit() {
        if (!alive || isInvulnerable()) return HitResult.HIT_NONE;
       
        lives--;
        if (lives <= 0) {
            alive = false;
            return HitResult.HIT_DEAD;
        } else {
            invulnerableUntil = System.currentTimeMillis() + 2000;
            return HitResult.HIT_ALIVE;
        }
    }


    public boolean isInvulnerable() {
        return System.currentTimeMillis() < invulnerableUntil;
    }


    // NEW: Send player back to spawn after getting hit
    public void respawn() {
        this.x = startX;
        this.y = startY;
    }


    // NEW: Full reset for when 'R' is pressed to restart the game
    public void reset() {
        this.x = startX;
        this.y = startY;
        this.alive = true;
        this.lives = 3;
        this.bombCapacity = 1;
        this.activeBombCount = 0;
        this.bombRange = 2;
        this.moveDelay = 180;
        this.invulnerableUntil = 0;
    }


    public void applyItem(ItemType type) {
        switch (type) {
            case BONUS_SPEED -> moveDelay = Math.max(MIN_MOVE_DELAY, moveDelay - 30);
            case MALUS_SLOW -> moveDelay = Math.min(MAX_MOVE_DELAY, moveDelay + 40);
            case BONUS_BOMB_COUNT -> bombCapacity = Math.min(bombCapacity + 1, 8);
            case BONUS_RANGE -> bombRange = Math.min(bombRange + 1, 9);
            case NONE -> {}
        }
    }


    public int getId(){ return id; }
    public int getX(){ return x; }
    public int getY(){ return y; }
    public int getBombRange(){ return bombRange; }
    public int getBombCapacity(){ return bombCapacity; }
    public boolean isAlive(){ return alive; }
    public int getLives() { return lives; }
    public int getMoveDelay(){ return moveDelay; }
    public void setAlive(boolean alive) { this.alive = alive; }
}
