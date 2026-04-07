import javax.swing.SwingUtilities;
import src.model.BombermanModel;
import src.model.GameSettings;
import src.View.BombermanView;
import src.controller.GameController;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameSettings settings = new GameSettings();

            BombermanModel model = new BombermanModel(
                    settings.getMapWidth(),
                    settings.getMapHeight(),
                    settings);

            BombermanView view = new BombermanView(model);

            // GameController remplace BombermanController (doublon supprimé)
            new GameController(model, view, settings);

            view.display();
        });
    }
}
