import java.util.*;

public class BombermanModel {
    private int width, height; 
    private TileType[][] grid;
    private ItemType[][] items;
    private List<Player> players;
    private List<Bomb> activeBombs;

    public BombermanModel(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new TileType[width][height];
        this.items = new ItemType[width][height];
        this.players = new ArrayList<>();
        this.activeBombs = new ArrayList<>();
        
        generateLevel();
        
        // Joueur 1 en haut à gauche, Joueur 2 en bas à droite [cite: 4]
        players.add(new Player(1, 1, 1));
        players.add(new Player(2, width - 2, height - 2));
    }

    private void generateLevel() {
        Random rand = new Random();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == 0 || j == 0 || i == width - 1 || j == height - 1 || (i % 2 == 0 && j % 2 == 0)) {
                    grid[i][j] = TileType.WALL; 
                } else if (rand.nextDouble() < 0.7) {
                    grid[i][j] = TileType.DESTRUCTIBLE_BLOCK; 
                } else {
                    grid[i][j] = TileType.EMPTY;
                }
                items[i][j] = ItemType.NONE;
            }
        }
        grid[1][1] = TileType.EMPTY; grid[1][2] = TileType.EMPTY; grid[2][1] = TileType.EMPTY;
        grid[width-2][height-2] = TileType.EMPTY; grid[width-3][height-2] = TileType.EMPTY; grid[width-2][height-3] = TileType.EMPTY;
    }

    public void update() {
        Iterator<Bomb> it = activeBombs.iterator();
        while (it.hasNext()) {
            Bomb b = it.next();
            if (b.isReadyToExplode()) {
                triggerExplosion(b);
                it.remove();
            }
        }
    }

    private void triggerExplosion(Bomb b) {
        int bx = b.getX();
        int by = b.getY();
        grid[bx][by] = TileType.EMPTY;

        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}};
        for (int[] d : directions) {
            for (int r = 1; r <= b.getRange(); r++) {
                int nx = bx + d[0] * r;
                int ny = by + d[1] * r;

                if (nx < 0 || nx >= width || ny < 0 || ny >= height) break;
                if (grid[nx][ny] == TileType.WALL) break;

                if (grid[nx][ny] == TileType.DESTRUCTIBLE_BLOCK) {
                    grid[nx][ny] = TileType.EMPTY;
                    if (Math.random() < 0.3) items[nx][ny] = ItemType.BONUS_SPEED;
                    break; 
                }
                
                for (Player p : players) {
                    if (p.getX() == nx && p.getY() == ny) p.setAlive(false);
                }
            }
        }
    }

    public void placeBomb(int playerId) {
        for (Player p : players) {
            if (p.getId() == playerId && p.isAlive()) {
                if (grid[p.getX()][p.getY()] != TileType.BOMB) {
                    grid[p.getX()][p.getY()] = TileType.BOMB;
                    activeBombs.add(new Bomb(p.getX(), p.getY(), p.getBombRange(), p));
                }
            }
        }
    }

    public void movePlayer(int playerId, int dx, int dy) {
        for (Player p : players) {
            if (p.getId() == playerId && p.isAlive() && p.canMove()) {
                int newX = p.getX() + dx;
                int newY = p.getY() + dy;

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    TileType targetTile = grid[newX][newY];
                    if (targetTile == TileType.EMPTY || targetTile == TileType.EXPLOSION) {
                        p.updatePosition(newX, newY);
                    }
                }
            }
        }
    }

    public TileType[][] getGrid() { return grid; }
    public List<Player> getPlayers() { return players; }
}