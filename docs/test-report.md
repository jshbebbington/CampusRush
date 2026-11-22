# Campus Rush Test Report

The tests below are the acceptance suite for release 1.0.0. Evidence labels refer to screenshots or console/log captures that should be collected during the local run.

| ID | Scenario | Expected result | Actual result | Evidence |
|---|---|---|---|---|
| T01 | Run `schema.sql` on a clean schema | Players and scores tables, keys, checks, and indexes are created | Record after local run | E01 |
| T02 | Create profile `Runner One` | One player row is created and appears in Profiles | Record after local run | E02 |
| T03 | Create duplicate profile `Runner One` | Friendly duplicate-name message; no second row | Record after local run | E03 |
| T04 | Create a one-character profile | Validation rejects the name before JDBC | Record after local run | E04 |
| T05 | Create a name containing an apostrophe | Profile is saved safely through parameters | Record after local run | E05 |
| T06 | Update a selected profile | Name changes and refreshed list shows committed value | Record after local run | E06 |
| T07 | Delete a selected profile after confirmation | Profile and cascaded scores are removed | Record after local run | E07 |
| T08 | Play and collide with a ground obstacle | Game ends and a score-save worker begins | Record after local run | E08 |
| T09 | Play and duck below an equation or book | Player remains alive when timing is correct | Record after local run | E09 |
| T10 | Pause and resume with `P` | Timer stops, overlay appears, and movement resumes | Record after local run | E10 |
| T11 | Finish a run while database is available | Score row is committed and success status is shown | Record after local run | E11 |
| T12 | Restart the application after T11 | Saved score remains in Leaderboard | Record after local run | E12 |
| T13 | Search a leaderboard name fragment | Only matching players are returned | Record after local run | E13 |
| T14 | Click leaderboard columns | Rows sort through the table sorter | Record after local run | E14 |
| T15 | Stop or misconfigure the database and refresh | Actionable database message appears; UI remains usable | Record after local run | E15 |
| T16 | Repeat profile load and leaderboard refresh | No duplicate UI rows or stale result state | Record after local run | E16 |
| T17 | Enter a blank search string | Full leaderboard returns, with no SQL concatenation | Record after local run | E17 |
| T18 | Tap and hold jump for different durations | A short tap produces a lower hop than a longer hold | Record after local run | E18 |

## Pass/fail summary

Complete the Actual result and evidence columns after running the clean setup and acceptance suite. The implementation is designed for all listed tests; the only environment-dependent items are Oracle availability, driver resolution, and the locally supplied credentials.

## Known limitations

- The application does not ship a database password.
- The local evidence screenshots must be captured on the machine where Oracle and the final release are run.
- The project intentionally uses Java2D shapes for the pixel art so no external image asset is required.

