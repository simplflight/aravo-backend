-- Habilita UUIDs nativos no PostgreSQL (para versões menores que a 13)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    points INTEGER NOT NULL DEFAULT 0,
    total_points INTEGER NOT NULL DEFAULT 0,
    streak INTEGER NOT NULL DEFAULT 0,
    highest_streak INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    focus_preference INTEGER NOT NULL DEFAULT 25,
    rest_preference INTEGER NOT NULL DEFAULT 5,
    last_activity_date DATE
);

CREATE TABLE items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL,
    icon VARCHAR(255) NOT NULL
);

CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty INTEGER NOT NULL,
    category VARCHAR(100) NOT NULL,
    points INTEGER NOT NULL,
    focus_time INTEGER NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_activity FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabela associativa: N:N entre users e items (inventário do usuário)
CREATE TABLE user_item (
    user_id UUID NOT NULL,
    item_id UUID NOT NULL,
    PRIMARY KEY (user_id, item_id),
    CONSTRAINT fk_ui_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ui_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);