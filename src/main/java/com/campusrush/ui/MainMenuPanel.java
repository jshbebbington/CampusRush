package com.campusrush.ui;

import com.campusrush.model.PlayerProfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class MainMenuPanel extends JPanel {

    private final JLabel playerLabel = new JLabel("No profile selected");

    public MainMenuPanel(Runnable onPlay, Runnable onProfiles,
                         Runnable onLeaderboard, Runnable onQuit) {
        setLayout(new BorderLayout(24, 24));
        setBackground(Theme.SKY_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(34, 58, 42, 58));

        JLabel title = new JLabel("CAMPUS RUSH", SwingConstants.CENTER);
        title.setFont(Theme.pixelFont(44, true));
        title.setForeground(Theme.NAVY);

        JLabel subtitle = new JLabel(
                "RUN SMART  •  JUMP HIGH  •  OWN THE LEADERBOARD",
                SwingConstants.CENTER);
        subtitle.setFont(Theme.pixelFont(13, true));
        subtitle.setForeground(Theme.GRASS_DARK);

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);
        add(heading, BorderLayout.NORTH);

        CampusArtPanel art = new CampusArtPanel();
        add(art, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(18, 12));
        bottom.setOpaque(false);
        playerLabel.setFont(Theme.pixelFont(13, true));
        playerLabel.setForeground(Theme.NAVY);
        bottom.add(playerLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 4, 12, 0));
        buttons.setOpaque(false);
        buttons.add(button("PLAY", onPlay, Theme.CORAL));
        buttons.add(button("PROFILES", onProfiles, Theme.PURPLE));
        buttons.add(button("LEADERBOARD", onLeaderboard, Theme.GRASS_DARK));
        buttons.add(button("QUIT", onQuit, Theme.NAVY));
        bottom.add(buttons, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setCurrentPlayer(PlayerProfile player) {
        if (player == null) {
            playerLabel.setText("No profile selected  •  Create one before playing");
        } else {
            playerLabel.setText("PLAYER: " + player.displayName()
                    + "  •  READY TO RUN");
        }
    }

    private JButton button(String text, Runnable action, Color color) {
        JButton button = new JButton(text);
        button.setFont(Theme.pixelFont(13, true));
        Theme.styleButton(button, color, Color.WHITE);
        button.setMargin(new Insets(8, 8, 8, 8));
        button.addActionListener(event -> action.run());
        return button;
    }

    private static final class CampusArtPanel extends JPanel {

        private CampusArtPanel() {
            setPreferredSize(new Dimension(800, 260));
            setBackground(Theme.SKY);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            int w = getWidth();
            int h = getHeight();

            g.setColor(Theme.SKY);
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(235, 255, 251));
            g.fillRoundRect(58, 45, 170, 34, 18, 18);
            g.fillRoundRect(w - 260, 70, 190, 38, 20, 20);
            g.setColor(Theme.SUN);
            g.fillRect(w - 120, 20, 48, 48);

            g.setColor(Theme.HILL);
            int[] hillX = {0, 110, 230, 390, 560, 710, 820, w};
            int[] hillY = {174, 146, 164, 119, 160, 127, 150, 132};
            for (int i = 0; i < hillX.length - 1; i++) {
                g.fillPolygon(new int[]{hillX[i], hillX[i + 1], hillX[i + 1],
                        hillX[i]}, new int[]{hillY[i], hillY[i + 1], h, h}, 4);
            }

            g.setColor(Theme.GRASS);
            g.fillRect(0, 188, w, h - 188);
            g.setColor(Theme.TRACK);
            g.fillRect(0, 218, w, 42);
            g.setColor(Theme.TRACK_LINE);
            g.fillRect(0, 228, w, 4);
            g.fillRect(0, 247, w, 4);

            drawTree(g, 92, 184, 1.0);
            drawTree(g, w - 150, 184, 1.15);
            drawRunner(g, 170, 165);
            g.dispose();
        }

        private void drawTree(java.awt.Graphics2D g, int x, int y,
                              double scale) {
            int trunkWidth = (int) (12 * scale);
            int trunkHeight = (int) (42 * scale);
            g.setColor(new Color(117, 76, 45));
            g.fillRect(x, y, trunkWidth, trunkHeight);
            g.setColor(Theme.GRASS_DARK);
            g.fillRect(x - 22, y - 18, (int) (60 * scale),
                    (int) (42 * scale));
            g.fillRect(x - 8, y - 38, (int) (38 * scale),
                    (int) (46 * scale));
        }

        private void drawRunner(java.awt.Graphics2D g, int x, int y) {
            g.setColor(new Color(255, 205, 153));
            g.fillRect(x + 15, y - 35, 18, 18);
            g.setColor(Theme.NAVY);
            g.fillRect(x + 12, y - 19, 24, 35);
            g.setColor(Theme.CORAL);
            g.fillRect(x + 8, y + 16, 10, 30);
            g.fillRect(x + 29, y + 16, 10, 30);
            g.setColor(Theme.CREAM);
            g.fillRect(x + 1, y - 12, 14, 8);
            g.fillRect(x + 34, y - 5, 17, 8);
        }
    }
}
