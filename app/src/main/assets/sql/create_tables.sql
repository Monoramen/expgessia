CREATE TABLE IF NOT EXISTS users (
id INTEGER PRIMARY KEY,
name TEXT NOT NULL DEFAULT 'Player',
experience INTEGER NOT NULL DEFAULT 0,
level INTEGER NOT NULL DEFAULT 1,
money INTEGER NOT NULL DEFAULT 0,    -- ИЗМЕНЕНО
strength INTEGER NOT NULL DEFAULT 5,
perception INTEGER NOT NULL DEFAULT 5,
endurance INTEGER NOT NULL DEFAULT 5, -- Добавлено, если не было
charisma INTEGER NOT NULL DEFAULT 5,
intelligence INTEGER NOT NULL DEFAULT 5,
agility INTEGER NOT NULL DEFAULT 5,
luck INTEGER NOT NULL DEFAULT 5,
last_login INTEGER,
photo_uri TEXT
);

CREATE TABLE IF NOT EXISTS characteristics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    icon_res_name TEXT
);

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    characteristic_id INTEGER NOT NULL,
    repeat_mode TEXT NOT NULL DEFAULT 'NONE',
    repeat_details TEXT,
    xp_reward INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (characteristic_id) REFERENCES characteristics(id)
);

-- 💡 НОВАЯ ТАБЛИЦА: task_instances
CREATE TABLE IF NOT EXISTS task_instances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    scheduled_for INTEGER, -- Начало дня, на который запланирована задача
    is_completed INTEGER NOT NULL DEFAULT 0, -- 0 (false) или 1 (true)
    completed_at INTEGER,
    xp_earned INTEGER NOT NULL DEFAULT 0,
    is_undone INTEGER NOT NULL DEFAULT 0, -- Флаг отмены выполнения (для отмены прогресса)

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE(task_id, scheduled_for) -- Гарантирует только один экземпляр задачи на один день
);

CREATE TABLE IF NOT EXISTS daily_stats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date INTEGER NOT NULL UNIQUE,
    xp_earned_today INTEGER NOT NULL DEFAULT 0
);


CREATE TABLE IF NOT EXISTS task_completions (
    id INTEGER PRIMARY KEY,
    task_id INTEGER NOT NULL,
    completion_date INTEGER NOT NULL,
    xp_earned INTEGER NOT NULL,
    characteristic_id INTEGER NOT NULL,
    is_repeating INTEGER NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (characteristic_id) REFERENCES characteristics(id)
);


INSERT OR IGNORE INTO characteristics (name, description, icon_res_name) VALUES
('Strength', 'Care of body and will. Walking, cleaning, sports.', 'strength'),
('Perception', 'Awareness and observation. Meditation, attention to detail.', 'perception'),
('Endurance', 'Health and resilience. Sleep, healthy food, habits.', 'endurance'),
('Charisma', 'Social and communication skills. Meetings, conversations, public speaking.', 'charisma'),
('Intelligence', 'Learning and analytical thinking. Reading, puzzles, new skills.', 'intelligence'),
('Agility', 'Movement and coordination. Dexterity, fast actions, reaction time.', 'agility'),
('Luck', 'Random events and risk management. Only luck.', 'luck');

INSERT OR IGNORE INTO users (id, name, experience, level, money, strength, perception, endurance, charisma, intelligence, agility, luck, last_login, photo_uri)
VALUES ( 1, 'Player', 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, NULL, NULL);

INSERT OR IGNORE INTO tasks (title, description, characteristic_id, repeat_mode, repeat_details, xp_reward)
VALUES
-- 1. Strength (Сила): ID=1
('Утренняя зарядка', 'Сделать 10 отжиманий, 15 приседаний или планку 30 секунд.', 1, 'DAILY', NULL, 15),

-- 2. Perception (Восприятие): ID=2
('Микрозаметки', 'Записать 5 мелких деталей своего окружения, которые обычно не замечаешь.', 2, 'DAILY', NULL, 10),

('Беззвучное наблюдение', 'Провести 5 минут, фокусируясь только на звуках.', 2, 'DAILY', NULL, 10),


-- 3. Endurance (Выносливость): ID=3
('Режим сна', 'Лечь спать до 23:00 и проснуться по будильнику.', 3, 'DAILY', NULL, 20),


-- 4. Charisma (Харизма): ID=4
('Активное слушание', 'Провести один разговор, не перебивая собеседника и задавая уточняющие вопросы.', 4, 'DAILY', NULL, 15),


-- 5. Intelligence (Интеллект): ID=5
('10 страниц книги', 'Прочитать 10 страниц книги или статьи на новую тему.', 5, 'DAILY', NULL, 15),


-- 6. Agility (Ловкость): ID=6
('Упражнение для рук', 'Сделать упражнение для развития мелкой моторики (например, жонглирование или сложный узел).', 6, 'DAILY', NULL, 10);
