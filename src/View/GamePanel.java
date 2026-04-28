package src.View;

import javax.swing.JPanel;
import src.model.*;
import src.model.BombermanModel.GamePhase;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class GamePanel extends JPanel {

    public  static final int TILE_SIZE  = 44;
    private static final int STAT_WIDTH = 160;
    private static final int BOTTOM_BAR = 40;

    private static final Color BG           = new Color(8,   8,  16);
    private static final Color WALL_DARK    = new Color(20,  20,  36);
    private static final Color WALL_LIGHT   = new Color(44,  44,  72);
    private static final Color WALL_EDGE    = new Color(70,  70, 110);
    private static final Color BLOCK_DARK   = new Color(90,  50,  15);
    private static final Color BLOCK_LIGHT  = new Color(160, 95,  35);
    private static final Color BLOCK_GRAIN  = new Color(200,130,  60);
    private static final Color FLOOR_A      = new Color(14,  20,  14);
    private static final Color FLOOR_B      = new Color(16,  24,  16);
    private static final Color EXPL_CORE    = new Color(255, 255, 160);
    private static final Color EXPL_MID     = new Color(255, 160,  20);
    private static final Color EXPL_OUTER   = new Color(220,  60,   0);
    private static final Color BOMB_BODY    = new Color(22,  22,  30);
    private static final Color BOMB_SHINE   = new Color(70,  70, 100);
    private static final Color FUSE_LIT     = new Color(255, 220,  40);
    private static final Color P1_BASE      = new Color( 40, 140, 255);
    private static final Color P1_DARK      = new Color( 20,  80, 180);
    private static final Color P1_GLOW      = new Color( 80, 180, 255);
    private static final Color P2_BASE      = new Color(255,  60,  60);
    private static final Color P2_DARK      = new Color(180,  20,  20);
    private static final Color P2_GLOW      = new Color(255, 120, 120);
    private static final Color BOT_ACCENT   = new Color(160, 255, 160);
    private static final Color PANEL_BG     = new Color(10,  10,  20);
    private static final Color PANEL_BORDER = new Color(40,  40,  80);
    private static final Color PANEL_GLOW   = new Color(60,  80, 140);
    private static final Color TEXT_DIM     = new Color(120, 120, 160);
    private static final Color SCANLINE     = new Color(0,   0,   0,  28);
    private static final Color ITEM_SPEED   = new Color( 60, 220, 255);
    private static final Color ITEM_BOMB    = new Color(255, 200,  40);
    private static final Color ITEM_RANGE   = new Color(255, 120,  40);
    private static final Color ITEM_SLOW    = new Color(180,  40, 200);

    private BombermanModel model;
    private boolean botMode = false;
    private Rectangle restartButtonBounds = null;
    private boolean   restartClicked      = false;
    private BufferedImage scanlineOverlay;

    public GamePanel(BombermanModel model) {
        this.model = model;
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.setBackground(BG);
        updatePreferredSize();

        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (restartButtonBounds != null
                        && restartButtonBounds.contains(e.getPoint())
                        && model.getPhase() == GamePhase.GAME_OVER) {
                    restartClicked = true;
                }
            }
        });
    }

    public void setBotMode(boolean bot) { this.botMode = bot; }

    private void updatePreferredSize() {
        int w = STAT_WIDTH * 2 + model.getWidth()  * TILE_SIZE;
        int h = model.getHeight() * TILE_SIZE + BOTTOM_BAR;
        setPreferredSize(new Dimension(w, h));
    }

    public void updateModel(BombermanModel m) {
        this.model = m;
        updatePreferredSize();
        scanlineOverlay = null;
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        List<Player> players = model.getPlayers();
        Player p1 = players.stream().filter(p -> p.getId() == 1).findFirst().orElse(null);
        Player p2 = players.stream().filter(p -> p.getId() == 2).findFirst().orElse(null);

        int gridH = model.getHeight() * TILE_SIZE;
        drawStatPanel(g2, p1, 0, gridH, true, false);
        drawStatPanel(g2, p2, STAT_WIDTH + model.getWidth() * TILE_SIZE, gridH, false, botMode);

        g2.translate(STAT_WIDTH, 0);
        drawGrid(g2);
        drawItems(g2);
        drawBombs(g2);
        drawPlayers(g2);
        g2.translate(-STAT_WIDTH, 0);

        drawScanlines(g2);
        drawBottomBar(g2);

        if (model.getPhase() == GamePhase.GAME_OVER) drawGameOver(g2);
    }

    private void drawGrid(Graphics2D g) {
        TileType[][] grid = model.getGrid();
        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                int px = x * TILE_SIZE, py = y * TILE_SIZE;
                switch (grid[x][y]) {
                    case WALL               -> drawWall(g, px, py);
                    case DESTRUCTIBLE_BLOCK -> drawBlock(g, px, py);
                    case EXPLOSION          -> { drawFloor(g, px, py, x, y); drawExplosion(g, px, py); }
                    default                 -> drawFloor(g, px, py, x, y);
                }
            }
        }
    }

    private void drawFloor(Graphics2D g, int px, int py, int x, int y) {
        g.setColor((x + y) % 2 == 0 ? FLOOR_A : FLOOR_B);
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
    }

    private void drawWall(Graphics2D g, int px, int py) {
        GradientPaint gp = new GradientPaint(px, py, WALL_LIGHT, px+TILE_SIZE, py+TILE_SIZE, WALL_DARK);
        g.setPaint(gp);
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        g.setPaint(null);
        g.setColor(WALL_EDGE);
        g.drawLine(px, py, px+TILE_SIZE-1, py);
        g.drawLine(px, py, px, py+TILE_SIZE-1);
        g.setColor(WALL_DARK.darker());
        g.drawLine(px+TILE_SIZE-1, py, px+TILE_SIZE-1, py+TILE_SIZE-1);
        g.drawLine(px, py+TILE_SIZE-1, px+TILE_SIZE-1, py+TILE_SIZE-1);
        g.setColor(new Color(80, 80, 120, 100));
        g.fillRect(px+TILE_SIZE/2-2, py+TILE_SIZE/2-2, 4, 4);
    }

    private void drawBlock(Graphics2D g, int px, int py) {
        g.setColor(BLOCK_DARK);
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        g.setColor(BLOCK_LIGHT);
        g.fillRect(px+2, py+2, TILE_SIZE-4, TILE_SIZE-4);
        g.setColor(BLOCK_GRAIN);
        g.drawLine(px+6,  py+2, px+6,  py+TILE_SIZE-2);
        g.drawLine(px+16, py+2, px+16, py+TILE_SIZE-2);
        g.drawLine(px+26, py+2, px+26, py+TILE_SIZE-2);
        g.drawLine(px+36, py+2, px+36, py+TILE_SIZE-2);
        g.setColor(BLOCK_DARK.darker());
        g.drawLine(px+11, py+2, px+11, py+TILE_SIZE-2);
        g.drawLine(px+21, py+2, px+21, py+TILE_SIZE-2);
        g.setColor(BLOCK_GRAIN.brighter());
        g.drawLine(px+2, py+2, px+TILE_SIZE-3, py+2);
        g.drawLine(px+2, py+2, px+2, py+TILE_SIZE-3);
        g.setColor(BLOCK_DARK.darker());
        g.drawLine(px+TILE_SIZE-3, py+2, px+TILE_SIZE-3, py+TILE_SIZE-3);
        g.drawLine(px+2, py+TILE_SIZE-3, px+TILE_SIZE-3, py+TILE_SIZE-3);
    }

    private void drawExplosion(Graphics2D g, int px, int py) {
        long t = System.currentTimeMillis();
        int m = (int)(4 * (0.7f + 0.3f * (float)Math.sin(t * 0.025)));
        g.setColor(new Color(EXPL_OUTER.getRed(), EXPL_OUTER.getGreen(), EXPL_OUTER.getBlue(), 120));
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        g.setColor(EXPL_MID);
        g.fillRoundRect(px+m, py+m, TILE_SIZE-m*2, TILE_SIZE-m*2, 10, 10);
        g.setColor(EXPL_CORE);
        g.fillRoundRect(px+m*2+2, py+m*2+2, TILE_SIZE-m*4-4, TILE_SIZE-m*4-4, 6, 6);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.setColor(EXPL_MID);
        g.fillRoundRect(px-2, py-2, TILE_SIZE+4, TILE_SIZE+4, 8, 8);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawItems(Graphics2D g) {
        ItemType[][] items = model.getItems();
        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                if (items[x][y] == ItemType.NONE) continue;
                int px = x*TILE_SIZE, py = y*TILE_SIZE;
                drawFloor(g, px, py, x, y);
                Color c = itemColor(items[x][y]);
                int m = 7;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g.setColor(c);
                g.fillRoundRect(px+m-3, py+m-3, TILE_SIZE-m*2+6, TILE_SIZE-m*2+6, 12, 12);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g.setColor(c.darker().darker());
                g.fillRoundRect(px+m, py+m, TILE_SIZE-m*2, TILE_SIZE-m*2, 8, 8);
                g.setColor(c);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(px+m, py+m, TILE_SIZE-m*2-1, TILE_SIZE-m*2-1, 8, 8);
                g.setStroke(new BasicStroke(1f));
                g.setFont(new Font("Monospaced", Font.BOLD, 13));
                g.setColor(Color.WHITE);
                String lbl = itemLabel(items[x][y]);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(lbl, px+(TILE_SIZE-fm.stringWidth(lbl))/2,
                                  py+TILE_SIZE/2+fm.getAscent()/3);
            }
        }
    }

    private Color itemColor(ItemType t) {
        return switch(t) {
            case BONUS_SPEED      -> ITEM_SPEED;
            case BONUS_BOMB_COUNT -> ITEM_BOMB;
            case BONUS_RANGE      -> ITEM_RANGE;
            case MALUS_SLOW       -> ITEM_SLOW;
            default               -> Color.WHITE;
        };
    }

    private String itemLabel(ItemType t) {
        return switch(t) {
            case BONUS_SPEED      -> "▲V";
            case BONUS_BOMB_COUNT -> "+B";
            case BONUS_RANGE      -> "▲R";
            case MALUS_SLOW       -> "▼V";
            default               -> "?";
        };
    }

    private void drawBombs(Graphics2D g) {
        for (Bomb b : model.getBombs()) {
            int px = b.getX()*TILE_SIZE, py = b.getY()*TILE_SIZE;
            drawFloor(g, px, py, b.getX(), b.getY());
            long rem = b.getRemainingMs();
            boolean blink = (rem % (rem > 1000 ? 600 : 250)) < (rem > 1000 ? 300 : 125);
            g.setColor(new Color(0,0,0,80));
            g.fillOval(px+8, py+10, TILE_SIZE-12, TILE_SIZE-10);
            GradientPaint gp = new GradientPaint(px+6, py+6, BOMB_SHINE, px+TILE_SIZE-6, py+TILE_SIZE-6, BOMB_BODY);
            g.setPaint(gp);
            g.fillOval(px+7, py+8, TILE_SIZE-14, TILE_SIZE-14);
            g.setPaint(null);
            g.setColor(new Color(0,0,0,120));
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(px+7, py+8, TILE_SIZE-14, TILE_SIZE-14);
            g.setStroke(new BasicStroke(1f));
            int cx = px+TILE_SIZE/2;
            int cy = py+8;
            g.setColor(new Color(80,60,30));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(cx-5, cy-8, 10, 10, 0, 200);
            g.setStroke(new BasicStroke(1f));
            if (blink) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g.setColor(FUSE_LIT);
                g.fillOval(cx+2, cy-10, 7, 7);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g.fillOval(cx-1, cy-13, 13, 13);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            g.setColor(blink ? FUSE_LIT : new Color(200,200,200));
            String cd = String.valueOf((rem/1000)+1);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(cd, px+(TILE_SIZE-fm.stringWidth(cd))/2, py+TILE_SIZE/2+fm.getAscent()/3+3);
        }
    }

    private void drawPlayers(Graphics2D g) {
        for (Player p : model.getPlayers()) {
            if (!p.isAlive()) continue;
            if (p.isInvincible() && (System.currentTimeMillis()/150)%2==0) continue;
            int px = p.getX()*TILE_SIZE, py = p.getY()*TILE_SIZE;
            boolean isP1 = p.getId() == 1;
            Color base = isP1 ? P1_BASE : P2_BASE;
            Color dark = isP1 ? P1_DARK : P2_DARK;
            Color glow = isP1 ? P1_GLOW : P2_GLOW;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
            g.setColor(glow);
            g.fillOval(px+1, py+1, TILE_SIZE-2, TILE_SIZE-2);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            GradientPaint gp = new GradientPaint(px+5, py+4, glow, px+TILE_SIZE-5, py+TILE_SIZE-4, dark);
            g.setPaint(gp);
            g.fillOval(px+4, py+4, TILE_SIZE-8, TILE_SIZE-8);
            g.setPaint(null);
            g.setColor(base.brighter());
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(px+4, py+4, TILE_SIZE-9, TILE_SIZE-9);
            g.setStroke(new BasicStroke(1f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g.setColor(Color.WHITE);
            g.fillOval(px+9, py+7, 10, 7);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            drawFace(g, px, py, isP1, p.getId()==2 && botMode);
        }
    }

    private void drawFace(Graphics2D g, int px, int py, boolean isP1, boolean isBot) {
        int cx = px+TILE_SIZE/2, cy = py+TILE_SIZE/2;
        if (isBot) {
            g.setColor(BOT_ACCENT);
            g.fillRect(cx-7, cy-5, 5, 4);
            g.fillRect(cx+2, cy-5, 5, 4);
            g.setColor(Color.BLACK);
            g.fillRect(cx-6, cy-4, 3, 3);
            g.fillRect(cx+3, cy-4, 3, 3);
            g.setColor(BOT_ACCENT);
            g.fillOval(cx-2, py+2, 4, 4);
        } else {
            g.setColor(Color.WHITE);
            g.fillOval(cx-7, cy-5, 5, 5);
            g.fillOval(cx+2, cy-5, 5, 5);
            g.setColor(Color.BLACK);
            g.fillOval(cx-6, cy-4, 3, 3);
            g.fillOval(cx+3, cy-4, 3, 3);
        }
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx-5, cy-1, 10, 8, 200, 140);
        g.setStroke(new BasicStroke(1f));
    }

    private void drawScanlines(Graphics2D g) {
        int w = getWidth(), h = getHeight();
        if (scanlineOverlay == null || scanlineOverlay.getWidth() != w || scanlineOverlay.getHeight() != h) {
            scanlineOverlay = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg = scanlineOverlay.createGraphics();
            sg.setColor(SCANLINE);
            for (int y = 0; y < h; y += 2) sg.drawLine(0, y, w, y);
            sg.dispose();
        }
        g.drawImage(scanlineOverlay, 0, 0, null);
    }

    private void drawStatPanel(Graphics2D g, Player p, int panelX, int panelH, boolean isLeft, boolean isBot) {
        if (p == null) return;
        Color playerColor = isLeft ? P1_BASE : P2_BASE;
        Color playerGlow  = isLeft ? P1_GLOW : P2_GLOW;

        GradientPaint bg = new GradientPaint(panelX, 0, PANEL_BG,
                panelX + (isLeft ? STAT_WIDTH : -STAT_WIDTH), panelH, BG);
        g.setPaint(bg);
        g.fillRect(panelX, 0, STAT_WIDTH, panelH);
        g.setPaint(null);

        int borderX = isLeft ? panelX+STAT_WIDTH-2 : panelX;
        g.setColor(PANEL_GLOW);
        g.fillRect(borderX, 0, 2, panelH);
        g.setColor(PANEL_BORDER);
        g.drawRect(panelX, 0, STAT_WIDTH-1, panelH-1);

        g.setColor(new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), 30));
        g.fillRect(panelX, 0, STAT_WIDTH, 40);
        g.setColor(playerGlow);
        g.drawLine(panelX+8, 40, panelX+STAT_WIDTH-8, 40);

        String title = isBot ? "⚙ BOT" : ("P" + p.getId());
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(playerGlow);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, panelX+(STAT_WIDTH-fm.stringWidth(title))/2, 27);

        int y = 56;
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(TEXT_DIM);
        g.drawString("LIVES", panelX+14, y);
        y += 18;

        int hs = 20, hw = 3*(hs+5)-5;
        int hsx = panelX+(STAT_WIDTH-hw)/2;
        for (int i = 0; i < 3; i++)
            drawHeart(g, hsx+i*(hs+5), y, hs, i < p.getLives() ? new Color(255,60,60) : new Color(60,20,20));
        y += hs+18;

        g.setColor(PANEL_BORDER);
        g.drawLine(panelX+10, y-8, panelX+STAT_WIDTH-10, y-8);

        y = drawNeonBar(g, panelX, y, "SPEED", p.getSpeedLevel(),    5, isLeft ? P1_BASE : P2_BASE); y += 6;
        y = drawNeonBar(g, panelX, y, "RANGE", p.getBombRange(),     9, new Color(255,140,40));       y += 6;
        y = drawNeonBar(g, panelX, y, "BOMBS", p.getBombCapacity(),  8, new Color(255,200,40));       y += 10;

        if (p.getActiveBombs() > 0) {
            g.setFont(new Font("Monospaced", Font.BOLD, 10));
            g.setColor(new Color(255,200,60));
            g.drawString("💣 " + p.getActiveBombs() + " active", panelX+12, y);
            y += 16;
        }

        if (!p.isAlive()) {
            g.setFont(new Font("Monospaced", Font.BOLD, 13));
            g.setColor(new Color(200,40,40));
            String dead = "ELIMINATED";
            FontMetrics fm2 = g.getFontMetrics();
            g.drawString(dead, panelX+(STAT_WIDTH-fm2.stringWidth(dead))/2, y+10);
        } else if (p.isInvincible() && (System.currentTimeMillis()/300)%2==0) {
            g.setFont(new Font("Monospaced", Font.BOLD, 10));
            g.setColor(new Color(255,230,80));
            String inv = "★ SHIELD ★";
            FontMetrics fm2 = g.getFontMetrics();
            g.drawString(inv, panelX+(STAT_WIDTH-fm2.stringWidth(inv))/2, y+10);
        }
    }

    private int drawNeonBar(Graphics2D g, int panelX, int y, String label, int value, int maxValue, Color barColor) {
        int innerW = STAT_WIDTH-24, barH = 9;
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(TEXT_DIM);
        g.drawString(label, panelX+14, y);
        y += 14;
        g.setColor(new Color(20,20,40));
        g.fillRoundRect(panelX+12, y, innerW, barH, 5, 5);
        int filled = Math.max(0, (int)Math.round((double)value/maxValue*innerW));
        if (filled > 0) {
            GradientPaint gp = new GradientPaint(panelX+12, y, barColor.brighter(), panelX+12+filled, y+barH, barColor.darker());
            g.setPaint(gp);
            g.fillRoundRect(panelX+12, y, filled, barH, 5, 5);
            g.setPaint(null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g.setColor(barColor);
            g.fillRoundRect(panelX+12, y-1, filled, barH+2, 5, 5);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.setColor(Color.WHITE);
        String val = value+"/"+maxValue;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(val, panelX+STAT_WIDTH-fm.stringWidth(val)-12, y+barH-1);
        return y+barH+6;
    }

    private void drawHeart(Graphics2D g, int x, int y, int size, Color color) {
        if (color.getRed() > 100) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g.setColor(color);
            g.fillOval(x-2, y-2, size+4, size+4);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
        g.setColor(color);
        g.fillOval(x, y, size/2, size/2);
        g.fillOval(x+size/2-size/4, y, size/2, size/2);
        int[] xs = {x, x+size, x+size/2};
        int[] ys = {y+size/3, y+size/3, y+size};
        g.fillPolygon(xs, ys, 3);
    }

    private void drawBottomBar(Graphics2D g) {
        int totalW = getWidth(), barY = model.getHeight()*TILE_SIZE;
        g.setColor(new Color(6,6,14));
        g.fillRect(0, barY, totalW, BOTTOM_BAR);
        g.setColor(PANEL_GLOW);
        g.drawLine(0, barY, totalW, barY);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.setColor(P1_GLOW);
        g.drawString("P1: ↑↓←→   ENTER=Bomb", 14, barY+26);
        g.setColor(botMode ? BOT_ACCENT : P2_GLOW);
        String right = botMode ? "P2: BOT  (R=Restart  ESC=Quit)" : "P2: WASD  SPACE=Bomb";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(right, totalW-fm.stringWidth(right)-14, barY+26);
    }

    private void drawGameOver(Graphics2D g) {
        int totalW = getWidth(), totalH = getHeight();
        g.setColor(new Color(0,0,0,200));
        g.fillRect(0, 0, totalW, totalH);
        Player winner = model.getWinner();
        String title  = (winner != null) ? "PLAYER "+winner.getId()+" WINS!" : "DRAW!";
        Color  tColor = (winner != null)
                ? (winner.getId()==1 ? P1_GLOW : (botMode ? BOT_ACCENT : P2_GLOW))
                : new Color(255,220,80);
        int cx = totalW/2, cy = totalH/2-50;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g.setColor(tColor);
        g.fillRoundRect(cx-160, cy-50, 320, 60, 16, 16);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setFont(new Font("Monospaced", Font.BOLD, 46));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(0,0,0,120));
        g.drawString(title, cx-fm.stringWidth(title)/2+2, cy+2);
        g.setColor(tColor);
        g.drawString(title, cx-fm.stringWidth(title)/2, cy);
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.setColor(TEXT_DIM);
        for (Player p : model.getPlayers()) {
            String s = "Player "+p.getId()+" — "+p.getLives()+" life/lives remaining";
            FontMetrics fm2 = g.getFontMetrics();
            int offset = (p.getId()==1) ? -18 : 10;
            g.drawString(s, cx-fm2.stringWidth(s)/2, cy+35+offset);
        }
        int btnW=200, btnH=50, btnX=cx-btnW/2, btnY=cy+80;
        restartButtonBounds = new Rectangle(btnX, btnY, btnW, btnH);
        g.setColor(new Color(0,180,80,200));
        g.fill(new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 14, 14));
        g.setColor(BOT_ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Float(btnX, btnY, btnW-1, btnH-1, 14, 14));
        g.setStroke(new BasicStroke(1f));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g.setColor(BOT_ACCENT);
        g.fill(new RoundRectangle2D.Float(btnX-3, btnY-3, btnW+6, btnH+6, 18, 18));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        FontMetrics fmBtn = g.getFontMetrics();
        String btnTxt = "▶  PLAY AGAIN";
        g.drawString(btnTxt, btnX+(btnW-fmBtn.stringWidth(btnTxt))/2, btnY+btnH/2+fmBtn.getAscent()/3);
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(TEXT_DIM);
        String hint = "or press  R";
        FontMetrics fmH = g.getFontMetrics();
        g.drawString(hint, cx-fmH.stringWidth(hint)/2, btnY+btnH+22);
    }
}