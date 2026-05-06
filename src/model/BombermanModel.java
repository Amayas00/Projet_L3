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

    /*
     * Cases actuellement enflammées : clé = position encodée (x * height + y),
     * valeur = timestamp d'expiration (en ms) le plus tardif.
     * Une Map permet de fusionner proprement les flammes qui se chevauchent
     * (deux explosions sur la même case prennent l'expiry maximum) au lieu
     * d'avoir des entrées dupliquees qui causaient un nettoyage premature
     * du grid.
     */
    private final Map<Long, Long> activeExplosions = new HashMap<>();

    private final int explosionDurationMs;
    private final double itemDropChance;

    private static final long MAP_SEED = 0; 

    public BombermanModel(int width, int height, GameSettings settings) {
        this.width = width;
        this.height = height;
        this.explosionDurationMs = settings.getExplosionDuration();
        this.itemDropChance  = settings.getItemDropChance();
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
            case 1 -> new int[]{1,     1      };
            case 2 -> new int[]{w - 2, h - 2  };
            default -> new int[]{1,     1      };
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

    public void update() {
        if (phase != GamePhase.PLAYING) return;

        expireExplosions();
        triggerReadyBombs();
        // Securite : un joueur peut etre reste immobile sur une case devenue
        // enflammee a ce tick - ou, a cause d'invincibilite, n'avoir pas ete
        // touche lors de l'apparition de la flamme. On reverifie ici pour que
        // la flamme blesse pendant toute sa duree de vie.
        damagePlayersOnFlames();
        checkWinCondition();
    }

    /**
     * Encode une position 2D en cle unique pour la Map des explosions actives.
     */
    private long key(int x, int y) {
        return (long) x * height + y;
    }

    /**
     * Indique si une case est actuellement enflammee (flamme encore visible).
     * Utilise en interne pour les collisions joueur / flamme.
     */
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
                long k = entry.getKey();
                int ex = (int) (k / height);
                int ey = (int) (k % height);

                // La flamme est terminee : on libere la case.
                // On ne touche pas aux cases devenues WALL ou DESTRUCTIBLE_BLOCK
                // (theoriquement impossible mais on reste defensif).
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
            // On retire la bombe de la liste AVANT de declencher l'explosion,
            // pour qu'elle ne soit plus consideree comme bloquante et pour
            // eviter qu'elle ne se redeclenche en chaine sur elle-meme.
            activeBombs.remove(b);
            triggerExplosion(b);
        }
    }

    /**
     * Inflige des degats a tout joueur situe sur une case actuellement
     * enflammee. Appelee a chaque tick : ainsi un joueur qui ENTRE dans une
     * flamme deja active meurt aussi (et pas seulement au moment ou la
     * flamme apparait). Respecte l'invincibilite geree par Player.hit().
     */
    private void damagePlayersOnFlames() {
        for (Player p : players) {
            if (!p.isAlive()) continue;
            if (!isFlameActive(p.getX(), p.getY())) continue;

            Player.HitResult result = p.hit();
            if (result == Player.HitResult.HIT_ALIVE) {
                p.respawn();
            }
        }
    }

    private void checkWinCondition() {
        long aliveCount = players.stream().filter(Player::isAlive).count();
        if (aliveCount <= 1) {
            phase  = GamePhase.GAME_OVER;
            winner = players.stream().filter(Player::isAlive).findFirst().orElse(null);
        }
    }

    public void placeBomb(int playerId) {
        for (Player p : players) {
            if (p.getId() == playerId && p.isAlive() && p.canPlaceBomb()) {
                int px = p.getX(), py = p.getY();
                boolean alreadyThere = activeBombs.stream()
                        .anyMatch(b -> b.getX() == px && b.getY() == py);
                // On evite aussi de poser une bombe sur une case enflammee
                // (cas tres rare mais coherent avec la logique de jeu).
                if (!alreadyThere && grid[px][py] != TileType.EXPLOSION) {
                    activeBombs.add(new Bomb(px, py, p.getBombRange(), p));
                    p.onBombPlaced();
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
                    ItemType drop = rollItem();
                    items[nx][ny] = drop;
                    applyExplosionCell(nx, ny, expiry);
                    break; 
                }

                applyExplosionCell(nx, ny, expiry);
            }
        }
    }

    private void applyExplosionCell(int x, int y, long expiry) {
        grid[x][y] = TileType.EXPLOSION;

        // Si la case etait deja enflammee par une autre explosion, on garde
        // l'expiry le plus tardif - sinon la premiere flamme a expirer
        // libererait la case alors que l'autre est encore active.
        long k = key(x, y);
        Long current = activeExplosions.get(k);
        if (current == null || expiry > current) {
            activeExplosions.put(k, expiry);
        }

        // Joueur deja present au moment ou la flamme apparait : touche tout
        // de suite (le check par tick gerera ceux qui entrent ensuite).
        for (Player p : players) {
            if (p.isAlive() && p.getX() == x && p.getY() == y) {
                Player.HitResult result = p.hit();
                if (result == Player.HitResult.HIT_ALIVE) {
                    p.respawn();
                }
            }
        }

        // Reaction en chaine : toute autre bombe touchee explose immediatement.
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

    public void movePlayer(int playerId, int dx, int dy) {
        if (phase != GamePhase.PLAYING) return;

        for (Player p : players) {
            if (p.getId() != playerId || !p.isAlive() || !p.canMove()) continue;

            int newX = p.getX() + dx;
            int newY = p.getY() + dy;

            if (!inBounds(newX, newY)) continue;

            TileType target = grid[newX][newY];
            // EXPLOSION reste "marchable" : un joueur peut decider d'entrer
            // dans une flamme (et y mourir), il n'est pas bloque par elle.
            // Une fois la flamme expiree, la case redevient EMPTY ou ITEM
            // dans expireExplosions() -> traversable normalement.
            boolean walkable = target == TileType.EMPTY
                    || target == TileType.EXPLOSION
                    || target == TileType.ITEM;

            // Une bombe NON encore explosee bloque le passage. Des qu'elle
            // explose, elle est retiree de activeBombs (cf. triggerReadyBombs)
            // donc la case redevient franchissable au tick suivant.
            boolean bombThere = activeBombs.stream()
                    .anyMatch(b -> b.getX() == newX && b.getY() == newY);

            if (walkable && !bombThere) {
                p.updatePosition(newX, newY);

                // Ramassage d'item eventuel.
                if (target == TileType.ITEM || items[newX][newY] != ItemType.NONE) {
                    p.applyItem(items[newX][newY]);
                    items[newX][newY] = ItemType.NONE;
                    // On ne remet a EMPTY que si la case n'est pas en feu :
                    // sinon on effacerait la flamme prematurement.
                    if (grid[newX][newY] != TileType.EXPLOSION) {
                        grid[newX][newY] = TileType.EMPTY;
                    }
                }

                // Si le joueur vient d'entrer dans une flamme active,
                // il prend un coup tout de suite. C'est LE point cle qui
                // corrige le bug "entrer dans une flamme ne tue pas".
                if (isFlameActive(newX, newY)) {
                    Player.HitResult result = p.hit();
                    if (result == Player.HitResult.HIT_ALIVE) {
                        p.respawn();
                    }
                }
            }
        }
    }

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

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public TileType[][]  getGrid()    { return grid; }
    public ItemType[][]  getItems()   { return items; }
    public List<Player>  getPlayers() { return players; }
    public List<Bomb> getBombs()   { return activeBombs; }
    public GamePhase  getPhase()   { return phase; }
    public Player  getWinner()  { return winner; }
    public int  getWidth()   { return width; }
    public int getHeight()  { return height; }
}