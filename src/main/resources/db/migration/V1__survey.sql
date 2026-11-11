CREATE TABLE survey
(
    work_id BIGINT PRIMARY KEY REFERENCES work (id),

    version SMALLINT NOT NULL,
    survey_time TIMESTAMP,

    channel_mask INTEGER NOT NULL,

    -- =========== Flag
    against_picketage  BOOLEAN,
    moving_backward    BOOLEAN,
    left_press         BOOLEAN,
    right_press        BOOLEAN,
    short_chord        BOOLEAN,
    intertrack_space   SMALLINT,
    marker_at_front    BOOLEAN,
    dls_enabled        BOOLEAN,
    -- ======================

    vpi_start_stp BIGINT,
    -- ========= ID TRIP parsed =============
    trip_number INTEGER,
    trip_wagon_number SMALLINT,
    trip_year SMALLINT,
    trip_month SMALLINT,
    trip_railway_code SMALLINT,
    trip_check_code SMALLINT,
    trip_checked BOOLEAN
    -- ======================
);


CREATE TABLE survey_machine_params
(
    work_id BIGINT PRIMARY KEY REFERENCES survey (work_id),

    continuous_correction BOOLEAN,
    profile_enabled BOOLEAN,
    plane_satellite BOOLEAN,
    --Биты 0–1: закодированный параметр количества подбиваемых шпал
    tamping_sleepers SMALLINT,

    alignment_arms_mm INTEGER[] NOT NULL,
    leveling_arms_mm INTEGER[] NOT NULL,
    trolley_arms_mm INTEGER[] NOT NULL,
    gauge_width_mm INTEGER NOT NULL,

    CONSTRAINT chk_machine_arrays
        CHECK (
            cardinality(alignment_arms_mm) = 5
                AND cardinality(leveling_arms_mm) = 5
                AND cardinality(trolley_arms_mm) = 4
            )
);


CREATE TABLE survey_fragment
(
    work_id BIGINT PRIMARY KEY REFERENCES survey(work_id),

    start_step_index INTEGER,

    start_km SMALLINT,
    start_m SMALLINT,

    end_km SMALLINT,
    end_m SMALLINT,

    direction_station_code BIGINT,
    park_code SMALLINT,
    track_number VARCHAR(10),
    against_picketage BOOLEAN,
    name VARCHAR(51)
);


CREATE TABLE survey_post
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    step_index INTEGER NOT NULL,
    kilometer_number SMALLINT NOT NULL,
    is_passport BOOLEAN NOT NULL
);



CREATE TABLE survey_switch
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    step_index INTEGER NOT NULL,
    is_trail_direction BOOLEAN, -- NULL = undefined
    type SMALLINT,
    length_m INTEGER,
    switch_number VARCHAR(5),
    station_code INTEGER, --todo FK?
    is_passport BOOLEAN NOT NULL
);

CREATE TABLE build_type
(
    id SMALLINT PRIMARY KEY,
    name VARCHAR(40) NOT NULL
);
INSERT INTO build_type (id, name)
VALUES
    (0, 'Неопределённый'),
    (1, 'Безбалластный мост'),
    (2, 'Балластный мост'),
    (3, 'Тоннель'),
    (4, 'Путепровод'),
    (5, 'Труба'), --todo исключение!!!!!!
    (6, 'Платформа');


CREATE TABLE survey_build
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    start_step_index INTEGER NOT NULL,
    end_step_index INTEGER NOT NULL,

    type_id SMALLINT NOT NULL REFERENCES build_type(id),

    is_passport BOOLEAN NOT NULL
);


CREATE TABLE survey_speed
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    start_step_index INTEGER NOT NULL,
    end_step_index INTEGER NOT NULL,

    start_km SMALLINT NOT NULL,
    start_picket SMALLINT NOT NULL,

    end_km SMALLINT NOT NULL,
    end_picket SMALLINT NOT NULL,

    passenger_speed INTEGER NOT NULL,
    freight_speed INTEGER NOT NULL,
    empty_train_speed INTEGER NOT NULL,

    CONSTRAINT chk_speed_pickets
        CHECK (
            start_picket BETWEEN 0 AND 9 --в исходном 4 байта, по факту ограничение должно быть до 9
                AND end_picket BETWEEN 0 AND 9
            )
);


CREATE TABLE survey_thread
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    start_step_index INTEGER NOT NULL,
    end_step_index INTEGER NOT NULL
);


CREATE TABLE survey_urov
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    step_index INTEGER NOT NULL,
    value_mm REAL NOT NULL
);


CREATE TABLE survey_curve
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    step_index INTEGER NOT NULL,

    radius_m REAL NOT NULL,
    gauge_norm_mm REAL NOT NULL,
    wear_mm REAL NOT NULL,
    tolerance_mm REAL NOT NULL
);

CREATE TABLE defect_type
(
    code INTEGER PRIMARY KEY,
    designation VARCHAR(4) NOT NULL,
    name VARCHAR(100) NOT NULL
);

