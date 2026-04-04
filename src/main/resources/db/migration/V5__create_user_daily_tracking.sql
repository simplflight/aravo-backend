CREATE TABLE user_daily_tracking (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    tracking_date DATE NOT NULL,
    activities_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    CONSTRAINT fk_tracking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_tracking_date UNIQUE (user_id, tracking_date)
);

CREATE INDEX idx_user_tracking_date ON user_daily_tracking(user_id, tracking_date);

ALTER TABLE user_daily_tracking ALTER COLUMN activities_count DROP DEFAULT;
ALTER TABLE user_daily_tracking ALTER COLUMN status DROP DEFAULT;