package com.campusrush.game;

public enum ObstacleType {
    DESK(false, "DESK"),
    CHAIR(false, "CHAIR"),
    STUDENT(false, "STUDENT"),
    EQUATION(true, "EQUATION"),
    BOOK(true, "BOOK");

    private final boolean airborne;
    private final String label;

    ObstacleType(boolean airborne, String label) {
        this.airborne = airborne;
        this.label = label;
    }

    public boolean airborne() {
        return airborne;
    }

    public String label() {
        return label;
    }
}
