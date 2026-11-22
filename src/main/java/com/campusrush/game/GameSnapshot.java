package com.campusrush.game;

import java.util.List;

public record GameSnapshot(int score, int distance, int speed,
                           double playerY, boolean jumping, boolean ducking,
                           boolean gameOver, List<ObstacleView> obstacles) {
}
