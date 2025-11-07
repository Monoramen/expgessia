CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL DEFAULT 'Player',
    experience INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    money INTEGER NOT NULL DEFAULT 0,
    last_login INTEGER,
    photo_uri TEXT
);

CREATE TABLE IF NOT EXISTS characteristics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    icon_res_name TEXT
);

CREATE TABLE IF NOT EXISTS user_characteristics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    characteristic_id INTEGER NOT NULL,
    value INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (characteristic_id) REFERENCES characteristics (id) ON DELETE CASCADE,
    UNIQUE(user_id, characteristic_id)
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

CREATE TABLE IF NOT EXISTS task_instances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    scheduled_for INTEGER,
    is_completed INTEGER NOT NULL DEFAULT 0,
    completed_at INTEGER,
    xp_earned INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE(task_id, scheduled_for)
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

INSERT OR IGNORE INTO users (id, name, experience, level, money, last_login, photo_uri)
VALUES (1, 'Player', 0, 1, 0, NULL, NULL);

INSERT OR IGNORE INTO user_characteristics (user_id, characteristic_id, value) VALUES
(1, 1, 1), (1, 2, 1), (1, 3, 1), (1, 4, 1), (1, 5, 1), (1, 6, 1), (1, 7, 1);

INSERT OR IGNORE INTO tasks (title, description, characteristic_id, repeat_mode, repeat_details, xp_reward) VALUES
-- Daily tasks
('Morning Workout', 'Do 10 push-ups, 15 squats, or 30-second plank.', 1, 'DAILY', NULL, 15),
('Micro Observations', 'Write down 5 small details in your environment that you usually miss.', 2, 'DAILY', NULL, 10),
('Sleep Schedule', 'Go to bed before 11 PM and wake up with alarm.', 3, 'DAILY', NULL, 20),
('Active Listening', 'Have one conversation without interrupting and asking clarifying questions.', 4, 'DAILY', NULL, 15),
('10 Pages Reading', 'Read 10 pages of a book or article on a new topic.', 5, 'DAILY', NULL, 15),
('Hand Exercise', 'Do hand coordination exercise (juggling, complex knot, etc.).', 6, 'DAILY', NULL, 10),

-- Saturday cleaning task
('Weekly Cleaning', 'Clean your room, organize workspace, or general household cleaning.', 1, 'WEEKLY', 6, 25);