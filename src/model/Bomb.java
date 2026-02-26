package src.model;
public class Bomb {
    private int x, y;
    private int range;
    private long explosionTimestamp;
    private Player owner;

    public Bomb(int x, int y, int range, Player owner) {
        this.x = x;
        this.y = y;
        this.range = range;
        this.owner = owner;
        this.explosionTimestamp = System.currentTimeMillis() + 2500;
    }

    public boolean isReadyToExplode() {
        return System.currentTimeMillis() >= explosionTimestamp;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRange() { return range; }
}