CREATE TABLE campaigns (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    bonus DOUBLE PRECISION NOT NULL,
    category VARCHAR(50),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);