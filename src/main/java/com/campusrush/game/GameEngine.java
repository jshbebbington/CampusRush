package com.campusrush.game;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GameEngine {

    public static final int WORLD_WIDTH = 960;
    public static final int WORLD_HEIGHT = 520;
    public static final int GROUND_Y = 405;

    private static final int PLAYER_X = 120;
    private static final int PLAYER_WIDTH = 36;
    private static final int NORMAL_HEIGHT = 60;
    private static final int DUCK_HEIGHT = 36;

    private final Random random;
    private final List<ObstacleState> obstacles = new ArrayList<>();
    private int score;
    private int distance;
    private double speed;
    private double verticalVelocity;
    private double playerY;
    private double nextSpawnDistance;
    private boolean ducking;
    private boolean jumpHeld;
    private boolean gameOver;

    public GameEngine() {
        random = new Random();
        reset();
    }

    public void reset() {
        score = 0;
        distance = 0;
        speed = 7.0;
        verticalVelocity = 0;
        playerY = GROUND_Y - NORMAL_HEIGHT;
        nextSpawnDistance = 520;
        ducking = false;
        jumpHeld = false;
        gameOver = false;
        obstacles.clear();
    }

    public void jump() {
        if (!gameOver && isOnGround() && !ducking) {
            verticalVelocity = -13.5;
            jumpHeld = true;
        }
    }

    public void releaseJump() {
        if (!gameOver && jumpHeld) {
            if (verticalVelocity < -4.0) {
                verticalVelocity *= 0.45;
            }
            jumpHeld = false;
        }
    }

    public void setDucking(boolean ducking) {
        if (!gameOver) {
            if (ducking && isOnGround()) {
                this.ducking = true;
                playerY = GROUND_Y - DUCK_HEIGHT;
            } else if (!ducking && this.ducking) {
                this.ducking = false;
                playerY = GROUND_Y - NORMAL_HEIGHT;
            }
        }
    }

    public void tick() {
        if (gameOver) {
            return;
        }

        distance += (int) Math.round(speed);
        score = distance / 10;
        speed = Math.min(15.0, 7.0 + score / 250.0);

        double gravity = jumpHeld && verticalVelocity < 0 ? 0.62 : 0.85;
        verticalVelocity += gravity;
        playerY += verticalVelocity;
        int playerHeight = ducking ? DUCK_HEIGHT : NORMAL_HEIGHT;
        double groundPlayerY = GROUND_Y - playerHeight;
        if (playerY >= groundPlayerY) {
            playerY = groundPlayerY;
            verticalVelocity = 0;
            jumpHeld = false;
        }

        for (ObstacleState obstacle : obstacles) {
            obstacle.x -= speed;
        }
        obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);

        if (distance >= nextSpawnDistance) {
            spawnObstacle();
            int gap = 310 + random.nextInt(270);
            nextSpawnDistance = distance + gap;
        }

        if (hasCollision()) {
            gameOver = true;
        }
    }

    public GameSnapshot snapshot() {
        List<ObstacleView> views = new ArrayList<>();
        for (ObstacleState obstacle : obstacles) {
            views.add(new ObstacleView(obstacle.type, obstacle.x, obstacle.y,
                    obstacle.width, obstacle.height));
        }
        int currentSpeed = (int) Math.round(speed);
        return new GameSnapshot(score, distance, currentSpeed, playerY,
                !isOnGround(), ducking, gameOver, List.copyOf(views));
    }

    private void spawnObstacle() {
        ObstacleType[] types = ObstacleType.values();
        ObstacleType type = types[random.nextInt(types.length)];
        int width;
        int height;
        double y;

        switch (type) {
            case DESK -> {
                width = 58;
                height = 50;
                y = GROUND_Y - height;
            }
            case CHAIR -> {
                width = 38;
                height = 58;
                y = GROUND_Y - height;
            }
            case STUDENT -> {
                width = 38;
                height = 64;
                y = GROUND_Y - height;
            }
            case EQUATION -> {
                width = 112;
                height = 38;
                y = GROUND_Y - 79;
            }
            case BOOK -> {
                width = 60;
                height = 42;
                y = GROUND_Y - 85;
            }
            default -> throw new IllegalStateException("Unknown obstacle.");
        }

        obstacles.add(new ObstacleState(type, WORLD_WIDTH + 20, y,
                width, height));
    }

    private boolean hasCollision() {
        int playerHeight = ducking ? DUCK_HEIGHT : NORMAL_HEIGHT;
        Rectangle player = new Rectangle(PLAYER_X + 6,
                (int) playerY + 5, PLAYER_WIDTH - 10, playerHeight - 8);

        for (ObstacleState obstacle : obstacles) {
            Rectangle obstacleBounds = new Rectangle(
                    (int) obstacle.x + 4,
                    (int) obstacle.y + 4,
                    obstacle.width - 8,
                    obstacle.height - 8);
            if (player.intersects(obstacleBounds)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnGround() {
        return playerY >= GROUND_Y - (ducking ? DUCK_HEIGHT : NORMAL_HEIGHT)
                - 0.1;
    }

    private static final class ObstacleState {

        private final ObstacleType type;
        private double x;
        private final double y;
        private final int width;
        private final int height;

        private ObstacleState(ObstacleType type, double x, double y,
                              int width, int height) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
