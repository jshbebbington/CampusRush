package com.campusrush.dao;

import com.campusrush.exception.DataAccessException;
import com.campusrush.model.ScoreEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class ScoreDao {

    private final ConnectionFactory connectionFactory;

    public ScoreDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public boolean save(long playerId, int score, int distance) {
        String sql = "INSERT INTO scores "
                + "(player_id, score_value, distance_value) VALUES (?, ?, ?)";

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, playerId);
            statement.setInt(2, score);
            statement.setInt(3, distance);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not save the score.",
                    exception);
        }
    }

    public List<ScoreEntry> findLeaderboard(String searchText) {
        boolean filtered = searchText != null && !searchText.isBlank();
        String sql = "SELECT score_id, player_id, display_name, "
                + "score_value, distance_value, achieved_at "
                + "FROM ("
                + "SELECT s.score_id, s.player_id, p.display_name, "
                + "s.score_value, s.distance_value, s.achieved_at, "
                + "ROW_NUMBER() OVER (PARTITION BY s.player_id "
                + "ORDER BY s.score_value DESC, s.achieved_at ASC, "
                + "s.score_id ASC) AS player_rank "
                + "FROM scores s JOIN players p "
                + "ON p.player_id = s.player_id "
                + (filtered ? "WHERE LOWER(p.display_name) LIKE ? " : "")
                + ") best_scores "
                + "WHERE player_rank = 1 "
                + "ORDER BY score_value DESC, achieved_at ASC, score_id ASC";
        List<ScoreEntry> scores = new ArrayList<>();

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filtered) {
                statement.setString(1, "%" + searchText.trim().toLowerCase()
                        + "%");
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next() && scores.size() < 100) {
                    scores.add(map(resultSet));
                }
            }
            return scores;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not load the leaderboard.",
                    exception);
        }
    }

    private ScoreEntry map(ResultSet resultSet) throws SQLException {
        Timestamp achieved = resultSet.getTimestamp("achieved_at");
        return new ScoreEntry(
                resultSet.getLong("score_id"),
                resultSet.getLong("player_id"),
                resultSet.getString("display_name"),
                resultSet.getInt("score_value"),
                resultSet.getInt("distance_value"),
                achieved.toInstant());
    }
}
