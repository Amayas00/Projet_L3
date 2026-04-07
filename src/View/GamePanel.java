package src.View;

import javax.swing.JPanel;
import src.model.BombermanModel;
import src.model.Bomb;
import src.model.Player;
import src.model.TileType;
import src.model.ItemType;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;


public class GamePanel extends JPanel {

    public  static final int TILE_SIZE   = 40;
    private static final int STAT_WIDTH  = 140;
    private static final int BOTTOM_BAR  = 32;  

    private static final Color COLOR_WALL = new Color(55,  55,  55);
    private static final Color COLOR_DESTRUCTIBLE = new Color(160, 100, 45);
    private static final Color COLOR_EMPTY = new Color(34,  139, 34);
    private static final Color COLOR_EXPLOSION    = new Color(255, 140, 0);
    private static final Color COLOR_P1 = new Color(30,  100, 220);
    private static final Color COLOR_P2 = new Color(200, 40,  40);
    private static final Color COLOR_STAT_BG = new Color(18,  18,  28);
    private static final Color COLOR_STAT_BORDER  = new Color(60,  60,  80);
    private static final Color COLOR_HEART_FULL   = new Color(220, 50,  50);
    private static final Color COLOR_HEART_EMPTY  = new Color(80,  30,  30);
    private static final Color COLOR_BAR_BG  = new Color(40,  40,  60);

    private Rectangle restartButtonBounds = null;
    private boolean   restartClicked      = false;

    private BombermanModel model;

