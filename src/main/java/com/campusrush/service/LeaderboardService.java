package com.campusrush.service;

import com.campusrush.dao.ScoreDao;
import com.campusrush.model.ScoreEntry;
import java.util.List;

public final class LeaderboardService {

    private final ScoreDao scoreDao;

    public LeaderboardService(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }

    public List<ScoreEntry> search(String playerName) {
        return scoreDao.findLeaderboard(playerName);
    }
}
