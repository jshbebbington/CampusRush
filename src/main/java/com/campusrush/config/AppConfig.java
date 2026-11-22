package com.campusrush.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record AppConfig(String databaseUrl, String databaseUser,
                        String databasePassword) {

    public static AppConfig load() {
        Properties properties = new Properties();
        Path localFile = Path.of("campus-rush.properties");

        if (Files.exists(localFile)) {
            try (InputStream input = Files.newInputStream(localFile)) {
                properties.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Could not read campus-rush.properties.", exception);
            }
        }

        String url = value(properties, "db.url", "CAMPUS_RUSH_DB_URL",
                "jdbc:oracle:thin:@localhost:1521/xepdb1");
        String user = value(properties, "db.user", "CAMPUS_RUSH_DB_USER",
                "system");
        String password = value(properties, "db.password",
                "CAMPUS_RUSH_DB_PASSWORD", "");

        if (password.isBlank() || password.startsWith("replace-with")) {
            throw new IllegalStateException(
                    "Set db.password in campus-rush.properties before running.");
        }

        return new AppConfig(url, user, password);
    }

    private static String value(Properties properties, String propertyKey,
                                String environmentKey, String fallback) {
        String propertyValue = properties.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        return fallback;
    }
}
