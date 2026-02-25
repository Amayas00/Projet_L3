public class Player {
    private int id;
    private int x, y; 
    private int bombCapacity = 1;
    private int bombRange = 2;
    private boolean alive = true;
    private long lastMoveTime = 0;
    private int moveDelay = 200; 

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

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getBombRange() { return bombRange; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
}