    public GamePanel(BombermanModel model) {
        this.model = model;
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.setBackground(Color.BLACK);
        updatePreferredSize();

        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (restartButtonBounds != null
                        && restartButtonBounds.contains(e.getPoint())
                        && model.getPhase() == BombermanModel.GamePhase.GAME_OVER) {
                    restartClicked = true;
                }
            }
        });
    }

    private void updatePreferredSize() {
        int w = STAT_WIDTH * 2 + model.getWidth()  * TILE_SIZE;
        int h = model.getHeight() * TILE_SIZE + BOTTOM_BAR;
        this.setPreferredSize(new Dimension(w, h));
    }

    public void updateModel(BombermanModel newModel) {
        this.model = newModel;
        updatePreferredSize();
        repaint();
    }

    public boolean isRestartClicked() {
        if (restartClicked) { restartClicked = false; return true; }
        return false;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (model == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<Player> players = model.getPlayers();
        Player p1 = players.stream().filter(p -> p.getId() == 1).findFirst().orElse(null);
        Player p2 = players.stream().filter(p -> p.getId() == 2).findFirst().orElse(null);

        int gridH = model.getHeight() * TILE_SIZE;
        drawStatPanel(g2, p1, 0,                          gridH, true);
        drawStatPanel(g2, p2, STAT_WIDTH + model.getWidth() * TILE_SIZE, gridH, false);

        g2.translate(STAT_WIDTH, 0);
        drawGrid(g2);
        drawItems(g2);
        drawBombs(g2);
        drawPlayers(g2);
        g2.translate(-STAT_WIDTH, 0);

        drawBottomBar(g2);

        if (model.getPhase() == BombermanModel.GamePhase.GAME_OVER) {
            drawGameOver(g2);
        }
    }


    private void drawStatPanel(Graphics2D g, Player p, int panelX, int panelH, boolean isLeft) {
        if (p == null) return;

        Color playerColor = isLeft ? COLOR_P1 : COLOR_P2;

        g.setColor(COLOR_STAT_BG);
        g.fillRect(panelX, 0, STAT_WIDTH, panelH);
        g.setColor(COLOR_STAT_BORDER);
        g.drawRect(panelX, 0, STAT_WIDTH - 1, panelH - 1);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(playerColor);
        String title = "JOUEUR " + p.getId();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, panelX + (STAT_WIDTH - fm.stringWidth(title)) / 2, 28);

        g.setColor(playerColor.darker());
        g.drawLine(panelX + 10, 34, panelX + STAT_WIDTH - 10, 34);

        int y = 60; // curseur vertical courant

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(new Color(180, 180, 180));
        g.drawString("VIES", panelX + 12, y - 4);

        int maxLives = 3;
        int heartSize = 18;
        int heartsWidth = maxLives * (heartSize + 4) - 4;
        int heartStartX = panelX + (STAT_WIDTH - heartsWidth) / 2;
        for (int i = 0; i < maxLives; i++) {
            boolean full = i < p.getLives();
            drawHeart(g, heartStartX + i * (heartSize + 4), y + 2, heartSize,
                      full ? COLOR_HEART_FULL : COLOR_HEART_EMPTY);
        }
        y += heartSize + 16;

        // ── Séparateur mince ──────────────────────────────────────────────
        g.setColor(COLOR_STAT_BORDER);
        g.drawLine(panelX + 10, y - 6, panelX + STAT_WIDTH - 10, y - 6);

        y = drawStatRow(g, panelX, y, "Vitesse",  p.getSpeedLevel(),  5, playerColor);
        y += 4;

        y = drawStatRow(g, panelX, y, "Portée",   p.getBombRange(),   9, playerColor);
        y += 4;

        y = drawStatRow(g, panelX, y, "Bombes",   p.getBombCapacity(), 8, playerColor);
        y += 12;

        int activeBombs = p.getActiveBombs();
        if (activeBombs > 0) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.setColor(new Color(255, 200, 60));
            String active = "💣 x" + activeBombs + " actives";
            g.drawString(active, panelX + 12, y);
            y += 16;
        }

        if (!p.isAlive()) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.setColor(new Color(200, 50, 50));
            String dead = "ÉLIMINÉ";
            FontMetrics fm2 = g.getFontMetrics();
            g.drawString(dead, panelX + (STAT_WIDTH - fm2.stringWidth(dead)) / 2, y + 10);
        } else if (p.isInvincible()) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(new Color(255, 230, 80));
            String inv = "★ INVINCIBLE ★";
            FontMetrics fm2 = g.getFontMetrics();
            // Animation de clignotement
            if ((System.currentTimeMillis() / 300) % 2 == 0) {
                g.drawString(inv, panelX + (STAT_WIDTH - fm2.stringWidth(inv)) / 2, y + 10);
            }
        }
    }

    private int drawStatRow(Graphics2D g, int panelX, int y,
                             String label, int value, int maxValue, Color barColor) {
        int innerW = STAT_WIDTH - 20;
        int barH   = 8;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(new Color(180, 180, 180));
        g.drawString(label, panelX + 12, y);
        y += 14;

        g.setColor(COLOR_BAR_BG);
        g.fillRoundRect(panelX + 10, y, innerW, barH, 4, 4);

        int filled = (int) Math.round((double) value / maxValue * innerW);
        if (filled > 0) {
            g.setColor(barColor);
            g.fillRoundRect(panelX + 10, y, filled, barH, 4, 4);
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(Color.WHITE);
        String val = value + "/" + maxValue;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(val, panelX + STAT_WIDTH - fm.stringWidth(val) - 10, y + barH - 1);

        return y + barH + 6;
    }


    private void drawHeart(Graphics2D g, int x, int y, int size, Color color) {
        g.setColor(color);
        // Deux cercles en haut
        int r = size / 4;
        g.fillOval(x,         y, size / 2, size / 2);
        g.fillOval(x + size/2 - r, y, size / 2, size / 2);
        // Triangle en bas
        int[] xs = {x, x + size, x + size / 2};
        int[] ys = {y + size/3, y + size/3, y + size};
        g.fillPolygon(xs, ys, 3);
    }

    private void drawGrid(Graphics2D g) {
        TileType[][] grid = model.getGrid();
        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;
                switch (grid[x][y]) {
                    case WALL -> {
                        g.setColor(COLOR_WALL);
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g.setColor(COLOR_WALL.brighter());
                        g.drawLine(px, py, px + TILE_SIZE - 1, py);
                        g.drawLine(px, py, px, py + TILE_SIZE - 1);
                    }
                    case DESTRUCTIBLE_BLOCK -> {
                        g.setColor(COLOR_DESTRUCTIBLE);
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g.setColor(COLOR_DESTRUCTIBLE.darker());
                        g.drawRect(px, py, TILE_SIZE - 1, TILE_SIZE - 1);
                        g.setColor(COLOR_DESTRUCTIBLE.brighter());
                        g.drawLine(px+5, py+5, px+TILE_SIZE-5, py+TILE_SIZE-5);
                        g.drawLine(px+TILE_SIZE-5, py+5, px+5, py+TILE_SIZE-5);
                    }
                    case EXPLOSION -> {
                        g.setColor(COLOR_EMPTY);
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g.setColor(COLOR_EXPLOSION);
                        g.fillRoundRect(px+2, py+2, TILE_SIZE-4, TILE_SIZE-4, 8, 8);
                        g.setColor(Color.YELLOW);
                        g.fillRoundRect(px+8, py+8, TILE_SIZE-16, TILE_SIZE-16, 6, 6);
                    }
                    case ITEM -> {
                        g.setColor(COLOR_EMPTY);
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    }
                    default -> {
                        g.setColor(COLOR_EMPTY);
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }
    }


    private void drawItems(Graphics2D g) {
        ItemType[][] items = model.getItems();
        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                if (items[x][y] == ItemType.NONE) continue;
                int px = x * TILE_SIZE, py = y * TILE_SIZE, m = TILE_SIZE / 5;
                g.setColor(itemColor(items[x][y]));
                g.fillRoundRect(px+m, py+m, TILE_SIZE-m*2, TILE_SIZE-m*2, 8, 8);
                g.setColor(Color.BLACK);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                String lbl = itemLabel(items[x][y]);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(lbl, px + (TILE_SIZE-fm.stringWidth(lbl))/2,
                                  py + TILE_SIZE/2 + fm.getAscent()/3);
            }
        }
    }

    private Color  itemColor(ItemType t) {
        return switch (t) {
            case BONUS_SPEED -> new Color(100, 220, 255);
            case BONUS_BOMB_COUNT -> new Color(255, 200, 50);
            case BONUS_RANGE -> new Color(255, 130, 50);
            case MALUS_SLOW -> new Color(180, 60, 180);
            default  -> Color.WHITE;
        };
    }

    private String itemLabel(ItemType t) {
        return switch (t) {
            case BONUS_SPEED -> "+V";
            case BONUS_BOMB_COUNT -> "+B";
            case BONUS_RANGE -> "+F";
            case MALUS_SLOW -> "-V";
            default  -> "?";
        };
    }


    private void drawBombs(Graphics2D g) {
        for (Bomb b : model.getBombs()) {
            int px = b.getX() * TILE_SIZE, py = b.getY() * TILE_SIZE;
            g.setColor(COLOR_EMPTY);
            g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
            g.setColor(Color.BLACK);
            g.fillOval(px+6, py+6, TILE_SIZE-12, TILE_SIZE-12);
            long rem = b.getRemainingMs();
            boolean blink = (rem % (rem > 1000 ? 600 : 250)) < (rem > 1000 ? 300 : 125);
            if (blink) {
                g.setColor(new Color(255, 180, 0));
                g.fillOval(px + TILE_SIZE/2 - 3, py + 5, 6, 6);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            String cd = String.valueOf((rem / 1000) + 1);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(cd, px+(TILE_SIZE-fm.stringWidth(cd))/2,
                            py+TILE_SIZE/2+fm.getAscent()/3);
        }
    }

    private void drawPlayers(Graphics2D g) {
        for (Player p : model.getPlayers()) {
            if (!p.isAlive()) continue;

            // Clignotement pendant l'invincibilité (toutes les 150 ms)
            if (p.isInvincible() && (System.currentTimeMillis() / 150) % 2 == 0) continue;

            int px = p.getX() * TILE_SIZE, py = p.getY() * TILE_SIZE;
            Color base = (p.getId() == 1) ? COLOR_P1 : COLOR_P2;

            g.setColor(base);
            g.fillOval(px+4, py+4, TILE_SIZE-8, TILE_SIZE-8);
            g.setColor(base.darker());
            g.drawOval(px+4, py+4, TILE_SIZE-9, TILE_SIZE-9);

            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String lbl = String.valueOf(p.getId());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(lbl, px+(TILE_SIZE-fm.stringWidth(lbl))/2,
                              py+TILE_SIZE/2+fm.getAscent()/3);
        }
    }

    private void drawBottomBar(Graphics2D g) {
        int totalW = getWidth();
        int barY   = model.getHeight() * TILE_SIZE;
        g.setColor(new Color(12, 12, 20));
        g.fillRect(0, barY, totalW, BOTTOM_BAR);
        g.setColor(new Color(60, 60, 80));
        g.drawLine(0, barY, totalW, barY);

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        g.setColor(COLOR_P1.brighter());
        g.drawString("J1: ↑↓←→  [Entrée]=Bombe", 10, barY + 21);

        g.setColor(COLOR_P2.brighter());
        String p2keys = "J2: WASD  [Espace]=Bombe";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(p2keys, totalW - fm.stringWidth(p2keys) - 10, barY + 21);
    }


    private void drawGameOver(Graphics2D g) {
        int totalW = getWidth(), totalH = getHeight();

        // Voile semi-transparent
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, totalW, totalH);

        Player winner = model.getWinner();
        String title  = (winner != null) ? "JOUEUR " + winner.getId() + " GAGNE !" : "ÉGALITÉ !";
        Color  tColor = (winner != null)
                ? (winner.getId() == 1 ? COLOR_P1 : COLOR_P2)
                : Color.YELLOW;

        // Titre
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 44));
        g.setColor(tColor);
        FontMetrics fm = g.getFontMetrics();
        int cx = totalW / 2;
        int cy = totalH / 2 - 40;
        g.drawString(title, cx - fm.stringWidth(title)/2, cy);

        // Sous-titre résumé vies
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        for (Player p : model.getPlayers()) {
            String summary = "Joueur " + p.getId() + " — " + p.getLives() + " vie(s) restante(s)";
            FontMetrics fm2 = g.getFontMetrics();
            int offset = (p.getId() == 1) ? -22 : 10;
            g.drawString(summary, cx - fm2.stringWidth(summary)/2, cy + 30 + offset);
        }

        // ── Bouton Rejouer ────────────────────────────────────────────────
        int btnW = 180, btnH = 46;
        int btnX = cx - btnW/2, btnY = cy + 70;
        restartButtonBounds = new Rectangle(btnX, btnY, btnW, btnH);

        // Fond du bouton avec arrondi
        g.setColor(new Color(40, 160, 80));
        g.fill(new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 12, 12));
        g.setColor(new Color(70, 220, 110));
        g.draw(new RoundRectangle2D.Float(btnX, btnY, btnW - 1, btnH - 1, 12, 12));

        // Texte du bouton
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(Color.WHITE);
        FontMetrics fmBtn = g.getFontMetrics();
        String btnTxt = "▶  Rejouer";
        g.drawString(btnTxt,
                btnX + (btnW - fmBtn.stringWidth(btnTxt)) / 2,
                btnY + btnH/2 + fmBtn.getAscent()/3);

        // ── Indice clavier ────────────────────────────────────────────────
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(150, 150, 150));
        String hint = "ou appuyez sur R";
        FontMetrics fmH = g.getFontMetrics();
        g.drawString(hint, cx - fmH.stringWidth(hint)/2, btnY + btnH + 20);
    }
}
