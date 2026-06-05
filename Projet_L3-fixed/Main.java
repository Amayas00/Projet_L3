import src.model.*;
import src.View.*;
import src.controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showMenu);
    }

    private static void showMenu() {
        Color BG          = new Color(8,  8, 16);
        Color ACCENT      = new Color(255, 220, 40);
        Color TEXT_DIM    = new Color(120, 120, 160);
        Color HUMAN_COLOR = new Color(80, 180, 255);
        Color BOT_COLOR   = new Color(160, 255, 160);

        JFrame menu = new JFrame("Bomberman – Select Mode");
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menu.setResizable(false);
        menu.getContentPane().setBackground(BG);
        menu.setLayout(new BorderLayout());

        // ── TITLE ──────────────────────────────────────────────────────────────
        JLabel title = new JLabel("💣 BOMBERMAN", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 36));
        title.setForeground(ACCENT);
        title.setBorder(BorderFactory.createEmptyBorder(30, 20, 6, 20));
        menu.add(title, BorderLayout.NORTH);

        // ── CENTER ─────────────────────────────────────────────────────────────
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(8, 36, 16, 36));

        JLabel sub = new JLabel("Choose a game mode", SwingConstants.CENTER);
        sub.setFont(new Font("Monospaced", Font.PLAIN, 13));
        sub.setForeground(TEXT_DIM);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(sub);
        center.add(Box.createVerticalStrut(22));

        // ── HUMAN VS HUMAN SECTION ─────────────────────────────────────────────
        JLabel humanLabel = makeSectionLabel("👥  HUMAN VS HUMAN", HUMAN_COLOR);
        center.add(humanLabel);
        center.add(Box.createVerticalStrut(8));

        JButton btn2p = makeMenuButton("  2 PLAYERS", HUMAN_COLOR, BG, "⚔");
        btn2p.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(btn2p);
        center.add(Box.createVerticalStrut(20));

        // ── DIVIDER ────────────────────────────────────────────────────────────
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(40, 40, 80));
                g2.fillRect(0, getHeight()/2, getWidth(), 1);
            }
        };
        sep.setOpaque(false);
        sep.setMaximumSize(new Dimension(320, 12));
        center.add(sep);
        center.add(Box.createVerticalStrut(12));

        // ── HUMAN VS BOT SECTION ───────────────────────────────────────────────
        JLabel botLabel = makeSectionLabel("🤖  HUMAN VS AI", BOT_COLOR);
        center.add(botLabel);
        center.add(Box.createVerticalStrut(8));

        JButton btnEasy   = makeMenuButton("  EASY",   new Color(100, 230, 100), BG, "🤖");
        JButton btnMedium = makeMenuButton("  MEDIUM", new Color( 60, 220, 255), BG, "🤖");
        JButton btnHard   = makeMenuButton("  HARD",   new Color(255,  90,  90), BG, "🤖");

        for (JButton b : new JButton[]{btnEasy, btnMedium, btnHard})
            b.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(btnEasy);
        center.add(Box.createVerticalStrut(7));
        center.add(btnMedium);
        center.add(Box.createVerticalStrut(7));
        center.add(btnHard);

        menu.add(center, BorderLayout.CENTER);

        // ── FOOTER ─────────────────────────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel f1 = new JLabel("P1: Arrow Keys + Enter  |  P2: WASD + Space", SwingConstants.CENTER);
        f1.setFont(new Font("Monospaced", Font.PLAIN, 10));
        f1.setForeground(TEXT_DIM);
        f1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel f2 = new JLabel("ESC = Quit  |  R = Restart", SwingConstants.CENTER);
        f2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        f2.setForeground(new Color(80, 80, 100));
        f2.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.add(f1);
        footer.add(Box.createVerticalStrut(3));
        footer.add(f2);
        menu.add(footer, BorderLayout.SOUTH);

        // ── ACTIONS ────────────────────────────────────────────────────────────
        btn2p    .addActionListener(e -> { menu.dispose(); launchGame(false, BotAI.Difficulty.MEDIUM); });
        btnEasy  .addActionListener(e -> { menu.dispose(); launchGame(true,  BotAI.Difficulty.EASY);   });
        btnMedium.addActionListener(e -> { menu.dispose(); launchGame(true,  BotAI.Difficulty.MEDIUM); });
        btnHard  .addActionListener(e -> { menu.dispose(); launchGame(true,  BotAI.Difficulty.HARD);   });

        menu.pack();
        menu.setMinimumSize(new Dimension(400, 500));
        menu.setLocationRelativeTo(null);
        menu.setVisible(true);
    }

    private static JLabel makeSectionLabel(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        lbl.setForeground(color.darker());
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        return lbl;
    }

    private static JButton makeMenuButton(String text, Color fg, Color bg, String icon) {
        String label = icon + " " + text;
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                // Background fill
                Color fill = hover
                        ? new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 55)
                        : new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 18);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Left accent bar
                g2.setColor(hover ? fg : fg.darker());
                g2.fillRoundRect(0, 6, 4, getHeight()-12, 4, 4);
                // Border
                g2.setColor(hover ? fg : new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Monospaced", Font.BOLD, 15));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 44));
        btn.setMaximumSize (new Dimension(300, 44));
        return btn;
    }

    private static void launchGame(boolean botMode, BotAI.Difficulty diff) {
        GameSettings settings = new GameSettings();
        BombermanModel model  = new BombermanModel(settings.getMapWidth(), settings.getMapHeight(), settings);
        BombermanView  view   = new BombermanView(model);
        view.getGamePanel().setBotMode(botMode);
        new GameController(model, view, settings, botMode, diff);
        view.display();
    }
}