package com.campusrush.model;

import java.time.Instant;

public record PlayerProfile(long id, String displayName,
                            Instant createdAt, Instant updatedAt) {

    @Override
    public String toString() {
        return displayName;
    }
}
