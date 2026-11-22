# Campus Rush Architecture

## Packages

```text
com.campusrush
├── App                         application composition and launch
├── config                      local, non-committed database configuration
├── exception                   validation and database exceptions
├── model                       immutable player and score records
├── dao                         JDBC connections and parameterized SQL
├── service                     validation and business rules
├── game                        pure endless-runner state and collision rules
└── ui                          Swing panels, navigation, and Java2D rendering
```

## Data flow

```text
Swing listener
    -> SwingWorker
    -> service validation/business rule
    -> DAO PreparedStatement
    -> Oracle players/scores tables
    -> model/result
    -> SwingWorker.done()
    -> Swing view/status/table
```

## Main design decisions

- `GameEngine` contains movement, obstacle generation, score, collision, and state transitions without database or Swing dependencies.
- `SwingWorker` keeps profile, leaderboard, and score-save JDBC work away from the Event Dispatch Thread.
- DAOs own SQL and try-with-resources; services own validation and domain messages.
- Records provide immutable model values; lists provide typed collection boundaries.
- A local ignored properties file or environment variables keeps credentials out of source control.

