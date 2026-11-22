# Campus Rush Traceability Matrix

| Final feature | Originating TSA | Implementation location | Tests | Evidence |
|---|---|---|---|---|
| Profile create/read/update/delete | TSA1 domain/model and CRUD rules | `PlayerService`; `PlayerDao`; `ProfilePanel` | T02-T07 | E02-E07 |
| Unique profile names | TSA1 validation/integrity rules | `schema.sql` unique constraint; `PlayerService`; `PlayerDao` | T03, T05 | E03, E05 |
| Jump and ground-obstacle collision | TSA2 game rules | `GameEngine.jump`, `GameEngine.hasCollision` | T08 | E08 |
| Duck and overhead-obstacle rule | TSA2 game rules | `GameEngine.setDucking`, `GameEngine.hasCollision` | T09 | E09 |
| Variable jump height | TSA2 game rules | `GamePanel` jump bindings; `GameEngine.jump`, `GameEngine.releaseJump` | T18 | E18 |
| Score and distance progression | TSA2 scoring rules | `GameEngine.tick`, `ScoreService.saveScore` | T08, T11 | E08, E11 |
| Score persistence | TSA3 service/DAO design | `ScoreDao.save`; `schema.sql` | T11-T12 | E11-E12 |
| Searchable leaderboard | TSA3 reporting requirement | `LeaderboardService.search`; `ScoreDao.findLeaderboard`; `LeaderboardPanel.refresh` | T13-T14 | E13-E14 |
| Responsive database operations | TSA4 Swing/JDBC integration | `SwingWorker` in `ProfilePanel`, `LeaderboardPanel`, `GamePanel` | T15-T16 | E15-E16 |
| Secure configuration | TSA4 reliability/security rule | `AppConfig`; ignored properties file | T01, T15 | E01, E15 |

