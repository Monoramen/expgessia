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
    is_completed INTEGER NOT NULL DEFAULT 0,
    scheduled_for INTEGER,
    FOREIGN KEY (characteristic_id) REFERENCES characteristics(id)
);

INSERT OR IGNORE INTO characteristics (name, description, icon_res_name) VALUES
('Strength', 'Care of body and will. Walking, cleaning, sports.', 'strength'),
('Perception', 'Awareness and observation. Meditation, attention to detail.', 'perception'),
('Endurance', 'Energy and resilience. Sleep, breaks, routine.', 'endurance'),
('Charisma', 'Connection with people. Conversation, compliments, smile.', 'charisma'),
('Intelligence', 'Curiosity and thinking. Reading, questioning, learning.', 'intelligence'),
('Agility', 'Flexibility and adaptability. New routes, changes.', 'agility'),
('Luck', 'Spontaneity and openness. Coincidence, game, surprise.', 'luck');


INSERT OR IGNORE INTO users (id, name, experience, level, money, strength, perception,
endurance, charisma, intelligence, agility, luck, last_login, photo_uri)
VALUES ( 1, 'Player', 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, NULL, NULL);

INSERT OR IGNORE INTO tasks (title, description, characteristic_id, repeat_mode, repeat_details, xp_reward, is_completed, scheduled_for)
VALUES
-- 1. Strength (Сила): ID=1
('Утренняя зарядка', 'Сделать 10 отжиманий, 15 приседаний или планку 30 секунд.', 1, 'DAILY', NULL, 15, 0, NULL),
('Поднять вес', 'Поднять и перенести тяжелый предмет 5 раз (пакеты, мебель).', 1, 'NONE', NULL, 15, 0, NULL),
('Час активности', 'Провести 60 минут в активной ходьбе, беге или уборке.', 1, 'NONE', NULL, 20, 0, NULL),

-- 2. Perception (Восприятие): ID=2
('Микрозаметки', 'Записать 5 мелких деталей своего окружения, которые обычно не замечаешь.', 2, 'DAILY', NULL, 10, 0, NULL),
('Беззвучное наблюдение', 'Провести 5 минут, фокусируясь только на звуках.', 2, 'NONE', NULL, 10, 0, NULL),
('Поиск ошибок', 'Проверить текст или код на наличие 3-5 ошибок (своих или чужих).', 2, 'NONE', NULL, 15, 0, NULL),

-- 3. Endurance (Выносливость): ID=3
('Режим сна', 'Лечь спать до 23:00 и проснуться по будильнику.', 3, 'DAILY', NULL, 20, 0, NULL),
('Пауза для тела', 'Сделать 10-минутный перерыв для растяжки или гимнастики.', 3, 'DAILY', NULL, 15, 0, NULL),
('Пить воду', 'Выпить 2 литра чистой воды в течение дня.', 3, 'DAILY', NULL, 10, 0, NULL),

-- 4. Charisma (Харизма): ID=4
('Комплимент дня', 'Сделать искренний комплимент коллеге, другу или члену семьи.', 4, 'DAILY', NULL, 10, 0, NULL),
('Сетевой звонок', 'Позвонить или написать старому знакомому, чтобы узнать, как дела.', 4, 'NONE', NULL, 15, 0, NULL),
('Активное слушание', 'Провести один разговор, не перебивая собеседника и задавая уточняющие вопросы.', 4, 'NONE', NULL, 15, 0, NULL),

-- 5. Intelligence (Интеллект): ID=5
('Обучение (30м)', 'Изучить новую тему/главу книги в течение 30 минут.', 5, 'DAILY', NULL, 20, 0, NULL),
('Решение головоломки', 'Решить кроссворд, судоку или логическую задачу.', 5, 'NONE', NULL, 15, 0, NULL),
('Конспект', 'Сделать краткий конспект по просмотренному видео/прочитанной статье.', 5, 'NONE', NULL, 15, 0, NULL),

-- 6. Agility (Ловкость): ID=6
('Новая еда', 'Попробовать блюдо, которое никогда раньше не ел.', 6, 'NONE', NULL, 10, 0, NULL),
('Другой маршрут', 'Намеренно изменить маршрут на работу/домой.', 6, 'NONE', NULL, 10, 0, NULL),
('Перестановка', 'Изменить расположение 3-5 предметов на столе или в комнате.', 6, 'NONE', NULL, 10, 0, NULL),

-- 7. Luck (Удача): ID=7
('Спонтанная покупка', 'Купить что-то, что бросилось в глаза, не раздумывая (не дороже 500 руб).', 7, 'NONE', NULL, 10, 0, NULL),
('Бросить монетку', 'Принять неважное решение с помощью жребия (например, что поесть).', 7, 'NONE', NULL, 10, 0, NULL),
('Начать отложенное', 'Сделать первый шаг в деле, которое долго откладывал (15 минут).', 7, 'NONE', NULL, 15, 0, NULL);