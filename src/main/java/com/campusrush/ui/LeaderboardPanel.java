package com.campusrush.ui;

import com.campusrush.exception.AppException;
import com.campusrush.model.ScoreEntry;
import com.campusrush.service.LeaderboardService;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class LeaderboardPanel extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());

    private final LeaderboardService leaderboardService;
    private final Runnable onBack;
    private final JTextField searchField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"RANK", "PLAYER", "SCORE", "DISTANCE", "DATE"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public LeaderboardPanel(LeaderboardService leaderboardService,
                            Runnable onBack) {
        this.leaderboardService = leaderboardService;
        this.onBack = onBack;
        buildUi();
    }

    public void refresh() {
        statusLabel.setText("Loading leaderboard...");
        String search = searchField.getText();
        new SwingWorker<List<ScoreEntry>, Void>() {
            @Override
            protected List<ScoreEntry> doInBackground() {
                return leaderboardService.search(search);
            }

            @Override
            protected void done() {
                try {
                    List<ScoreEntry> entries = get();
                    tableModel.setRowCount(0);
                    int rank = 1;
                    for (ScoreEntry entry : entries) {
                        tableModel.addRow(new Object[]{
                            rank++, entry.playerName(), entry.score(),
                            entry.distance(), TIME_FORMAT.format(entry.achievedAt())
                        });
                    }
                    statusLabel.setText(entries.size()
                            + " score(s) loaded. Click a column to sort.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading was interrupted.");
                } catch (ExecutionException exception) {
                    statusLabel.setText(message(exception));
                }
            }
        }.execute();
    }

    private void buildUi() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(26, 32, 26, 32));

        JLabel heading = new JLabel("LEADERBOARD");
        heading.setFont(Theme.pixelFont(24, true));
        heading.setForeground(Theme.NAVY);
        add(heading, BorderLayout.NORTH);

        table.setFont(Theme.pixelFont(13, false));
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.setRowSorter(new TableRowSorter<>(tableModel));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setOpaque(false);
        JPanel search = new JPanel(new GridLayout(1, 3, 8, 0));
        search.setOpaque(false);
        search.add(new JLabel("Search player:"));
        search.add(searchField);
        JButton refreshButton = new JButton("SEARCH / REFRESH");
        Theme.styleButton(refreshButton, Theme.SKY_LIGHT, Theme.NAVY);
        refreshButton.addActionListener(event -> refresh());
        search.add(refreshButton);
        bottom.add(search, BorderLayout.NORTH);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        statusLabel.setFont(Theme.pixelFont(12, false));
        actions.add(statusLabel, BorderLayout.CENTER);
        JButton back = new JButton("BACK TO MENU");
        Theme.styleButton(back, Theme.SKY_LIGHT, Theme.NAVY);
        back.addActionListener(event -> onBack.run());
        actions.add(back, BorderLayout.EAST);
        bottom.add(actions, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    private String message(Exception exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof AppException appException) {
            return appException.getMessage();
        }
        return "Database error. Check the connection and try again.";
    }
}
