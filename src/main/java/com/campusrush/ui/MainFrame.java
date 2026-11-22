package com.campusrush.ui;

import com.campusrush.model.PlayerProfile;
import com.campusrush.service.LeaderboardService;
import com.campusrush.service.PlayerService;
import com.campusrush.service.ScoreService;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

public final class MainFrame extends JFrame {

    private static final String MENU = "menu";
    private static final String PROFILES = "profiles";
    private static final String LEADERBOARD = "leaderboard";
    private static final String GAME = "game";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final MainMenuPanel menuPanel;
    private final ProfilePanel profilePanel;
    private final LeaderboardPanel leaderboardPanel;
    private final GamePanel gamePanel;
    private PlayerProfile currentPlayer;

    public MainFrame(PlayerService playerService,
                     ScoreService scoreService,
                     LeaderboardService leaderboardService) {
        setTitle("Campus Rush");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1000, 700);
        setLocationRelativeTo(null);

        menuPanel = new MainMenuPanel(
                this::startGame,
                this::showProfiles,
                this::showLeaderboard,
                this::dispose);
        profilePanel = new ProfilePanel(
                playerService,
                this::selectPlayer,
                this::profileDeleted,
                this::showMenu);
        leaderboardPanel = new LeaderboardPanel(
                leaderboardService,
                this::showMenu);
        gamePanel = new GamePanel(scoreService, this::showMenu);

        cards.add(menuPanel, MENU);
        cards.add(profilePanel, PROFILES);
        cards.add(leaderboardPanel, LEADERBOARD);
        cards.add(gamePanel, GAME);
        add(cards);
        showMenu();
    }

    private void startGame() {
        if (currentPlayer == null) {
            showProfiles();
            profilePanel.showMessage("Create or select a profile before playing.");
            return;
        }
        gamePanel.start(currentPlayer);
        cardLayout.show(cards, GAME);
    }

    private void showProfiles() {
        profilePanel.refresh();
        cardLayout.show(cards, PROFILES);
    }

    private void showLeaderboard() {
        leaderboardPanel.refresh();
        cardLayout.show(cards, LEADERBOARD);
    }

    private void showMenu() {
        menuPanel.setCurrentPlayer(currentPlayer);
        cardLayout.show(cards, MENU);
    }

    private void selectPlayer(PlayerProfile player) {
        currentPlayer = player;
        menuPanel.setCurrentPlayer(player);
        showMenu();
    }

    private void profileDeleted(PlayerProfile deletedPlayer) {
        if (currentPlayer != null
                && currentPlayer.id() == deletedPlayer.id()) {
            currentPlayer = null;
            menuPanel.setCurrentPlayer(null);
        }
    }
}
