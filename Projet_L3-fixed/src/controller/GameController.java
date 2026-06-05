package src.controller;

import src.model.BombermanModel;
import src.model.GameSettings;
import src.View.BombermanView;
import javax.swing.Timer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class GameController extends KeyAdapter {// keyadapter qui est une classe abstarite de java Swing
    private BombermanModel model;
    private final BombermanView view;
    private final GameSettings  settings;
    private final Set<Integer>  pressedKeys = new HashSet<>();//on stocke le code dse touches enfoncées 
    private final Timer         gameLoop;

    private final boolean botEnabled;
    private final BotAI   bot;

    // AJOUTÉ : flags pour les bombes, traités dans tick() comme le mouvement.
    // Évite la race condition entre keyPressed (EDT) et tick() (Timer Swing).
    private boolean placeBomb1 = false;
    private boolean placeBomb2 = false;

    public GameController(BombermanModel model, BombermanView view, GameSettings settings) {
        this(model, view, settings, false, BotAI.Difficulty.MEDIUM);
    }

    public GameController(BombermanModel model, BombermanView view,
                          GameSettings settings, boolean botMode, BotAI.Difficulty diff) {
        this.model      = model;
        this.view       = view;
        this.settings   = settings;
        this.botEnabled = botMode;
        this.bot        = botMode ? new BotAI(diff) : null;

        view.getGamePanel().addKeyListener(this);

        gameLoop = new Timer(settings.getGameSpeed(), e -> tick());
        // le timer swing prend un delai en ms et un actionlistener. 
        // e-> tick() est une lambda qui appelle la méthode tick() à 
        // chaque tick du timer.
        


        gameLoop.start();// démarre le timer, qui appelle tick() à intervalle régulier défini par settings.getGameSpeed()
    }



    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        pressedKeys.add(code);

        // MODIFIÉ : on ne pose plus la bombe ici directement.
        // On lève juste un flag, traité au prochain tick().
        if (code == KeyEvent.VK_ENTER)                    placeBomb1 = true;
        if (!botEnabled && code == KeyEvent.VK_SPACE)     placeBomb2 = true;

        if (code == KeyEvent.VK_R && model.getPhase() == BombermanModel.GamePhase.GAME_OVER)
            restartGame();// si le jeu est terminé et que le joueur appuie sur R on relance une partie 

        if (code == KeyEvent.VK_ESCAPE) System.exit(0);
    }

    @Override
    //lorsque une touche enfoncée est relachée, on la retire de l'ensembble des touches presées. 
    //ce mecanisme permet de savoir a tout moment quelles touches sont actuellement enfoncées, sans avoir a tout recalculer.
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    private void tick() {
        if (view.getGamePanel().isRestartClicked()) {
            restartGame();
            return;
        }

        if (model.getPhase() == BombermanModel.GamePhase.PLAYING) {
            handleHumanMovement();
            if (botEnabled) handleBotTurn();
        }

        model.update();
        view.refresh();// rafraîchit l'affichage après la mise à jour du modèle
    }

    private void handleHumanMovement() {
        // AJOUTÉ : pose des bombes depuis le tick, pas depuis keyPressed.
        if (placeBomb1) { model.placeBomb(1); placeBomb1 = false; }
        if (placeBomb2) { model.placeBomb(2); placeBomb2 = false; }

        float dx = 0, dy = 0;
        if (pressedKeys.contains(KeyEvent.VK_LEFT))  dx -= 1;
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) dx += 1;
        if (pressedKeys.contains(KeyEvent.VK_UP))    dy -= 1;
        if (pressedKeys.contains(KeyEvent.VK_DOWN))  dy += 1;
        if (dx != 0 || dy != 0) model.movePlayer(1, dx, dy);

        if (!botEnabled) {
            float dx2 = 0, dy2 = 0;
            if (pressedKeys.contains(KeyEvent.VK_A)) dx2 -= 1;
            if (pressedKeys.contains(KeyEvent.VK_D)) dx2 += 1;
            if (pressedKeys.contains(KeyEvent.VK_W)) dy2 -= 1;
            if (pressedKeys.contains(KeyEvent.VK_S)) dy2 += 1;
            if (dx2 != 0 || dy2 != 0) model.movePlayer(2, dx2, dy2);
        }
    }

    private void handleBotTurn() {
        BotAI.BotAction action = bot.computeAction(model);
        if (action.placeBomb) {
            model.placeBomb(2);
        } else if (action.isMove()) {
            model.movePlayer(2, (float) action.dx, (float) action.dy);
        }
    }

    private void restartGame() {
        model.resetGame();// réinitialise le modèle pour une nouvelle partie
        view.updateModel(model);// met à jour la vue avec le nouveau modèle réinitialisé
        pressedKeys.clear();// réinitialise l'ensemble des touches pressées
        placeBomb1 = false;
        placeBomb2 = false;
        view.refresh();
        view.getGamePanel().requestFocusInWindow();// s'assure que le panneau de jeu a le focus pour recevoir les événements clavier
    }
}