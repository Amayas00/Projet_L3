import javax.swing.SwingUtilities;
import src.model.BombermanModel;
import src.model.GameSettings;
import src.View.BombermanView;
import src.controller.GameController;

/**
 * Point d'entrée du jeu.
 *
 * CHANGEMENT : GameSettings est maintenant passé au modèle ET au contrôleur
 * pour que gameSpeed et les autres paramètres soient effectivement utilisés.
 * Avant, GameSettings était instancié mais seuls mapWidth/mapHeight étaient lus.
 */
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
