package com.campusrush.ui;

import com.campusrush.exception.AppException;
import com.campusrush.game.GameEngine;
import com.campusrush.game.GameSnapshot;
import com.campusrush.game.ObstacleType;
import com.campusrush.game.ObstacleView;
import com.campusrush.model.PlayerProfile;
import com.campusrush.service.ScoreService;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public final class GamePanel extends JPanel {

    private final ScoreService scoreService;
    private final Runnable onMenu;
    private final GameEngine engine = new GameEngine();
    private final GameCanvas canvas = new GameCanvas();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton againButton = new JButton("PLAY AGAIN");
    private final JButton menuButton = new JButton("MENU");
    private final Timer timer;
    private PlayerProfile player;
    private boolean paused;
    private boolean scoreSaving;

    public GamePanel(ScoreService scoreService, Runnable onMenu) {
        this.scoreService = scoreService;
        this.onMenu = onMenu;
        timer = new Timer(16, event -> gameTick());
        buildUi();
        installKeyBindings();
    }

    public void start(PlayerProfile player) {
        this.player = player;
        paused = false;
        scoreSaving = false;
        engine.reset();
        againButton.setVisible(false);
        statusLabel.setText("TAP / HOLD SPACE: JUMP     HOLD DOWN: DUCK     P: PAUSE");
        timer.start();
        canvas.requestFocusInWindow();
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.NAVY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel.setFont(Theme.pixelFont(12, true));
        statusLabel.setForeground(Color.WHITE);
        add(statusLabel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        Theme.styleButton(againButton, Theme.SUN, Theme.NAVY);
        Theme.styleButton(menuButton, Theme.SKY_LIGHT, Theme.NAVY);
        menuButton.addActionListener(event -> {
            timer.stop();
            onMenu.run();
        });
        againButton.addActionListener(event -> start(player));
        bottom.add(againButton, BorderLayout.WEST);
        bottom.add(menuButton, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void installKeyBindings() {
        InputMap inputMap = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = canvas.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("pressed SPACE"), "jumpStart");
        inputMap.put(KeyStroke.getKeyStroke("released SPACE"), "jumpRelease");
        inputMap.put(KeyStroke.getKeyStroke("pressed UP"), "jumpStart");
        inputMap.put(KeyStroke.getKeyStroke("released UP"), "jumpRelease");
        inputMap.put(KeyStroke.getKeyStroke("pressed DOWN"), "duckOn");
        inputMap.put(KeyStroke.getKeyStroke("released DOWN"), "duckOff");
        inputMap.put(KeyStroke.getKeyStroke("P"), "pause");

        actionMap.put("jumpStart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                engine.jump();
            }
        });
        actionMap.put("jumpRelease", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                engine.releaseJump();
            }
        });
        actionMap.put("duckOn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                engine.setDucking(true);
            }
        });
        actionMap.put("duckOff", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                engine.setDucking(false);
            }
        });
        actionMap.put("pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                togglePause();
            }
        });
    }

    private void togglePause() {
        if (engine.snapshot().gameOver()) {
            return;
        }
        paused = !paused;
        if (paused) {
            timer.stop();
            statusLabel.setText("PAUSED     Press P to continue");
        } else {
            timer.start();
            statusLabel.setText("TAP / HOLD SPACE: JUMP     HOLD DOWN: DUCK     P: PAUSE");
        }
        canvas.repaint();
    }

    private void gameTick() {
        engine.tick();
        GameSnapshot snapshot = engine.snapshot();
        canvas.repaint();
        if (snapshot.gameOver()) {
            timer.stop();
            finishGame(snapshot);
        }
    }

    private void finishGame(GameSnapshot snapshot) {
        againButton.setVisible(true);
        statusLabel.setText("GAME OVER  •  SCORE " + snapshot.score()
                + "  •  Saving score...");
        if (scoreSaving) {
            return;
        }
        scoreSaving = true;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return scoreService.saveScore(player.id(), snapshot.score(),
                        snapshot.distance());
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        statusLabel.setText("GAME OVER  •  SCORE "
                                + snapshot.score() + " saved for "
                                + player.displayName() + ".");
                    } else {
                        statusLabel.setText("GAME OVER  •  Score was not saved.");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Score save interrupted. Try again.");
                } catch (ExecutionException exception) {
                    statusLabel.setText(message(exception));
                }
            }
        }.execute();
    }

    private String message(Exception exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof AppException appException) {
            return "GAME OVER  •  " + appException.getMessage();
        }
        return "GAME OVER  •  Database error; score was not saved.";
    }

    private final class GameCanvas extends JPanel {

        private GameCanvas() {
            setPreferredSize(new Dimension(GameEngine.WORLD_WIDTH,
                    GameEngine.WORLD_HEIGHT));
            setFocusable(true);
            setBorder(BorderFactory.createLineBorder(Theme.SUN, 3));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            double scaleX = getWidth() / (double) GameEngine.WORLD_WIDTH;
            double scaleY = getHeight() / (double) GameEngine.WORLD_HEIGHT;
            g.scale(scaleX, scaleY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);

            GameSnapshot snapshot = engine.snapshot();
            drawBackground(g);
            drawObstacles(g, snapshot);
            drawPlayer(g, snapshot);
            drawHud(g, snapshot);

            if (paused) {
                drawOverlay(g, "PAUSED", "Press P to continue");
            } else if (snapshot.gameOver()) {
                drawOverlay(g, "RUN COMPLETE", "Press PLAY AGAIN or MENU");
            }
            g.dispose();
        }

        private void drawBackground(Graphics2D g) {
            g.setPaint(new GradientPaint(0, 0, Theme.SKY,
                    0, GameEngine.WORLD_HEIGHT, Theme.SKY_LIGHT));
            g.fillRect(0, 0, GameEngine.WORLD_WIDTH,
                    GameEngine.WORLD_HEIGHT);
            g.setColor(new Color(235, 255, 252));
            cloud(g, 90, 84, 170, 28);
            cloud(g, 700, 54, 165, 30);
            g.setColor(Theme.SUN);
            g.fillRect(770, 24, 54, 54);
            g.setColor(Theme.HILL);
            g.fillOval(-120, 225, 620, 250);
            g.fillOval(340, 205, 700, 280);
            g.setColor(Theme.GRASS);
            g.fillRect(0, 300, GameEngine.WORLD_WIDTH, 105);
            drawCampusBuildings(g);
            drawTrees(g);
            drawTrack(g);
        }

        private void drawTrack(Graphics2D g) {
            g.setColor(Theme.TRACK);
            g.fillRect(0, GameEngine.GROUND_Y, GameEngine.WORLD_WIDTH,
                    GameEngine.WORLD_HEIGHT - GameEngine.GROUND_Y);
            g.setColor(Theme.TRACK_LINE);
            for (int y = GameEngine.GROUND_Y + 12; y < GameEngine.WORLD_HEIGHT;
                    y += 40) {
                g.fillRect(0, y, GameEngine.WORLD_WIDTH, 4);
            }

            for (int x = 24; x < GameEngine.WORLD_WIDTH; x += 112) {
                g.setColor(Theme.SUN);
                g.fillRect(x, GameEngine.GROUND_Y + 28, 54, 6);
                g.setColor(Theme.TRACK_LINE);
                g.fillRect(x + 72, GameEngine.GROUND_Y + 70, 28, 5);
            }

            g.setColor(Theme.GRASS);
            g.fillRect(0, GameEngine.WORLD_HEIGHT - 14,
                    GameEngine.WORLD_WIDTH, 14);
            for (int x = 12; x < GameEngine.WORLD_WIDTH; x += 70) {
                g.setColor(Theme.GRASS_DARK);
                g.fillRect(x, GameEngine.WORLD_HEIGHT - 18, 18, 4);
                g.fillRect(x + 24, GameEngine.WORLD_HEIGHT - 11, 12, 3);
            }
        }

        private void drawCampusBuildings(Graphics2D g) {
            g.setColor(new Color(246, 239, 216));
            g.fillRect(610, 245, 150, 62);
            g.setColor(new Color(226, 131, 82));
            g.fillRect(600, 233, 170, 14);
            g.setColor(Theme.NAVY);
            for (int x = 630; x < 750; x += 30) {
                g.fillRect(x, 263, 14, 18);
            }
            g.fillRect(674, 275, 24, 32);
        }

        private void drawTrees(Graphics2D g) {
            tree(g, 70, 322, 1.0);
            tree(g, 250, 327, 0.8);
            tree(g, 500, 322, 1.1);
            tree(g, 870, 320, 1.2);
        }

        private void tree(Graphics2D g, int x, int y, double scale) {
            g.setColor(new Color(116, 77, 46));
            g.fillRect(x, y, (int) (12 * scale), (int) (48 * scale));
            g.setColor(Theme.GRASS_DARK);
            g.fillRect(x - (int) (24 * scale), y - (int) (18 * scale),
                    (int) (60 * scale), (int) (42 * scale));
            g.fillRect(x - (int) (9 * scale), y - (int) (43 * scale),
                    (int) (40 * scale), (int) (50 * scale));
        }

        private void drawObstacles(Graphics2D g, GameSnapshot snapshot) {
            for (ObstacleView obstacle : snapshot.obstacles()) {
                int x = (int) obstacle.x();
                int y = (int) obstacle.y();
                switch (obstacle.type()) {
                    case DESK -> drawDesk(g, x, y, obstacle.width(), obstacle.height());
                    case CHAIR -> drawChair(g, x, y, obstacle.width(), obstacle.height());
                    case STUDENT -> drawStudent(g, x, y, obstacle.width(), obstacle.height());
                    case EQUATION -> drawEquation(g, x, y, obstacle.width(), obstacle.height());
                    case BOOK -> drawBook(g, x, y, obstacle.width(), obstacle.height());
                }
            }
        }

        private void drawDesk(Graphics2D g, int x, int y, int width, int height) {
            g.setColor(Theme.INK);
            g.fillRect(x, y, width, 13);
            g.fillRect(x + 7, y + 11, 8, height - 11);
            g.fillRect(x + width - 15, y + 11, 8, height - 11);
            g.setColor(new Color(157, 91, 48));
            g.fillRect(x + 3, y + 3, width - 6, 7);
            g.setColor(new Color(218, 126, 63));
            g.fillRect(x + 10, y + 17, width - 20, 8);
            g.setColor(Theme.SUN);
            g.fillRect(x + 18, y + 4, 15, 3);
            g.setColor(Theme.CORAL);
            g.fillRect(x + width - 28, y + 4, 10, 3);
        }

        private void drawChair(Graphics2D g, int x, int y, int width, int height) {
            g.setColor(Theme.INK);
            g.fillRect(x + 6, y, width - 8, 11);
            g.fillRect(x + 7, y + 8, 8, height - 8);
            g.fillRect(x + 1, y + height - 15, width - 2, 11);
            g.fillRect(x + 5, y + height - 5, 7, 9);
            g.fillRect(x + width - 13, y + height - 5, 7, 9);
            g.setColor(Theme.CORAL);
            g.fillRect(x + 9, y + 3, width - 14, 5);
            g.fillRect(x + 5, y + height - 12, width - 10, 5);
        }

        private void drawStudent(Graphics2D g, int x, int y, int width,
                                 int height) {
            g.setColor(Theme.INK);
            g.fillRect(x + 11, y, 17, 17);
            g.fillRect(x + 7, y + 17, 25, 26);
            g.setColor(new Color(255, 205, 153));
            g.fillRect(x + 13, y + 3, 14, 13);
            g.setColor(Theme.NAVY);
            g.fillRect(x + 10, y + 19, 19, 21);
            g.setColor(Theme.CORAL);
            g.fillRect(x + 9, y + 40, 8, height - 40);
            g.fillRect(x + 23, y + 40, 8, height - 40);
            g.setColor(Theme.CREAM);
            g.fillRect(x + 1, y + 23, 9, 6);
            g.fillRect(x + 28, y + 28, 12, 6);
        }

        private void drawEquation(Graphics2D g, int x, int y, int width, int height) {
            g.setColor(Theme.INK);
            g.fillRect(x, y, width, height);
            g.setColor(new Color(243, 235, 255));
            g.fillRect(x + 3, y + 3, width - 6, height - 6);
            g.setColor(Theme.PURPLE);
            g.fillRect(x + 7, y + 7, width - 14, 5);
            g.setColor(Theme.CORAL);
            g.fillRect(x + width - 20, y + 6, 8, 8);
            g.setColor(Theme.NAVY);
            g.setFont(Theme.pixelFont(15, true));
            g.drawString("2 + 2 = 4", x + 11, y + 28);
        }

        private void drawBook(Graphics2D g, int x, int y, int width, int height) {
            g.setColor(Theme.NAVY);
            g.fillRect(x, y, width, height);
            g.setColor(Theme.CREAM);
            g.fillRect(x + 4, y + 4, width / 2 - 6, height - 8);
            g.fillRect(x + width / 2 + 2, y + 4, width / 2 - 6, height - 8);
            g.setColor(Theme.SUN);
            g.fillRect(x + width / 2 - 2, y + 3, 4, height - 6);
            g.setColor(Theme.CORAL);
            g.fillRect(x + 10, y + 13, width / 2 - 16, 4);
            g.fillRect(x + width / 2 + 8, y + 13, width / 2 - 16, 4);
            g.setColor(Theme.PURPLE);
            g.fillRect(x + width - 12, y + 4, 5, 18);
        }

        private void drawPlayer(Graphics2D g, GameSnapshot snapshot) {
            int x = 120;
            int y = (int) snapshot.playerY();
            int height = snapshot.ducking() ? 36 : 60;
            g.setColor(new Color(20, 33, 48, 70));
            g.fillRect(x - 4, GameEngine.GROUND_Y + 3, 48, 4);
            g.setColor(new Color(255, 205, 153));
            g.fillRect(x + 13, y, 20, 20);
            g.setColor(Theme.INK);
            g.fillRect(x + 13, y - 2, 20, 5);
            g.setColor(Theme.NAVY);
            g.fillRect(x + 9, y + 19, 28, snapshot.ducking() ? 17 : 27);
            g.setColor(Theme.CORAL);
            if (snapshot.ducking()) {
                g.fillRect(x + 2, y + 25, 21, 8);
                g.fillRect(x + 27, y + 25, 20, 8);
            } else {
                g.fillRect(x + 7, y + 43, 10, height - 43);
                g.fillRect(x + 28, y + 43, 10, 9);
                g.fillRect(x + 34, y + 49, 14, 8);
            }
            g.setColor(Theme.CREAM);
            if (snapshot.ducking()) {
                g.fillRect(x + 1, y + 29, 16, 8);
                g.fillRect(x + 34, y + 23, 12, 8);
            } else {
                g.fillRect(x + 1, y + 24, 12, 8);
                g.fillRect(x + 34, y + 28, 16, 8);
            }
            g.setColor(Theme.INK);
            g.fillRect(x + 27, y + 7, 4, 4);
        }

        private void drawHud(Graphics2D g, GameSnapshot snapshot) {
            g.setColor(Theme.INK);
            g.setFont(Theme.pixelFont(25, true));
            g.drawString(String.format("%04d", snapshot.score()), 28, 42);
            g.setFont(Theme.pixelFont(12, true));
            g.drawString("SCORE", 31, 59);
            g.drawString("SPEED " + snapshot.speed(), 820, 40);
        }

        private void drawOverlay(Graphics2D g, String title, String detail) {
            g.setColor(new Color(20, 33, 48, 185));
            g.fillRect(250, 175, 460, 120);
            g.setColor(Theme.SUN);
            g.setFont(Theme.pixelFont(28, true));
            centerText(g, title, 235);
            g.setColor(Color.WHITE);
            g.setFont(Theme.pixelFont(13, true));
            centerText(g, detail, 267);
        }

        private void centerText(Graphics2D g, String text, int y) {
            FontMetrics metrics = g.getFontMetrics();
            int x = (GameEngine.WORLD_WIDTH - metrics.stringWidth(text)) / 2;
            g.drawString(text, x, y);
        }

        private void cloud(Graphics2D g, int x, int y, int width, int height) {
            g.fillRoundRect(x, y, width, height, height, height);
            g.fillOval(x + width / 4, y - height / 2, width / 3, height * 2);
            g.fillOval(x + width / 2, y - height / 3, width / 3, height * 2);
        }
    }
}
