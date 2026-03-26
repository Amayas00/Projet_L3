package src.model;

public class Tile {
    private TileType type;
    private ItemType item;

    public Tile(TileType type) {
        this.type = type;
        this.item = ItemType.NONE;
    }

    public TileType getType() { return type; }
    public void setType(TileType type) { this.type = type; }

    public ItemType getItem() { return item; }
    public void setItem(ItemType item) { this.item = item; }

    public boolean isWalkable() {
        return type == TileType.EMPTY || type == TileType.EXPLOSION;
    }
}
