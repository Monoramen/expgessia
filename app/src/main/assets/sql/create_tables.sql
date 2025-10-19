CREATE TABLE characteristics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    icon_res_name TEXT
);

CREATE TABLE tasks (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    characteristic_id INTEGER NOT NULL,
    xp_reward INTEGER NOT NULL DEFAULT 0,
    is_completed INTEGER NOT NULL DEFAULT 0,
    scheduled_for INTEGER,
    FOREIGN KEY (characteristic_id) REFERENCES characteristics(id)
);

INSERT INTO characteristics (name, description, icon_res_name) VALUES
('Strength', 'Забота о теле и воле. Прогулка, уборка, спорт.', 'ic_strength'),
('Perception', 'Осознанность и наблюдение. Медитация, внимание к деталям.', 'ic_perception'),
('Endurance', 'Энергия и устойчивость. Сон, паузы, режим.', 'ic_endurance'),
('Charisma', 'Связь с людьми. Разговор, комплимент, улыбка.', 'ic_charisma'),
('Intelligence', 'Любопытство и мышление. Чтение, вопросы, обучение.', 'ic_intelligence'),
('Agility', 'Гибкость и адаптивность. Новые маршруты, перемены.', 'ic_agility'),
('Luck', 'Спонтанность и открытость. Случай, игра, сюрприз.', 'ic_luck');


INSERT INTO tasks (id, title, characteristic_id, xp_reward, is_completed, scheduled_for)
VALUES ('task_001', 'Learn something new', 5, 10, 0, 1739810400000);
