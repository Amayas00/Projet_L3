package src.controller;

import src.model.*;
import java.util.*;
import java.util.Set;
import java.util.HashSet;

public class BotAI {

    private static final int BOT_ID = 2;

    public enum Difficulty { EASY, MEDIUM, HARD }
    private final Difficulty difficulty;

    // --- Mouvement continu par tile ---
    // Le bot choisit une direction toutes les N ticks (cooldown), puis
    // continue dans cette direction tick par tick jusqu'à avoir parcouru
    // exactement une tile entière (TILE_SIZE pixels). Cela correspond au
    // fonctionnement réel de Player.moveBy() qui travaille en pixels.
    private int    tickCooldown  = 0;
    private int    moveDx        = 0;   
    private int    moveDy        = 0;
    private float  remainingPx   = 0f;

    /**
     * Constructeur du BotAI.
     * Initialise le niveau de difficulté du bot.
     */
    public BotAI(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Calcule l'action que le bot doit effectuer à ce tick.
     * Gère les déplacements, l'évitement des dangers,
     * la pose de bombes et la poursuite du joueur humain.
     */
    public BotAction computeAction(BombermanModel model) {
        Player bot = getBot(model);
        if (bot == null || !bot.isAlive()) return BotAction.NONE;

        // Si le bot est en train de traverser une tile,
        // on continue dans la même direction.
        if (remainingPx > 0) {
            remainingPx -= bot.getSpeed();
            if (remainingPx < 0) remainingPx = 0;
            if (moveDx != 0 || moveDy != 0) return BotAction.move(moveDx, moveDy);
        }

        // Attendre le prochain cooldown avant de choisir une nouvelle action.
        tickCooldown--;
        if (tickCooldown > 0) return BotAction.NONE;

        tickCooldown = switch (difficulty) {
            case EASY   -> 6 + new Random().nextInt(6);
            case MEDIUM -> 3 + new Random().nextInt(3);
            case HARD   -> 1;
        };

        Player human = getHuman(model);
        int bx = bot.getX(), by = bot.getY();

        // Si le bot est en danger, chercher une direction sûre.
        if (isInDanger(bx, by, model)) {
            int[] safeDir = findSafeDirection(bx, by, model);
            if (safeDir != null) return startMove(safeDir[0], safeDir[1], bot);

            // Si aucune issue, essayer de poser une bombe.
            if (bot.canPlaceBomb() && !isBombAt(bx, by, model))
                return BotAction.PLACE_BOMB;

            return BotAction.NONE;
        }

        // Décision de poser une bombe.
        if (bot.canPlaceBomb() && !isBombAt(bx, by, model)) {
            boolean nearHuman   = human != null && human.isAlive()
                    && manhattan(bx, by, human.getX(), human.getY()) <= 2;

            boolean nearBlocks  = countAdjacentBlocks(bx, by, model) >= 1;

            boolean willSurvive = hasSafeEscape(bx, by, bot.getBombRange(), model);

            double bombProb = switch (difficulty) {
                case EASY   -> 0.3;
                case MEDIUM -> 0.6;
                case HARD   -> 1.0;
            };

            if ((nearHuman || nearBlocks) && willSurvive && Math.random() < bombProb)
                return BotAction.PLACE_BOMB;
        }

        // Se rapprocher du joueur humain.
        if (human != null && human.isAlive()) {
            int[] dir = moveToward(bx, by, human.getX(), human.getY(), model);
            if (dir != null) return startMove(dir[0], dir[1], bot);
        }

        // Sinon déplacement aléatoire.
        return randomWalkAction(bx, by, model, bot);
    }

    /**
     * Démarre un déplacement d'une tile complète
     * dans la direction indiquée.
     */
    private BotAction startMove(int dx, int dy, Player bot) {
        this.moveDx = dx;
        this.moveDy = dy;

        // Le premier pas est effectué immédiatement,
        // les suivants via remainingPx.
        this.remainingPx = Player.TILE_SIZE - bot.getSpeed();

        if (this.remainingPx < 0) this.remainingPx = 0;

        return BotAction.move(dx, dy);
    }

    /**
     * Récupère le joueur contrôlé par le bot.
     */
    private Player getBot(BombermanModel m) {
        return m.getPlayers().stream()
                .filter(p -> p.getId() == BOT_ID)
                .findFirst()
                .orElse(null);
    }

    /**
     * Récupère le joueur humain.
     */
    private Player getHuman(BombermanModel m) {
        return m.getPlayers().stream()
                .filter(p -> p.getId() == 1)
                .findFirst()
                .orElse(null);
    }

    /**
     * Vérifie si une position est dangereuse
     * (explosion ou portée d'une bombe).
     */
    private boolean isInDanger(int x, int y, BombermanModel model) {
        if (model.getGrid()[x][y] == TileType.EXPLOSION) return true;

        for (Bomb b : model.getBombs()) {
            if (b.getRemainingMs() < 1800 && inBlastRadius(x, y, b, model))
                return true;
        }

        return false;
    }

    /**
     * Vérifie si une case est dans le rayon
     * d'explosion d'une bombe.
     */
    private boolean inBlastRadius(int x, int y, Bomb b, BombermanModel model) {
        if (b.getX() == x && b.getY() == y) return true;

        TileType[][] grid = model.getGrid();
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        for (int[] d : dirs) {
            for (int r = 1; r <= b.getRange(); r++) {
                int nx = b.getX()+d[0]*r;
                int ny = b.getY()+d[1]*r;

                if (!inBounds(nx, ny, model)) break;
                if (grid[nx][ny] == TileType.WALL) break;

                if (nx == x && ny == y) return true;

                if (grid[nx][ny] == TileType.DESTRUCTIBLE_BLOCK) break;
            }
        }

        return false;
    }

    /**
     * Cherche une direction sûre pour échapper au danger.
     */
    private int[] findSafeDirection(int x, int y, BombermanModel model) {
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        List<int[]> safe = new ArrayList<>();

        for (int[] d : dirs) {
            int nx = x+d[0];
            int ny = y+d[1];

            if (canWalk(nx, ny, model) && !isInDanger(nx, ny, model))
                safe.add(d);
        }

        if (safe.isEmpty()) return null;

        return safe.get(new Random().nextInt(safe.size()));
    }

    /**
     * Calcule le chemin le plus court vers une cible
     * avec un parcours en largeur (BFS).
     */
    private int[] moveToward(int x, int y, int tx, int ty, BombermanModel model) {
        if (x == tx && y == ty) return null;

        int[][] prev = new int[model.getWidth() * model.getHeight()][2];

        for (int[] row : prev)
            Arrays.fill(row, -1);

        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{x, y});

        prev[idx(x, y, model)] = new int[]{x, y};

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};

