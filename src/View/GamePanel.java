package src.View;

import javax.swing.JPanel;
import src.model.BombermanModel;
import src.model.Player;
import src.model.TileType;
import src.model.ItemType;
import src.model.Bomb;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GamePanel extends JPanel {
    private boolean restartClicked = false;
    private final BombermanModel model;

    public static final int TILE_SIZE = 40;
    public static final int UI_WIDTH = 120;

    public GamePanel(BombermanModel model) {
        this.model = model;
        
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        int widthPx = (model.getWidth() * TILE_SIZE) + (UI_WIDTH * 2);
        int heightPx = model.getHeight() * TILE_SIZE;
        this.setPreferredSize(new Dimension(widthPx, heightPx));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (model.getPhase() == BombermanModel.GamePhase.GAME_OVER) {
                    triggerRestart();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (model == null) return;

        TileType[][] grid = model.getGrid();
        ItemType[][] items = model.getItems();

        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                int px = UI_WIDTH + (x * TILE_SIZE);
                int py = y * TILE_SIZE;

                drawGrass(g, x, y, px, py);

                if (grid[x][y] == TileType.WALL) {
                    drawWall(g, px, py);
                } else if (grid[x][y] == TileType.DESTRUCTIBLE_BLOCK) {
                    drawDestructibleBlock(g, px, py);
                } else if (grid[x][y] == TileType.EXPLOSION) {
                    drawExplosion(g, px, py);
                }

                if (items[x][y] != null && items[x][y] != ItemType.NONE) {
                    drawItem(g, items[x][y], px, py);
                }
            }
        }

        for (Bomb b : model.getBombs()) {
            drawBomb(g, UI_WIDTH + (b.getX() * TILE_SIZE), b.getY() * TILE_SIZE);
        }

        for (Player p : model.getPlayers()) {
            if (p.isAlive()) {
                if (p.isInvulnerable() && (System.currentTimeMillis() / 150) % 2 == 0) {
                    continue; 
                }
                drawHumanPlayer(g, p, UI_WIDTH);
            }
        }

        drawInterface(g);

        if (model.getPhase() == BombermanModel.GamePhase.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            
            Player winner = model.getWinner();
            String winText = (winner != null) ? "PLAYER " + winner.getId() + " WINS!" : "IT'S A DRAW!";
            
            g.drawString(winText, getWidth() / 2 - 110, getHeight() / 2 - 20);
            
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Press 'R' or Click to Restart", getWidth() / 2 - 110, getHeight() / 2 + 20);
        }
    }

    private void drawInterface(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 16));

        for (Player p : model.getPlayers()) {
            int panelX = (p.getId() == 1) ? 0 : UI_WIDTH + (model.getWidth() * TILE_SIZE);
            
            g.setColor(new Color(40, 40, 40));
            g.fillRect(panelX, 0, UI_WIDTH, getHeight());

            g.setColor(Color.WHITE);
            g.drawString("PLAYER " + p.getId(), panelX + 20, 40);
            
            int avatarX = panelX + (UI_WIDTH / 2) - 20;
            drawDummyAvatar(g, avatarX, 70, p.getId() == 1 ? Color.BLUE : Color.RED);

            g.setColor(Color.WHITE);
            g.drawString("LIVES: " + p.getLives(), panelX + 20, 150);
            for (int i = 0; i < p.getLives(); i++) {
                g.setColor(Color.RED);
                g.fillOval(panelX + 20 + (i * 20), 160, 15, 15);
            }

            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Bombs: " + p.getBombCapacity(), panelX + 20, 210);
            g.drawString("Range: " + p.getBombRange(), panelX + 20, 230);
            
            g.setFont(new Font("Arial", Font.BOLD, 16));
        }
    }

    private void drawDummyAvatar(Graphics g, int px, int py, Color shirtColor) {
        g.setColor(new Color(255, 224, 189));
        g.fillOval(px, py, 20, 20);
        g.setColor(shirtColor);
        g.fillRect(px - 5, py + 20, 30, 20);
    }

    private void drawGrass(Graphics g, int x, int y, int px, int py) {
        boolean isEven = (x + y) % 2 == 0;
        g.setColor(isEven ? new Color(124, 252, 0) : new Color(50, 205, 50)); 
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
    }

    private void drawWall(Graphics g, int px, int py) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(px, py, TILE_SIZE, 4); 
        g.fillRect(px, py, 4, TILE_SIZE); 
        g.setColor(Color.BLACK);
        g.fillRect(px, py + TILE_SIZE - 4, TILE_SIZE, 4); 
        g.fillRect(px + TILE_SIZE - 4, py, 4, TILE_SIZE); 
    }

    private void drawDestructibleBlock(Graphics g, int px, int py) {
        g.setColor(new Color(205, 133, 63)); 
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        g.setColor(new Color(139, 69, 19)); 
        g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
        g.drawLine(px, py + TILE_SIZE/2, px + TILE_SIZE, py + TILE_SIZE/2);
        g.drawLine(px + TILE_SIZE/2, py, px + TILE_SIZE/2, py + TILE_SIZE/2);
        g.drawLine(px + TILE_SIZE/4, py + TILE_SIZE/2, px + TILE_SIZE/4, py + TILE_SIZE);
        g.drawLine(px + 3*TILE_SIZE/4, py + TILE_SIZE/2, px + 3*TILE_SIZE/4, py + TILE_SIZE);
    }

    private void drawExplosion(Graphics g, int px, int py) {
        g.setColor(Color.ORANGE);
        g.fillRect(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        g.setColor(Color.RED);
        g.fillOval(px + 8, py + 8, TILE_SIZE - 16, TILE_SIZE - 16);
        g.setColor(Color.YELLOW);
        g.fillOval(px + 14, py + 14, TILE_SIZE - 28, TILE_SIZE - 28);
    }

    private void drawBomb(Graphics g, int px, int py) {
        g.setColor(Color.BLACK);
        g.fillOval(px + 6, py + 10, TILE_SIZE - 12, TILE_SIZE - 14);
        g.setColor(Color.WHITE);
        g.fillOval(px + 12, py + 14, 8, 8);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(px + TILE_SIZE/2 - 2, py + 6, 4, 6);
        g.setColor((System.currentTimeMillis() / 150 % 2 == 0) ? Color.RED : Color.YELLOW);
        g.fillOval(px + TILE_SIZE/2 - 5, py + 2, 10, 10);
    }

    private void drawHumanPlayer(Graphics g, Player p, int offsetX) {
        int px = offsetX + (p.getX() * TILE_SIZE);
        int py = p.getY() * TILE_SIZE;

        Color skinColor = new Color(255, 224, 189);
        Color shirtColor = (p.getId() == 1) ? Color.BLUE : Color.RED;
        Color pantsColor = Color.DARK_GRAY;

        g.setColor(skinColor);
        g.fillOval(px + TILE_SIZE/2 - 7, py + 4, 14, 14);
        g.setColor(Color.BLACK);
        g.fillOval(px + TILE_SIZE/2 - 4, py + 8, 3, 3); 
        g.fillOval(px + TILE_SIZE/2 + 1, py + 8, 3, 3); 
        g.setColor(shirtColor);
        g.fillRect(px + TILE_SIZE/2 - 8, py + 18, 16, 12);
        g.setColor(skinColor);
        g.fillRect(px + TILE_SIZE/2 - 12, py + 18, 4, 10); 
        g.fillRect(px + TILE_SIZE/2 + 8, py + 18, 4, 10);  
        g.setColor(pantsColor);
        g.fillRect(px + TILE_SIZE/2 - 7, py + 30, 6, 8); 
        g.fillRect(px + TILE_SIZE/2 + 1, py + 30, 6, 8); 
    }

    private void drawItem(Graphics g, ItemType type, int px, int py) {
        g.setColor(Color.WHITE); 
        g.fillRoundRect(px + 8, py + 8, TILE_SIZE - 16, TILE_SIZE - 16, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(px + 8, py + 8, TILE_SIZE - 16, TILE_SIZE - 16, 10, 10);

        switch(type) {
            case BONUS_SPEED: 
                g.setColor(Color.BLUE); 
                g.drawString(">>", px + 14, py + 24);
                break;
            case BONUS_BOMB_COUNT: 
                g.setColor(Color.BLACK); 
                g.fillOval(px + 14, py + 14, 12, 12);
                break;
            case BONUS_RANGE: 
                g.setColor(Color.RED); 
                g.fillRect(px + 12, py + 18, 16, 4);
                g.fillRect(px + 18, py + 12, 4, 16);
                break;
            case MALUS_SLOW: 
                g.setColor(Color.DARK_GRAY); 
                g.fillOval(px + 16, py + 16, 8, 8);
                break;
            default: return;
        }
    }

    public boolean isRestartClicked() {
        if (restartClicked) {
            restartClicked = false;
            return true;
        }
        return false;
    }

    public void triggerRestart() {
        this.restartClicked = true;
    }
}