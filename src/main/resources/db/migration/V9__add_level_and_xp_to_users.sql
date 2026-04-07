ALTER TABLE users RENAME COLUMN total_points TO xp;

ALTER TABLE users ADD COLUMN level INTEGER NOT NULL DEFAULT 1;

ALTER TABLE users RENAME COLUMN points TO shards;

ALTER TABLE activities RENAME COLUMN points_earned TO xp_earned;