package src.View;

import src.model.BombermanModel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class GameView extends JFrame {

    private final GamePanel gamePanel;

    public GameView(BombermanModel model) {
        setTitle("Bomberman — Prototype v1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel(model);
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
    }

    public void refresh() {
        gamePanel.repaint();
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void display() {
        setVisible(true);
        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
    }
}
