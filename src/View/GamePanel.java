package src.View;

import javax.swing.JPanel;
import src.model.BombermanModel;
import src.model.Player;
import src.model.TileType;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;

public class GamePanel extends JPanel {
    private BombermanModel model;
    public static final int TILE_SIZE = 40; 

    public GamePanel(BombermanModel model) {
        this.model = model;
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        int widthPx = model.getGrid().length * TILE_SIZE;
        int heightPx = model.getGrid()[0].length * TILE_SIZE;
        this.setPreferredSize(new Dimension(widthPx, heightPx));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (model == null) return;

        TileType[][] grid = model.getGrid();
        
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                switch (grid[x][y]) {
                    case WALL:
                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        break;
                    case DESTRUCTIBLE_BLOCK:
                        g.setColor(new Color(139, 69, 19));
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                        break;
                    case EMPTY:
                        g.setColor(new Color(34, 139, 34));
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        break;
                    case BOMB:
                        g.setColor(new Color(34, 139, 34));
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g.setColor(Color.BLACK);
                        g.fillOval(px + 5, py + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                        break;
                    case EXPLOSION:
                        g.setColor(Color.ORANGE);
                        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        break;
                }
            }
        }

        for (Player p : model.getPlayers()) {
            if (p.isAlive()) {
                g.setColor(p.getId() == 1 ? Color.BLUE : Color.RED);
                int px = p.getX() * TILE_SIZE;
                int py = p.getY() * TILE_SIZE;
                g.fillOval(px + 4, py + 4, TILE_SIZE - 8, TILE_SIZE - 8);
            }
        }
    }
}