INSERT INTO players (display_name) VALUES ('Pixel Pat');
INSERT INTO players (display_name) VALUES ('Campus Kai');

INSERT INTO scores (player_id, score_value, distance_value)
SELECT player_id, 420, 1680 FROM players WHERE display_name = 'Pixel Pat';

INSERT INTO scores (player_id, score_value, distance_value)
SELECT player_id, 315, 1260 FROM players WHERE display_name = 'Campus Kai';

COMMIT;

