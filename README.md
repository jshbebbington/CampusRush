# Campus Rush

Campus Rush is a self-contained Java Swing endless runner with a summer 8-bit-inspired campus scene. A player profile is selected before play, the game records the final score through JDBC, and the leaderboard reads persisted scores from Oracle.

Repository: https://github.com/jshbebbington?tab=repositories

The documented verification environment is Oracle Database 21c Express Edition, service `xepdb1`, host `localhost`, port `1521`, and JDK `26.0.2`. The project compiles with Java source release 17 for compatibility with JDK 17 and newer.

## Prerequisites

- JDK 17 or newer
- Maven 3.9 or newer
- Oracle Database 21c Express Edition or the instructor-approved Oracle database
- The `ojdbc11` driver, resolved by Maven from the dependency in `pom.xml`

## First-time setup

1. Create or open an Oracle connection for the approved service, for example `localhost:1521/xepdb1`.
2. Run `schema.sql` once in the application schema.
3. Optionally run `seed_data.sql`.
4. Copy `campus-rush.properties.example` to `campus-rush.properties`.
5. Put the local database password in `campus-rush.properties`. Do not commit or submit that local file.
6. From the project directory, run `mvn clean compile`.

## Run

```text
mvn exec:java
```

The same project can be opened in Apache NetBeans as a Maven project. Run `com.campusrush.App`.

## Main workflows

1. Create or select a profile from **Profiles**.
   Profiles appear in creation order, and the visible list number is separate from Oracle's internal identity ID.
2. Select **Play** from the menu.
3. Tap **Space** or **Up** for a short hop, or hold it longer for a higher jump over desks, chairs, and running students.
4. Hold **Down** to duck under equations and flying books.
5. On game over, wait for the score-save message, then use **Play Again** or **Menu**.
6. Open **Leaderboard** to search, refresh, and sort saved scores.
7. Use **Delete Selected** in Profiles only after confirming the cascade removal of that player's scores.

## Configuration troubleshooting

- `No suitable driver`: confirm Maven downloaded `ojdbc11` or add the instructor-provided JAR to the project classpath.
- `ORA-00942`: run `schema.sql` in the same Oracle service and schema used by the properties file.
- `Database error`: verify the host, port, service name, username, and password in `campus-rush.properties`.
- A profile name must be 2-40 characters and unique. Names containing apostrophes are valid because all SQL values use parameters.

