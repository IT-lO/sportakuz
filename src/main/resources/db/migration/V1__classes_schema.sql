-- ===========================================
-- V1: Wersja tworząca część bazy danych związaną z zajęciami dla PostgreSQL 16.10
-- - cykliczne zajęcia przez tabelę class_series
-- - rezerwacje na KONKRETNE wystąpienia (classes) - lub pojedyncze zajęcia
-- ===========================================

CREATE TABLE instructors (
                             id BIGSERIAL PRIMARY KEY,
                             first_name VARCHAR(100) NOT NULL,
                             last_name  VARCHAR(100) NOT NULL,
                             email      VARCHAR(255) NOT NULL UNIQUE,
                             phone      VARCHAR(40),
                             bio        TEXT,
                             active     BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE rooms (
                       id BIGSERIAL PRIMARY KEY,
                       name       VARCHAR(120) NOT NULL UNIQUE,
                       location   VARCHAR(255),
                       capacity   INTEGER NOT NULL CHECK (capacity > 0),
                       active     BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE class_types (
                             id BIGSERIAL PRIMARY KEY,
                             name        VARCHAR(120) NOT NULL UNIQUE,
                             description TEXT,
                             default_duration_minutes INTEGER CHECK (default_duration_minutes IS NULL OR default_duration_minutes > 0),
                             difficulty  VARCHAR(20)
);

-- 2) Seria (szablon cyklicznych zajęć)
CREATE TABLE class_series (
                              id BIGSERIAL PRIMARY KEY,
                              type_id      BIGINT NOT NULL REFERENCES class_types(id),
                              instructor_id BIGINT NOT NULL REFERENCES instructors(id),
                              room_id      BIGINT NOT NULL REFERENCES rooms(id),
                              start_time   TIMESTAMPTZ NOT NULL,  -- pierwszy termin start
                              end_time     TIMESTAMPTZ NOT NULL,  -- pierwszy termin koniec
                              capacity     INTEGER NOT NULL CHECK (capacity > 0), -- domyślny limit dla wystąpień
                              recurrence_pattern VARCHAR(20) NOT NULL CHECK (recurrence_pattern IN ('DAILY','WEEKLY','MONTHLY')),
                              recurrence_until   TIMESTAMPTZ NOT NULL,           -- do kiedy generować
                              note         TEXT,
                              active       BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              CONSTRAINT chk_series_time_order CHECK (start_time < end_time)
);

-- 3) Konkretne wystąpienia zajęć (terminy), powiązane opcjonalnie z serią
CREATE TABLE classes (
                         id BIGSERIAL PRIMARY KEY,
                         series_id     BIGINT REFERENCES class_series(id),
                         type_id       BIGINT NOT NULL REFERENCES class_types(id),
                         instructor_id BIGINT NOT NULL REFERENCES instructors(id),
                         room_id       BIGINT NOT NULL REFERENCES rooms(id),
                         start_time    TIMESTAMPTZ NOT NULL,
                         end_time      TIMESTAMPTZ NOT NULL,
                         capacity      INTEGER NOT NULL CHECK (capacity > 0),
                         status        VARCHAR(16) NOT NULL DEFAULT 'PLANNED'
                             CHECK (status IN ('PLANNED','OPEN','CANCELLED','FINISHED')),
                         note          TEXT,
                         created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         CONSTRAINT chk_class_time_order CHECK (start_time < end_time)
);

-- 4) Rezerwacje na KONKRETNE wystąpienia
CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          class_id     BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
                          user_name    VARCHAR(100) NOT NULL,  -- placeholder dopóki nie ma tabeli users
                          status       VARCHAR(16)  NOT NULL CHECK (status IN ('REQUESTED','CONFIRMED','PAID','CANCELLED')),
                          created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          cancelled_at TIMESTAMPTZ
);

-- Jedna aktywna rezerwacja danej osoby na te same zajęcia (statusy aktywne)
CREATE UNIQUE INDEX ux_booking_unique_active
    ON bookings(class_id, user_name)
    WHERE status IN ('REQUESTED','CONFIRMED','PAID');

-- Indeksy typowych zapytań
CREATE INDEX idx_classes_start_time   ON classes(start_time);
CREATE INDEX idx_classes_room         ON classes(room_id);
CREATE INDEX idx_classes_instructor   ON classes(instructor_id);
CREATE INDEX idx_bookings_class       ON bookings(class_id);
CREATE INDEX idx_bookings_status      ON bookings(status);

-- 5) Triggery i funkcje pomocnicze

-- aktualizacja updated_at przy zmianach w classes
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
RETURN NEW;
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_classes_updated_at
    BEFORE UPDATE ON classes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- spójność: capacity klasy nie może przekraczać pojemności sali
CREATE OR REPLACE FUNCTION enforce_class_capacity_vs_room()
RETURNS TRIGGER AS $$
DECLARE room_cap INT;
BEGIN
SELECT capacity INTO room_cap FROM rooms WHERE id = NEW.room_id;
IF NEW.capacity > room_cap THEN
    RAISE EXCEPTION 'Class capacity (%) cannot exceed room capacity (%)', NEW.capacity, room_cap;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_enforce_class_capacity_vs_room ON classes;
CREATE TRIGGER trg_enforce_class_capacity_vs_room
    BEFORE INSERT OR UPDATE OF room_id, capacity ON classes
    FOR EACH ROW EXECUTE FUNCTION enforce_class_capacity_vs_room();

-- blokada overbookingu: licz rezerwacje aktywne i porównuj z capacity
CREATE OR REPLACE FUNCTION check_class_capacity()
RETURNS TRIGGER AS $$
DECLARE reserved_count INT;
DECLARE class_capacity INT;
BEGIN
SELECT capacity INTO class_capacity FROM classes WHERE id = NEW.class_id;
SELECT COUNT(*) INTO reserved_count
FROM bookings
WHERE class_id = NEW.class_id
  AND status IN ('REQUESTED','CONFIRMED','PAID');

IF reserved_count >= class_capacity THEN
    RAISE EXCEPTION 'Brak wolnych miejsc na zajęciach (class_id=%)', NEW.class_id;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_check_capacity ON bookings;
CREATE TRIGGER trg_check_capacity
    BEFORE INSERT ON bookings
    FOR EACH ROW EXECUTE FUNCTION check_class_capacity();

-- 6) Wygodny widok dostępności (capacity - aktywne rezerwacje)
CREATE OR REPLACE VIEW v_class_availability AS
SELECT
    c.id AS class_id,
    c.capacity,
    COUNT(b.*) FILTER (WHERE b.status IN ('REQUESTED','CONFIRMED','PAID')) AS reserved,
    (c.capacity - COUNT(b.*) FILTER (WHERE b.status IN ('REQUESTED','CONFIRMED','PAID'))) AS free_spots
FROM classes c
         LEFT JOIN bookings b ON b.class_id = c.id
GROUP BY c.id;
