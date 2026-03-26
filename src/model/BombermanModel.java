package src.model;

import java.util.*;

public class BombermanModel {

    public enum GamePhase { PLAYING, GAME_OVER }

    private final int width;
    private final int height;

    private final TileType[][] grid;
    private final ItemType[][] items;

    private final List<Player> players;
    private final List<Bomb>   activeBombs;

    private final long[][] explosionEndTime;

    private final GameSettings settings;

    private GamePhase phase = GamePhase.PLAYING;

    public BombermanModel(int width, int height, GameSettings settings) {
        this.width    = width;
        this.height   = height;
        this.settings = settings;

        this.grid = new TileType[width][height];
        this.items = new ItemType[width][height];
        this.explosionEndTime = new long[width][height];

        this.players     = new ArrayList<>();
        this.activeBombs = new ArrayList<>();

        generateLevel();

        players.add(new Player(1, 1, 1));
        players.add(new Player(2, width - 2, height - 2));
    }

    private void generateLevel() {
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y]  = decideTileType(x, y, rand);
                items[x][y] = ItemType.NONE;
                explosionEndTime[x][y] = 0;
            }
        }
        clearSpawnZone(1, 1);
        clearSpawnZone(width - 2, height - 2);
    }

    private TileType decideTileType(int x, int y, Random rand) {
        boolean isBorder      = (x == 0 || y == 0 || x == width - 1 || y == height - 1);
        boolean isFixedPillar = (x % 2 == 0 && y % 2 == 0);
        if (isBorder || isFixedPillar) return TileType.WALL;
        if (rand.nextDouble() < 0.70)  return TileType.DESTRUCTIBLE_BLOCK;
        return TileType.EMPTY;
    }

    private void clearSpawnZone(int cx, int cy) {
        grid[cx][cy] = TileType.EMPTY;
        grid[cx + (cx == 1 ? 1 : -1)][cy] = TileType.EMPTY;
        grid[cx][cy + (cy == 1 ? 1 : -1)] = TileType.EMPTY;
    }

    public void update() {
        if (phase == GamePhase.GAME_OVER) return;

        tickBombs();
        clearExpiredExplosions();
        checkItemPickups();
        checkPlayerDeaths();
        checkWinCondition();
    }

    public void placeBomb(int playerId) {
        Player player = findPlayer(playerId);
        if (player == null || !player.isAlive() || !player.canPlaceBomb()) return;

        int bx = player.getX();
        int by = player.getY();

        if (isBombAt(bx, by)) return;

        activeBombs.add(new Bomb(bx, by, player.getBombRange(), player));
        player.onBombPlaced();
    }

    public boolean isBombAt(int x, int y) {
        for (Bomb b : activeBombs) {
            if (b.getX() == x && b.getY() == y) return true;
        }
        return false;
    }

    private void tickBombs() {
        Iterator<Bomb> it = activeBombs.iterator();
        while (it.hasNext()) {
            Bomb b = it.next();
            if (b.isReadyToExplode()) {
                triggerExplosion(b);
                b.getOwner().onBombExploded();
                it.remove();
            }
        }
    }

    private void triggerExplosion(Bomb bomb) {
        markExplosion(bomb.getX(), bomb.getY());

        int[][] directions = { {0, 1}, {0, -1}, {1, 0}, {-1, 0} };
        for (int[] dir : directions) {
            spreadExplosion(bomb, dir[0], dir[1]);
        }
    }

    private void spreadExplosion(Bomb bomb, int dx, int dy) {
        for (int r = 1; r <= bomb.getRange(); r++) {
            int nx = bomb.getX() + dx * r;
            int ny = bomb.getY() + dy * r;

            if (!isInBounds(nx, ny)) break;
            if (grid[nx][ny] == TileType.WALL) break;

            if (grid[nx][ny] == TileType.DESTRUCTIBLE_BLOCK) {
                destroyBlock(nx, ny);
                break;
            }

            markExplosion(nx, ny);
            detonateBombAt(nx, ny);
        }
    }

    private void markExplosion(int x, int y) {
        grid[x][y] = TileType.EXPLOSION;
        explosionEndTime[x][y] = System.currentTimeMillis() + settings.getExplosionDuration();
    }

    private void detonateBombAt(int x, int y) {
        Iterator<Bomb> it = activeBombs.iterator();
        while (it.hasNext()) {
            Bomb b = it.next();
            if (b.getX() == x && b.getY() == y) {
                it.remove();
                b.getOwner().onBombExploded();
                triggerExplosion(b);
                break;
            }
        }
    }

    private void destroyBlock(int x, int y) {
        grid[x][y] = TileType.EMPTY;
        markExplosion(x, y);
        if (Math.random() < settings.getItemDropChance()) {
            items[x][y] = randomItem();
        }
    }

    private ItemType randomItem() {
        ItemType[] pool = {
            ItemType.BONUS_SPEED, ItemType.BONUS_BOMB_COUNT,
            ItemType.BONUS_RANGE, ItemType.MALUS_SLOW
        };
        return pool[(int)(Math.random() * pool.length)];
    }

    private void clearExpiredExplosions() {
        long now = System.currentTimeMillis();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == TileType.EXPLOSION && now >= explosionEndTime[x][y]) {
                    grid[x][y] = TileType.EMPTY;
                }
            }
        }
    }

    public void movePlayer(int playerId, int dx, int dy) {
        Player player = findPlayer(playerId);
        if (player == null || !player.isAlive() || !player.canMove()) return;

        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        if (!isInBounds(newX, newY)) return;

        TileType target = grid[newX][newY];
        if (target == TileType.EMPTY || target == TileType.EXPLOSION) {
            player.updatePosition(newX, newY);
        }
    }

    private void checkItemPickups() {
        for (Player p : players) {
            if (!p.isAlive()) continue;
            ItemType item = items[p.getX()][p.getY()];
            if (item != ItemType.NONE) {
                p.applyItem(item);
                items[p.getX()][p.getY()] = ItemType.NONE;
            }
        }
    }

    private void checkPlayerDeaths() {
        for (Player p : players) {
            if (p.isAlive() && grid[p.getX()][p.getY()] == TileType.EXPLOSION) {
                p.hit();
            }
        }
    }

    private void checkWinCondition() {
        long alive = players.stream().filter(Player::isAlive).count();
        if (alive <= 1) phase = GamePhase.GAME_OVER;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private Player findPlayer(int id) {
        for (Player p : players) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public TileType[][] getGrid() { return grid; }
    public ItemType[][] getItems() { return items; }
    public List<Player> getPlayers() { return players; }
    public List<Bomb> getActiveBombs() { return activeBombs; }
    public GamePhase getPhase() { return phase; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
