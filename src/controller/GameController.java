package src.controller;

import src.model.BombermanModel;
import src.model.GameSettings;
import src.View.BombermanView;
import src.View.GameView;

import javax.swing.Timer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameController extends KeyAdapter {

    private BombermanModel  model;
    private GameView view;
    private final GameSettings settings;
    private final Timer gameLoop;

    public GameController(BombermanModel model, GameView view, GameSettings settings) {
        this.model = model;
        this.view = view;
        this.settings = settings;

        view.getGamePanel().addKeyListener(this);

        gameLoop = new Timer(settings.getGameSpeed(), e -> tick());
        gameLoop.start();
    }

    private void tick() {
        model.update();
        view.refresh();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_UP) model.movePlayer(1,  0, -1);
        if (k == KeyEvent.VK_DOWN) model.movePlayer(1,  0,  1);
        if (k == KeyEvent.VK_LEFT) model.movePlayer(1, -1,  0);
        if (k == KeyEvent.VK_RIGHT) model.movePlayer(1,  1,  0);
        if (k == KeyEvent.VK_ENTER) model.placeBomb(1);

        if (k == KeyEvent.VK_W) model.movePlayer(2,  0, -1);
        if (k == KeyEvent.VK_S) model.movePlayer(2,  0,  1);
        if (k == KeyEvent.VK_A) model.movePlayer(2, -1,  0);
        if (k == KeyEvent.VK_D) model.movePlayer(2,  1,  0);
        if (k == KeyEvent.VK_SPACE) model.placeBomb(2);

        if (k == KeyEvent.VK_R && model.getPhase() == BombermanModel.GamePhase.GAME_OVER) {
            restartGame();
        }

        if (k == KeyEvent.VK_ESCAPE) System.exit(0);
    }
    
    private void restartGame() {
        gameLoop.stop();
        view.dispose();

        BombermanModel newModel = new BombermanModel(
                settings.getMapWidth(),
                settings.getMapHeight(),
                settings);
        GameView newView = new GameView(newModel);
        new GameController(newModel, newView, settings);
        newView.display();
    }
}
