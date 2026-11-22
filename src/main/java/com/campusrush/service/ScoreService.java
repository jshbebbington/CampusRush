package com.campusrush.service;

import com.campusrush.dao.ScoreDao;
import com.campusrush.exception.ValidationException;

public final class ScoreService {

    private final ScoreDao scoreDao;

    public ScoreService(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }

    public boolean saveScore(long playerId, int score, int distance) {
        if (playerId <= 0) {
            throw new ValidationException("A valid player is required.");
        }
        if (score < 0 || distance < 0) {
            throw new ValidationException("Score values cannot be negative.");
        }
        return scoreDao.save(playerId, score, distance);
    }
}
