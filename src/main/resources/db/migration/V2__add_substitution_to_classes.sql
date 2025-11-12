-- ===========================================
-- V2: Wersja dodająca obsługę zastępstw w tabeli classes i dodająca dane demonstracyjne
-- - dodanie kolumny substituted_for_id do tabeli classes
-- - dodanie przykładowych danych dla instruktorów, sal i typów zajęć
-- ==========================================

ALTER TABLE classes
    ADD COLUMN substituted_for_id BIGINT REFERENCES instructors(id);

-- Ustawienie domyślnych wartości dla kolumny difficulty oraz default_duration w tabeli class_types, by uniknąć problemów przy migracji
UPDATE class_types
SET difficulty = 'ALL_LEVELS'
WHERE difficulty IS NULL
   OR difficulty NOT IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ALL_LEVELS');

UPDATE class_types
SET default_duration_minutes = 55
WHERE default_duration_minutes IS NULL
   OR default_duration_minutes <= 0;

-- Zdjęcie starych checków
ALTER TABLE class_types
DROP CONSTRAINT IF EXISTS class_types_default_duration_minutes_check;

-- Założenie checka na typy zajęć
ALTER TABLE class_types
    ADD CONSTRAINT class_types_difficulty_check
        CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ALL_LEVELS'));

ALTER TABLE class_types
    ADD CONSTRAINT class_types_default_duration_check
        CHECK (default_duration_minutes > 0);

-- Wymuszenie NOT NULL na kolumnach, gdzie powinno to być ustawione, a nie jest
ALTER TABLE class_types
    ALTER COLUMN difficulty SET NOT NULL;

ALTER TABLE class_types
    ALTER COLUMN default_duration_minutes SET NOT NULL;


-- Przykładowi instruktorzy
INSERT INTO instructors (first_name, last_name, email, phone, bio)
VALUES
    ('Anna', 'Kowalska', 'anna.kowalska@example.com', '+48 600 111 222', 'Instruktorka jogi i pilatesu, 5 lat doświadczenia.'),
    ('Marek', 'Nowak', 'marek.nowak@example.com', '+48 501 222 333', 'Specjalista od treningu siłowego i funkcjonalnego.'),
    ('Julia', 'Wiśniewska', 'julia.wisniewska@example.com', NULL, 'Prowadzi zajęcia taneczne i mobility.'),
    ('Tomasz', 'Lis', 'tomasz.lis@example.com', '+48 503 777 888', 'Były zawodnik, obecnie trener personalny.'),
    ('Ewa', 'Zielińska', 'ewa.zielinska@example.com', '+48 509 444 555', 'Instruktorka pilatesu, nastawiona na pracę indywidualną.')
    ON CONFLICT (email) DO NOTHING;


-- Przykładowe sale
INSERT INTO rooms (name, location, capacity)
VALUES
    ('Sala Główna', 'Parter, po lewej', 25),
    ('Studio Joga', '1 piętro, sala 101', 15),
    ('Sala Taneczna', '1 piętro, sala 103', 20),
    ('Siłownia Mała', 'Piwnica', 10),
    ('Sala Funkcjonalna', 'Parter, za recepcją', 18)
    ON CONFLICT (name) DO NOTHING;


-- Przykładowe typy zajęć
INSERT INTO class_types (name, description, default_duration_minutes, difficulty)
VALUES
    ('Joga', 'Zajęcia rozciągająco-relaksacyjne.', 60, 'BEGINNER'),
    ('Trening siłowy', 'Trening z obciążeniem dla całego ciała.', 50, 'INTERMEDIATE'),
    ('Zumba', 'Dynamiczne zajęcia taneczno-kondycyjne.', 45, 'INTERMEDIATE'),
    ('Pilates', 'Wzmacnianie mięśni głębokich i poprawa postawy.', 55, 'BEGINNER'),
    ('HIIT', 'Interwały o wysokiej intensywności.', 30, 'ADVANCED')
    ON CONFLICT (name) DO NOTHING;
