package src.controller;

import src.model.BombermanModel;
import src.model.GameSettings;
import src.View.BombermanView;
import javax.swing.Timer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class GameController extends KeyAdapter {
    private BombermanModel model;
    private final BombermanView view;
    private final GameSettings settings;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Timer gameLoop;

    public GameController(BombermanModel model, BombermanView view, GameSettings settings) {
        this.model = model;
        this.view = view;
        this.settings = settings;

        view.getGamePanel().addKeyListener(this);

        gameLoop = new Timer(settings.getGameSpeed(), e -> tick());
        gameLoop.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        pressedKeys.add(code);

        if (code == KeyEvent.VK_ENTER) model.placeBomb(1);
        if (code == KeyEvent.VK_SPACE) model.placeBomb(2);

        if (code == KeyEvent.VK_R && model.getPhase() == BombermanModel.GamePhase.GAME_OVER) {
            restartGame();
        }

        if (code == KeyEvent.VK_ESCAPE) System.exit(0);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    private void tick() {
        if (view.getGamePanel().isRestartClicked()) {
            restartGame();
            return; 
        }

        if (model.getPhase() == BombermanModel.GamePhase.PLAYING) {
            handleMovement();
        }

        model.update();
        view.refresh();
    }

    private void handleMovement() {
        if (pressedKeys.contains(KeyEvent.VK_UP))    model.movePlayer(1,  0, -1);
        if (pressedKeys.contains(KeyEvent.VK_DOWN))  model.movePlayer(1,  0,  1);
        if (pressedKeys.contains(KeyEvent.VK_LEFT))  model.movePlayer(1, -1,  0);
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) model.movePlayer(1,  1,  0);

        if (pressedKeys.contains(KeyEvent.VK_W)) model.movePlayer(2,  0, -1);
        if (pressedKeys.contains(KeyEvent.VK_S)) model.movePlayer(2,  0,  1);
        if (pressedKeys.contains(KeyEvent.VK_A)) model.movePlayer(2, -1,  0);
        if (pressedKeys.contains(KeyEvent.VK_D)) model.movePlayer(2,  1,  0);
    }

    private void restartGame() {
        model.resetGame();
        pressedKeys.clear();
        view.refresh();
        view.getGamePanel().requestFocusInWindow();
    }
}