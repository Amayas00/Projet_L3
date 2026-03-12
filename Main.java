import src.controller.GameController;
import src.model.BombermanModel;
import src.model.GameSettings;
import src.View.GameView;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameSettings   settings = new GameSettings();
            BombermanModel model    = new BombermanModel(
                    settings.getMapWidth(),
                    settings.getMapHeight(),
                    settings);
            GameView view = new GameView(model);
            new GameController(model, view, settings);
            view.display();
        });
    }
}
