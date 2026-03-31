DROP TABLE IF EXISTS user_item;

ALTER TABLE items
    ADD COLUMN description TEXT NOT NULL DEFAULT 'No description',
    ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'COSMETIC',
    ADD COLUMN max_quantity INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE items RENAME COLUMN icon TO icon_key;

-- Remove os valores DEFAULT temporários (usados apenas para o Postgres aceitar o ALTER TABLE)
ALTER TABLE items ALTER COLUMN description DROP DEFAULT;
ALTER TABLE items ALTER COLUMN type DROP DEFAULT;
ALTER TABLE items ALTER COLUMN max_quantity DROP DEFAULT;

CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    item_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_inventory_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    -- Garante que o usuário não tenha duas rows do mesmo item
    CONSTRAINT uq_inventory_user_item UNIQUE (user_id, item_id)
);