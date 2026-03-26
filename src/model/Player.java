package src.model;

public class Player {
    private final int id;
    private int x, y;

    private int bombCapacity  = 1;  
    private int activeBombCount = 0; 
    private int bombRange = 2;

    private boolean alive = true;

    private long lastMoveTime = 0;
    private int  moveDelay = 180; 
    private static final int MIN_MOVE_DELAY = 60;
    private static final int MAX_MOVE_DELAY = 400;


    public Player(int id, int startX, int startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
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

    public boolean hit() {
        if (!alive) return false;
        alive = false;
        return true;
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

    public int getId(){ return id; }

    public int getX(){ return x; }
    public int getY(){ return y; }

    public int getBombRange(){ return bombRange; }

    public int getBombCapacity(){ return bombCapacity; }

    public boolean isAlive(){ return alive; }

    public int getMoveDelay(){ return moveDelay; }

    public void setAlive(boolean alive) { this.alive = alive; }
}
