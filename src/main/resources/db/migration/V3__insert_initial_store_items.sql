INSERT INTO items (id, name, description, price, type, icon_key, max_quantity, created_at)
VALUES (
    uuid_generate_v4(),
    'Bloqueio de Ofensiva',
    'Protege a sua ofensiva caso esqueça de realizar uma atividade por um dia.',
    100,
    'STREAK_FREEZE',
    'ic_streak_freeze',
    3,
    CURRENT_TIMESTAMP
);

INSERT INTO items (id, name, description, price, type, icon_key, max_quantity, created_at)
VALUES (
    uuid_generate_v4(),
    'Multiplicador de Pontos (24h)',
    'Duplica os pontos ganhos em todas as atividades durante 24 horas.',
    300,
    'XP_BOOST',
    'ic_xp_boost',
    5,
    CURRENT_TIMESTAMP
);