        boolean found = false;

        outer:
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();

            for (int[] d : dirs) {
                int nx = cur[0]+d[0];
                int ny = cur[1]+d[1];

                if (!inBounds(nx, ny, model) || !canWalk(nx, ny, model))
                    continue;

                int nid = idx(nx, ny, model);

                if (prev[nid][0] != -1)
                    continue;

                prev[nid] = cur;

                if (nx == tx && ny == ty) {
                    found = true;
                    break outer;
                }

                queue.add(new int[]{nx, ny});
            }
        }

        if (!found) return null;

        int[] cur = {tx, ty};

        while (true) {
            int[] p = prev[idx(cur[0], cur[1], model)];

            if (p[0] == x && p[1] == y)
                return new int[]{cur[0]-x, cur[1]-y};

            cur = p;
        }
    }

    /**
     * Effectue un déplacement aléatoire parmi
     * les directions possibles.
     */
    private BotAction randomWalkAction(int x, int y, BombermanModel model, Player bot) {
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        List<int[]> options = new ArrayList<>();

        for (int[] d : dirs) {
            if (canWalk(x+d[0], y+d[1], model))
                options.add(d);
        }

        if (options.isEmpty()) return BotAction.NONE;

        int[] d = options.get(new Random().nextInt(options.size()));

        return startMove(d[0], d[1], bot);
    }

    /**
     * Vérifie si le bot possède une échappatoire sûre
     * après avoir posé une bombe.
     */
    private boolean hasSafeEscape(int bx, int by, int range, BombermanModel model) {
        Set<String> blastZone = new HashSet<>();

        blastZone.add(bx + "," + by);

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        TileType[][] grid = model.getGrid();

        for (int[] d : dirs) {
            for (int r = 1; r <= range; r++) {
                int nx = bx + d[0]*r;
                int ny = by + d[1]*r;

                if (!inBounds(nx, ny, model)) break;
                if (grid[nx][ny] == TileType.WALL) break;
                if (grid[nx][ny] == TileType.DESTRUCTIBLE_BLOCK) break;

                blastZone.add(nx + "," + ny);
            }
        }

        Queue<int[]> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(new int[]{bx, by, 0});
        visited.add(bx + "," + by);

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();

            int cx = cur[0];
            int cy = cur[1];
            int dist = cur[2];

            if (!blastZone.contains(cx + "," + cy))
                return true;

            if (dist >= range + 2)
                continue;

            for (int[] d : dirs) {
                int nx = cx+d[0];
                int ny = cy+d[1];

                String key = nx+","+ny;

                if (!inBounds(nx, ny, model)) continue;
                if (visited.contains(key)) continue;
                if (!canWalk(nx, ny, model)) continue;

                visited.add(key);

                queue.add(new int[]{nx, ny, dist+1});
            }
        }

        return false;
    }

    /**
     * Compte le nombre de blocs destructibles
     * adjacents à une position.
     */
    private int countAdjacentBlocks(int x, int y, BombermanModel model) {
        int count = 0;

        TileType[][] grid = model.getGrid();

        for (int[] d : new int[][]{{0,1},{0,-1},{1,0},{-1,0}}) {
            int nx = x+d[0];
            int ny = y+d[1];

            if (inBounds(nx, ny, model)
                    && grid[nx][ny] == TileType.DESTRUCTIBLE_BLOCK)
                count++;
        }

        return count;
    }

    /**
     * Vérifie si une case peut être traversée.
     */
    private boolean canWalk(int x, int y, BombermanModel model) {
        if (!inBounds(x, y, model)) return false;

        TileType t = model.getGrid()[x][y];

        boolean walkable =
                t == TileType.EMPTY
                || t == TileType.EXPLOSION
                || t == TileType.ITEM;

        return walkable && !isBombAt(x, y, model);
    }

    /**
     * Vérifie si une bombe est présente sur une case.
     */
    private boolean isBombAt(int x, int y, BombermanModel model) {
        return model.getBombs().stream()
                .anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    /**
     * Vérifie si une position est dans les limites de la map.
     */
    private boolean inBounds(int x, int y, BombermanModel model) {
        return x >= 0
                && x < model.getWidth()
                && y >= 0
                && y < model.getHeight();
    }

    /**
     * Calcule la distance de Manhattan
     * entre deux positions.
     */
    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1-x2) + Math.abs(y1-y2);
    }

    /**
     * Convertit des coordonnées (x,y)
     * en index linéaire.
     */
    private int idx(int x, int y, BombermanModel model) {
        return y * model.getWidth() + x;
    }

    // ── BOT ACTION ─────────────────────────────────────────────

    public static class BotAction {

        public static final BotAction NONE =
                new BotAction(0, 0, false);

        public static final BotAction PLACE_BOMB =
                new BotAction(0, 0, true);

        public final int dx, dy;
        public final boolean placeBomb;

        /**
         * Constructeur privé d'une action du bot.
         */
        private BotAction(int dx, int dy, boolean placeBomb) {
            this.dx = dx;
            this.dy = dy;
            this.placeBomb = placeBomb;
        }

        /**
         * Crée une action de déplacement.
         */
        public static BotAction move(int dx, int dy) {
            return new BotAction(dx, dy, false);
        }

        /**
         * Vérifie si l'action correspond à un déplacement.
         */
        public boolean isMove() {
            return dx != 0 || dy != 0;
        }
    }
}