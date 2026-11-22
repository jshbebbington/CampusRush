package com.campusrush.dao;

import com.campusrush.exception.DataAccessException;
import com.campusrush.model.PlayerProfile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class PlayerDao {

    private final ConnectionFactory connectionFactory;

    public PlayerDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public List<PlayerProfile> findAll() {
        String sql = "SELECT player_id, display_name, created_at, updated_at "
                + "FROM players ORDER BY created_at, player_id";
        List<PlayerProfile> players = new ArrayList<>();

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                players.add(map(resultSet));
            }
            return players;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not load profiles.", exception);
        }
    }

    public PlayerProfile create(String displayName) {
        String sql = "INSERT INTO players (display_name) VALUES (?)";

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(
                     sql, new String[]{"PLAYER_ID"})) {
            statement.setString(1, displayName);
            int affected = statement.executeUpdate();
            if (affected != 1) {
                throw new DataAccessException(
                        "Profile was not saved because no row was changed.",
                        null);
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException(
                            "Profile was saved but its ID was not returned.",
                            null);
                }
                long id = keys.getLong(1);
                return findById(id);
            }
        } catch (SQLException exception) {
            if (isDuplicate(exception)) {
                throw new DataAccessException(
                        "That profile name is already in use.", exception);
            }
            throw new DataAccessException("Could not create the profile.",
                    exception);
        }
    }

    public PlayerProfile update(long id, String displayName) {
        String sql = "UPDATE players SET display_name = ?, "
                + "updated_at = SYSTIMESTAMP WHERE player_id = ?";

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, displayName);
            statement.setLong(2, id);
            int affected = statement.executeUpdate();
            if (affected != 1) {
                throw new DataAccessException(
                        "The selected profile no longer exists.", null);
            }
            return findById(id);
        } catch (SQLException exception) {
            if (isDuplicate(exception)) {
                throw new DataAccessException(
                        "That profile name is already in use.", exception);
            }
            throw new DataAccessException("Could not update the profile.",
                    exception);
        }
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM players WHERE player_id = ?";

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not delete the profile.",
                    exception);
        }
    }

    private PlayerProfile findById(long id) {
        String sql = "SELECT player_id, display_name, created_at, updated_at "
                + "FROM players WHERE player_id = ?";

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException(
                            "The saved profile could not be reloaded.", null);
                }
                return map(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Could not reload the saved profile.", exception);
        }
    }

    private PlayerProfile map(ResultSet resultSet) throws SQLException {
        Timestamp created = resultSet.getTimestamp("created_at");
        Timestamp updated = resultSet.getTimestamp("updated_at");
        return new PlayerProfile(
                resultSet.getLong("player_id"),
                resultSet.getString("display_name"),
                created.toInstant(),
                updated.toInstant());
    }

    private boolean isDuplicate(SQLException exception) {
        return "23000".equals(exception.getSQLState())
                || exception.getErrorCode() == 1;
    }
}
