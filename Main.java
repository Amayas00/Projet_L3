import javax.swing.SwingUtilities;
import src.model.BombermanModel;
import src.model.GameSettings;
import src.View.BombermanView;
import src.controller.BombermanController;

public class Main {
    public static void main(String[] args) {
        // Lancement propre de l'interface graphique Swing
        SwingUtilities.invokeLater(() -> {
            // Paramètres (permet de modifier la taille de la carte) 
            GameSettings settings = new GameSettings();
            
            // Instanciation de l'architecture MVC
            BombermanModel model = new BombermanModel(settings.getMapWidth(), settings.getMapHeight());
            BombermanView view = new BombermanView(model);
            BombermanController controller = new BombermanController(model, view);
            
            view.display();
        });
    }
}