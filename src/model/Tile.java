package src.model;

public class Tile {
    private TileType type;
    private Item bonus; 

    public Tile(TileType type) {
        this.type = type;
    }

    public TileType getType() { return type; }
    public void setType(TileType type) { this.type = type; }
    
    public boolean isWalkable() {
        return type == TileType.EMPTY;
    }
}