INSERT INTO defect_type (code, designation, name)
VALUES
    (3117, 'Р', 'Рихтовка по левому борту'),
    (3116, 'Р', 'Рихтовка по правому борту'),
    (3115, 'П', 'Перекос'),
    (3119, 'Пр', 'Просадка по левому борту'),
    (3118, 'Пр', 'Просадка по правому борту'),
    (3112, 'У', 'Плавное отклонение по уровню'),
    (3114, 'Суж', 'Сужение'),
    (3113, 'Уш', 'Уширение'),
    (3145, 'УУ', 'Разностороннее отклонение уровня');



CREATE TABLE survey_defect
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    start_step_index INTEGER NOT NULL,
    end_step_index INTEGER NOT NULL,

    mask INTEGER NOT NULL, -- todo мы не поняли что это т_т
    value_mm REAL NOT NULL,

    type_code INTEGER NOT NULL REFERENCES defect_type(code),

    passenger_speed INTEGER NOT NULL,
    freight_speed INTEGER NOT NULL,
    empty_train_speed INTEGER NOT NULL,

    degree SMALLINT NOT NULL,
    label VARCHAR(7),

    CONSTRAINT chk_defect_mask
        CHECK (mask BETWEEN 0 AND 65535),

    CONSTRAINT chk_defect_degree
        CHECK (degree BETWEEN 0 AND 255)
);


CREATE TABLE survey_gabarit
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    control_distance_m DOUBLE PRECISION,
    step_index INTEGER,

    kilometer SMALLINT,
    picket SMALLINT,
    meter REAL,

    object_type SMALLINT,
    clearance_code SMALLINT,
    intertrack_code SMALLINT,

    left_distance_mm INTEGER,
    right_distance_mm INTEGER,

    height_mm SMALLINT,

    left_range_mm SMALLINT,
    right_range_mm SMALLINT,

    description VARCHAR(64),

    CONSTRAINT chk_gabarit_picket
        CHECK (picket BETWEEN 0 AND 9),

    CONSTRAINT chk_gabarit_clearance
        CHECK (clearance_code BETWEEN 0 AND 3),

    CONSTRAINT chk_gabarit_intertrack
        CHECK (intertrack_code BETWEEN 0 AND 3)
);



CREATE TABLE channel_physical_type
(
    id SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

INSERT INTO channel_physical_type (id, name)
VALUES
    (0, 'Безразмерная величина'),
    (1, 'Миллиметры'),
    (2, 'Метры'),
    (3, 'Градусы'),
    (4, 'Радианы'),
    (5, 'Секунды');



CREATE TABLE survey_channel
(
    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    channel_index SMALLINT NOT NULL,
    value_type SMALLINT NOT NULL,
    physical_type_id SMALLINT NOT NULL
        REFERENCES channel_physical_type(id),
    scale REAL NOT NULL,

    data BYTEA,

    PRIMARY KEY (work_id, channel_index),

    CONSTRAINT chk_channel_index
        CHECK (channel_index BETWEEN 0 AND 15)
);



CREATE TABLE marker_type
(
    id SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

INSERT INTO marker_type (id, name)
VALUES
    (0, 'Пикет'),
    (1, 'Опора контактной сети'),
    (2, 'Переезд начало'),
    (3, 'Переезд конец'),
    (4, 'Платформа начало'),
    (5, 'Платформа конец'),
    (6, 'Мост начало'),
    (7, 'Мост конец'),
    (8, 'Тоннель начало'),
    (9, 'Тоннель конец'),
    (10, 'Сигнал (светофор)'),
    (11, 'Труба'),
    (12, 'УТС'),
    (13, 'Стрелка крестовина'),
    (14, 'Стрелка остряк'),
    (15, 'Разное'),
    (16, 'Прижим левый'),
    (17, 'Прижим правый');


CREATE TABLE survey_marker
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    type_id SMALLINT NOT NULL REFERENCES marker_type(id),

    step_index BIGINT NOT NULL
);


CREATE TABLE survey_rfid
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    sync_pulse_count BIGINT NOT NULL,
    tag_identifier BIGINT NOT NULL,
    data_blocks BIGINT[] NOT NULL,

    kilometer SMALLINT NOT NULL,
    picket SMALLINT NOT NULL,
    marker_type_id SMALLINT REFERENCES marker_type(id),

    block_mask SMALLINT NOT NULL,
    point_count SMALLINT NOT NULL,
    has_error BOOLEAN NOT NULL,

    control_distance_m DOUBLE PRECISION NOT NULL,

    CONSTRAINT chk_rfid_blocks
        CHECK (cardinality(data_blocks) = 3),

    CONSTRAINT chk_rfid_picket
        CHECK (picket BETWEEN 0 AND 9),

    CONSTRAINT chk_rfid_state
        CHECK (
            block_mask BETWEEN 0 AND 15
                AND point_count BETWEEN 0 AND 2047
            )
);

