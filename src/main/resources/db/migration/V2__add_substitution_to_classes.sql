-- ===========================================
-- V2: Wersja dodająca obsługę zastępstw w tabeli classes i dodająca dane demonstracyjne
-- - dodanie kolumny substituted_for_id do tabeli classes
-- - dodanie przykładowych danych dla instruktorów, sal i typów zajęć
-- ==========================================

ALTER TABLE classes
    ADD COLUMN substituted_for_id BIGINT REFERENCES instructors(id);

-- instructors
INSERT INTO instructors (first_name, last_name, email, phone, bio)
VALUES
    ('Anna', 'Kowalska', 'anna.kowalska@example.com', '+48 600 111 222', 'Instruktorka jogi i pilatesu, 5 lat doświadczenia.'),
    ('Marek', 'Nowak', 'marek.nowak@example.com', '+48 501 222 333', 'Specjalista od treningu siłowego i funkcjonalnego.'),
    ('Julia', 'Wiśniewska', 'julia.wisniewska@example.com', NULL, 'Prowadzi zajęcia taneczne i mobility.'),
    ('Tomasz', 'Lis', 'tomasz.lis@example.com', '+48 503 777 888', 'Były zawodnik, obecnie trener personalny.'),
    ('Ewa', 'Zielińska', 'ewa.zielinska@example.com', '+48 509 444 555', 'Instruktorka pilatesu, nastawiona na pracę indywidualną.')
    ON CONFLICT (email) DO NOTHING;


-- rooms
INSERT INTO rooms (name, location, capacity)
VALUES
    ('Sala Główna', 'Parter, po lewej', 25),
    ('Studio Joga', '1 piętro, sala 101', 15),
    ('Sala Taneczna', '1 piętro, sala 103', 20),
    ('Siłownia Mała', 'Piwnica', 10),
    ('Sala Funkcjonalna', 'Parter, za recepcją', 18)
    ON CONFLICT (name) DO NOTHING;


-- class_types
INSERT INTO class_types (name, description, default_duration_minutes, difficulty)
VALUES
    ('Joga', 'Zajęcia rozciągająco-relaksacyjne.', 60, 'easy'),
    ('Trening siłowy', 'Trening z obciążeniem dla całego ciała.', 50, 'medium'),
    ('Zumba', 'Dynamiczne zajęcia taneczno-kondycyjne.', 45, 'medium'),
    ('Pilates', 'Wzmacnianie mięśni głębokich i poprawa postawy.', 55, 'easy'),
    ('HIIT', 'Interwały o wysokiej intensywności.', 30, 'hard')
    ON CONFLICT (name) DO NOTHING;
