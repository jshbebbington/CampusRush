package com.campusrush.model;

import java.time.Instant;

public record ScoreEntry(long id, long playerId, String playerName,
                         int score, int distance, Instant achievedAt) {
}
