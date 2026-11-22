package com.campusrush;

import com.campusrush.config.AppConfig;
import com.campusrush.dao.ConnectionFactory;
import com.campusrush.dao.PlayerDao;
import com.campusrush.dao.ScoreDao;
import com.campusrush.service.LeaderboardService;
import com.campusrush.service.PlayerService;
import com.campusrush.service.ScoreService;
import com.campusrush.ui.MainFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AppConfig config = AppConfig.load();
                ConnectionFactory connectionFactory =
                        new ConnectionFactory(config);
                PlayerService playerService = new PlayerService(
                        new PlayerDao(connectionFactory));
                ScoreService scoreService = new ScoreService(
                        new ScoreDao(connectionFactory));
                LeaderboardService leaderboardService =
                        new LeaderboardService(new ScoreDao(connectionFactory));

                MainFrame frame = new MainFrame(
                        playerService, scoreService, leaderboardService);
                frame.setVisible(true);
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(
                        null,
                        exception.getMessage(),
                        "Campus Rush could not start",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
