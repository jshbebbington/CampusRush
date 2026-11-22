package com.campusrush.service;

import com.campusrush.dao.PlayerDao;
import com.campusrush.exception.ValidationException;
import com.campusrush.model.PlayerProfile;
import java.util.List;

public final class PlayerService {

    private final PlayerDao playerDao;

    public PlayerService(PlayerDao playerDao) {
        this.playerDao = playerDao;
    }

    public List<PlayerProfile> listProfiles() {
        return playerDao.findAll();
    }

    public PlayerProfile createProfile(String name) {
        return playerDao.create(validName(name));
    }

    public PlayerProfile updateProfile(long id, String name) {
        if (id <= 0) {
            throw new ValidationException("Select a valid profile first.");
        }
        return playerDao.update(id, validName(name));
    }

    public boolean deleteProfile(long id) {
        if (id <= 0) {
            throw new ValidationException("Select a profile to delete.");
        }
        return playerDao.delete(id);
    }

    private String validName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.length() < 2 || name.length() > 40) {
            throw new ValidationException(
                    "Profile name must contain 2 to 40 characters.");
        }
        return name;
    }
}
