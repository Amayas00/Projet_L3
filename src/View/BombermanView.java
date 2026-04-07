package src.View;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import src.model.BombermanModel;

public class BombermanView extends JFrame {
    private final GamePanel gamePanel;

    public BombermanView(BombermanModel model) {
        this.setTitle("Bomberman - MVC");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        this.gamePanel = new GamePanel(model);
        this.add(gamePanel);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public void updateModel(BombermanModel newModel) {
        gamePanel.updateModel(newModel);
        }

    public void refresh() {
        gamePanel.repaint();
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void display() {
        this.setVisible(true);
        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
    }
}
