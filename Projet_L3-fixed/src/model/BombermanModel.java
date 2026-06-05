package src.model;

import java.util.*;

public class BombermanModel {

    public enum GamePhase { PLAYING, GAME_OVER }
    private GamePhase phase = GamePhase.PLAYING;
    private Player winner = null;

    private final int width, height;
    private final TileType[][] grid;
    private final ItemType[][] items;

    private final List<Player> players = new ArrayList<>();
    private final List<Bomb> activeBombs = new ArrayList<>();

    private final Map<Long, Long> activeExplosions = new HashMap<>();

    private final int explosionDurationMs;
    private final double itemDropChance;

    private static final long MAP_SEED = 0;

    public BombermanModel(int width, int height, GameSettings settings) {
        this.width = width;
        this.height = height;
        this.explosionDurationMs = settings.getExplosionDuration();
        this.itemDropChance = settings.getItemDropChance();
        this.grid = new TileType[width][height];
        this.items = new ItemType[width][height];

        generateLevel();

        int[] p1spawn = spawnFor(1, width, height);
        int[] p2spawn = spawnFor(2, width, height);
        players.add(new Player(1, p1spawn[0], p1spawn[1]));
        players.add(new Player(2, p2spawn[0], p2spawn[1]));
    }

    private int[] spawnFor(int playerId, int w, int h) {
        return switch (playerId) {
            case 1  -> new int[]{1,     1    };
            case 2  -> new int[]{w - 2, h - 2};
            default -> new int[]{1,     1    };
        };
    }

