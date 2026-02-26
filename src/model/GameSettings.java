package src.model;

public class GameSettings {
    private int mapWidth = 15;
    private int mapHeight = 15;
    private int gameSpeed = 30;
    private int explosionDuration = 800;
    private double itemDropChance = 0.35;

    public GameSettings() {}

    public int getMapWidth(){ return mapWidth; }
    public void setMapWidth(int v){ mapWidth = v; }

    public int getMapHeight(){ return mapHeight; }
    public void setMapHeight(int v){ mapHeight = v; }

    public int getGameSpeed(){ return gameSpeed; }
    public void setGameSpeed(int v){ gameSpeed = v; }

    public int getExplosionDuration(){ return explosionDuration; }
    public void setExplosionDuration(int v){ explosionDuration = v; }
    
    public double getItemDropChance(){ return itemDropChance; }
    public void setItemDropChance(double v){ itemDropChance = v; }
}