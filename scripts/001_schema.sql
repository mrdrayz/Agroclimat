-- =====================================================================
-- AgroClimate Monitoring — схема БД для PostgreSQL (snake_case).
-- Запустить ОДИН раз на чистой БД agro_db (например, в pgAdmin / psql).
-- Имена таблиц совпадают с @Table(name=...) в JPA-сущностях.
-- ВНИМАНИЕ: скрипт DROP'ает существующие таблицы (включая старые camelCase).
-- =====================================================================

-- Удаляем старые camelCase-варианты, если они остались с предыдущего запуска
DROP TABLE IF EXISTS Alert CASCADE;
DROP TABLE IF EXISTS Measurement CASCADE;
DROP TABLE IF EXISTS ThresholdRule CASCADE;
DROP TABLE IF EXISTS Sensor CASCADE;
DROP TABLE IF EXISTS GreenhouseUser CASCADE;
DROP TABLE IF EXISTS Zone CASCADE;
DROP TABLE IF EXISTS UserRole CASCADE;
DROP TABLE IF EXISTS Greenhouse CASCADE;
DROP TABLE IF EXISTS Role CASCADE;
DROP TABLE IF EXISTS AppUser CASCADE;

-- И snake_case-варианты тоже (если уже создавались Hibernate'ом)
DROP TABLE IF EXISTS alert CASCADE;
DROP TABLE IF EXISTS measurement CASCADE;
DROP TABLE IF EXISTS threshold_rule CASCADE;
DROP TABLE IF EXISTS sensor CASCADE;
DROP TABLE IF EXISTS greenhouse_user CASCADE;
DROP TABLE IF EXISTS zone CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS greenhouse CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

-- 1. app_user
CREATE TABLE app_user (
    id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         varchar UNIQUE NOT NULL,
    password_hash varchar NOT NULL,
    full_name     varchar,
    status        varchar NOT NULL DEFAULT 'ACTIVE',
    created_at    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_user_status CHECK (status IN ('ACTIVE', 'BLOCKED'))
);

-- 2. role
CREATE TABLE role (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        varchar UNIQUE NOT NULL,
    name        varchar,
    description text,
    CONSTRAINT chk_role_code CHECK (code IN ('ADMIN', 'AGRONOMIST', 'OPERATOR', 'VIEWER'))
);

-- 3. greenhouse
CREATE TABLE greenhouse (
    id       bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     varchar NOT NULL,
    location varchar
);

-- 4. user_role (composite PK)
CREATE TABLE user_role (
    user_id     bigint NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id     bigint NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    assigned_at timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- 5. zone
CREATE TABLE zone (
    id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    greenhouse_id bigint NOT NULL REFERENCES greenhouse(id) ON DELETE CASCADE,
    name          varchar NOT NULL,
    description   text,
    area_m2       numeric
);

-- 6. greenhouse_user (composite PK)
CREATE TABLE greenhouse_user (
    greenhouse_id bigint NOT NULL REFERENCES greenhouse(id) ON DELETE CASCADE,
    user_id       bigint NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    access_level  varchar NOT NULL DEFAULT 'VIEW',
    assigned_at   timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (greenhouse_id, user_id),
    CONSTRAINT chk_greenhouse_user_access CHECK (access_level IN ('VIEW', 'MANAGE'))
);

-- 7. sensor
CREATE TABLE sensor (
    id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zone_id      bigint NOT NULL REFERENCES zone(id) ON DELETE CASCADE,
    metric       varchar NOT NULL,
    name         varchar,
    status       varchar NOT NULL DEFAULT 'ACTIVE',
    installed_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sensor_metric CHECK (metric IN ('TEMPERATURE', 'HUMIDITY', 'LIGHT')),
    CONSTRAINT chk_sensor_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- 8. threshold_rule
CREATE TABLE threshold_rule (
    id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zone_id   bigint NOT NULL REFERENCES zone(id) ON DELETE CASCADE,
    metric    varchar NOT NULL,
    min_value numeric,
    max_value numeric,
    enabled   boolean NOT NULL DEFAULT true,
    severity  varchar NOT NULL,
    CONSTRAINT chk_threshold_rule_metric   CHECK (metric IN ('TEMPERATURE', 'HUMIDITY', 'LIGHT')),
    CONSTRAINT chk_threshold_rule_severity CHECK (severity IN ('INFO', 'WARN', 'CRITICAL'))
);

-- 9. measurement
CREATE TABLE measurement (
    id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sensor_id bigint NOT NULL REFERENCES sensor(id) ON DELETE CASCADE,
    ts        timestamp NOT NULL,
    value     numeric NOT NULL,
    source    varchar NOT NULL DEFAULT 'REAL',
    CONSTRAINT chk_measurement_source CHECK (source IN ('SIM', 'REAL'))
);

-- 10. alert
CREATE TABLE alert (
    id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rule_id    bigint NOT NULL REFERENCES threshold_rule(id) ON DELETE CASCADE,
    sensor_id  bigint NOT NULL REFERENCES sensor(id) ON DELETE CASCADE,
    started_at timestamp NOT NULL,
    ended_at   timestamp,
    status     varchar NOT NULL DEFAULT 'OPEN',
    last_value numeric,
    message    text,
    CONSTRAINT chk_alert_status CHECK (status IN ('OPEN', 'ACK', 'CLOSED'))
);

-- Индексы для частых запросов
CREATE INDEX idx_measurement_sensor_ts ON measurement (sensor_id, ts DESC);
CREATE INDEX idx_alert_status          ON alert (status);
CREATE INDEX idx_sensor_zone           ON sensor (zone_id);
CREATE INDEX idx_zone_greenhouse       ON zone (greenhouse_id);