    private void generateLevel() {
        Random rand = MAP_SEED == 0 ? new Random() : new Random(MAP_SEED);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                items[x][y] = ItemType.NONE;

                boolean isBorder = (x == 0 || y == 0 || x == width - 1 || y == height - 1);
                boolean isPillar = (x % 2 == 0 && y % 2 == 0);

                if (isBorder || isPillar) {
                    grid[x][y] = TileType.WALL;
                } else if (rand.nextDouble() < 0.65) {
                    grid[x][y] = TileType.DESTRUCTIBLE_BLOCK;
                } else {
                    grid[x][y] = TileType.EMPTY;
                }
            }
        }

        int[] s1 = spawnFor(1, width, height);
        int[] s2 = spawnFor(2, width, height);
        applySpawnSafeZone(s1[0], s1[1]);
        applySpawnSafeZone(s2[0], s2[1]);
    }

    private void applySpawnSafeZone(int cx, int cy) {
        setEmpty(cx, cy);
        setEmpty(cx + 1, cy);
        setEmpty(cx - 1, cy);
        setEmpty(cx, cy + 1);
        setEmpty(cx, cy - 1);

        boolean hasExit = false;
        int[][] cardinals = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : cardinals) {
            int nx = cx + d[0], ny = cy + d[1];
            if (inBounds(nx, ny) && grid[nx][ny] == TileType.EMPTY) {
                hasExit = true;
                break;
            }
        }
        if (!hasExit) {
            for (int[] d : cardinals) {
                int nx = cx + d[0], ny = cy + d[1];
                if (inBounds(nx, ny) && grid[nx][ny] != TileType.WALL) {
                    grid[nx][ny]  = TileType.EMPTY;
                    items[nx][ny] = ItemType.NONE;
                    break;
                }
            }
        }
    }

    private void setEmpty(int x, int y) {
        if (inBounds(x, y) && grid[x][y] != TileType.WALL) {
            grid[x][y]  = TileType.EMPTY;
            items[x][y] = ItemType.NONE;
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public void update() {
        if (phase != GamePhase.PLAYING) return;

        expireExplosions();
        triggerReadyBombs();
        damagePlayersOnFlames();
        checkWinCondition();
    }

    private long key(int x, int y) {
        return (long) x * height + y;
    }

    private boolean isFlameActive(int x, int y) {
        Long expiry = activeExplosions.get(key(x, y));
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    private void expireExplosions() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Long, Long>> it = activeExplosions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Long> entry = it.next();
            if (now >= entry.getValue()) {
                long k  = entry.getKey();
                int ex  = (int)(k / height);
                int ey  = (int)(k % height);
                if (grid[ex][ey] == TileType.EXPLOSION) {
                    grid[ex][ey] = (items[ex][ey] != ItemType.NONE)
                            ? TileType.ITEM
                            : TileType.EMPTY;
                }
                it.remove();
            }
        }
    }

    private void triggerReadyBombs() {
        List<Bomb> toExplode = new ArrayList<>();
        for (Bomb b : activeBombs) {
            if (b.isReadyToExplode()) toExplode.add(b);
        }
        for (Bomb b : toExplode) {
            activeBombs.remove(b);
            triggerExplosion(b);
        }
    }

    private void damagePlayersOnFlames() {
        for (Player p : players) {
            if (!p.isAlive()) continue;
            if (!isFlameActive(p.getX(), p.getY())) continue;
            Player.HitResult result = p.hit();
            if (result == Player.HitResult.HIT_ALIVE) p.respawn();
        }
    }

    private void checkWinCondition() {
        long aliveCount = players.stream().filter(Player::isAlive).count();
        if (aliveCount <= 1) {
            phase  = GamePhase.GAME_OVER;
            winner = players.stream().filter(Player::isAlive).findFirst().orElse(null);
        }
    }

    // ── BOMB ──────────────────────────────────────────────────────────────────

    public void placeBomb(int playerId) {
        for (Player p : players) {
            if (p.getId() == playerId && p.isAlive() && p.canPlaceBomb()) {
                int bx = p.getX(), by = p.getY();
                boolean alreadyThere = activeBombs.stream()
                        .anyMatch(b -> b.getX() == bx && b.getY() == by);
                if (!alreadyThere && grid[bx][by] != TileType.EXPLOSION) {
                    Bomb bomb = new Bomb(bx, by, p.getBombRange(), p);
                    activeBombs.add(bomb);
                    p.onBombPlaced();
                    // MODIFIÉ : on mémorise la tile de la bombe dans le joueur
                    // (coordonnées entières) pour que canMoveTo() l'ignore
                    // tant que le joueur n'a pas quitté cette tile.
                    p.setOwnedBombTile(bx, by);
                }
            }
        }
    }

    private void triggerExplosion(Bomb b) {
        b.getOwner().onBombExploded();

        long expiry = System.currentTimeMillis() + explosionDurationMs;

        applyExplosionCell(b.getX(), b.getY(), expiry);

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] d : dirs) {
            for (int r = 1; r <= b.getRange(); r++) {
                int nx = b.getX() + d[0] * r;
                int ny = b.getY() + d[1] * r;

                if (!inBounds(nx, ny)) break;
                if (grid[nx][ny] == TileType.WALL) break;

                if (grid[nx][ny] == TileType.DESTRUCTIBLE_BLOCK) {
                    items[nx][ny] = rollItem();
                    applyExplosionCell(nx, ny, expiry);
                    break;
                }

                applyExplosionCell(nx, ny, expiry);
            }
        }
    }

    private void applyExplosionCell(int x, int y, long expiry) {
        grid[x][y] = TileType.EXPLOSION;

        long k = key(x, y);
        Long current = activeExplosions.get(k);
        if (current == null || expiry > current) {
            activeExplosions.put(k, expiry);
        }

        for (Player p : players) {
            if (p.isAlive() && p.getX() == x && p.getY() == y) {
                Player.HitResult result = p.hit();
                if (result == Player.HitResult.HIT_ALIVE) p.respawn();
            }
        }

        activeBombs.stream()
                .filter(b -> b.getX() == x && b.getY() == y)
                .findFirst()
                .ifPresent(chain -> {
                    activeBombs.remove(chain);
                    triggerExplosion(chain);
                });
    }

    private ItemType rollItem() {
        if (Math.random() >= itemDropChance) return ItemType.NONE;
        ItemType[] drops = {
            ItemType.BONUS_SPEED,
            ItemType.BONUS_BOMB_COUNT,
            ItemType.BONUS_RANGE,
            ItemType.MALUS_SLOW
        };
        return drops[(int)(Math.random() * drops.length)];
    }

    // ── MOVE ──────────────────────────────────────────────────────────────────

    public void movePlayer(int playerId, float dx, float dy) {
        if (phase != GamePhase.PLAYING) return;

        for (Player p : players) {
            if (p.getId() != playerId || !p.isAlive() || !p.canMove()) continue;

            p.moveBy(dx, dy, grid, activeBombs, width, height);

            int tx = p.getX();
            int ty = p.getY();

            // Ramassage d'item sur la tile courante.
            if (items[tx][ty] != ItemType.NONE) {
                p.applyItem(items[tx][ty]);
                items[tx][ty] = ItemType.NONE;
                if (grid[tx][ty] != TileType.EXPLOSION) {
                    grid[tx][ty] = TileType.EMPTY;
                }
            }

            // Dégâts si le joueur est sur une flamme active.
            if (isFlameActive(tx, ty)) {
                Player.HitResult result = p.hit();
                if (result == Player.HitResult.HIT_ALIVE) p.respawn();
            }
        }
    }

    // ── RESET ─────────────────────────────────────────────────────────────────

    public void resetGame() {
        activeBombs.clear();
        activeExplosions.clear();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                items[x][y] = ItemType.NONE;

        generateLevel();

        for (Player p : players) p.reset();

        phase  = GamePhase.PLAYING;
        winner = null;
    }

    // ── UTILS ─────────────────────────────────────────────────────────────────

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    // ── GETTERS ───────────────────────────────────────────────────────────────

    public TileType[][] getGrid()    { return grid; }
    public ItemType[][] getItems()   { return items; }
    public List<Player> getPlayers() { return players; }
    public List<Bomb>   getBombs()   { return activeBombs; }
    public GamePhase    getPhase()   { return phase; }
    public Player       getWinner()  { return winner; }
    public int          getWidth()   { return width; }
    public int          getHeight()  { return height; }
}