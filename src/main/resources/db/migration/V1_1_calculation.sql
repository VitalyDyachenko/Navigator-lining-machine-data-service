CREATE TABLE calculation
(
    work_id BIGINT PRIMARY KEY REFERENCES work (id),

    version SMALLINT NOT NULL,
    calculation_time TIMESTAMP,
    computer_name VARCHAR(24),

    channel_mask INTEGER NOT NULL,
    otvod_count INTEGER NOT NULL,

    against_picketage BOOLEAN,
    moving_backward BOOLEAN,
    left_press BOOLEAN,
    right_press BOOLEAN,
    short_chord BOOLEAN,
    dls_enabled BOOLEAN,
    warning BOOLEAN,

    -- tsSETUP_PROJECT
    setup_passenger_speed INTEGER,
    setup_freight_speed INTEGER,
    setup_max_shift_left_mm INTEGER,
    setup_max_shift_right_mm INTEGER,
    setup_max_lift_mm INTEGER,
    setup_min_lift_mm INTEGER,

    -- tsEXTRA_EXIT
    extra_exit_machine_pos INTEGER,
    extra_exit_plane_front_pos INTEGER,
    extra_exit_time_seconds BIGINT,
    extra_exit_status SMALLINT,

    extra_exit_against_picketage BOOLEAN,
    extra_exit_moving_backward BOOLEAN,
    extra_exit_left_press BOOLEAN,
    extra_exit_right_press BOOLEAN,
    extra_exit_short_chord BOOLEAN,
    extra_exit_intertrack_space SMALLINT,
    extra_exit_marker_at_front BOOLEAN,
    extra_exit_dls_enabled BOOLEAN,

    CONSTRAINT chk_extra_exit_status
        CHECK (extra_exit_status BETWEEN 0 AND 3),


    -- tsCDT_H
    cdt_base_rail SMALLINT,
    cdt_points_mask SMALLINT,
    cdt_base_offset_mm SMALLINT,
    cdt_objects_count INTEGER
);


CREATE TABLE calculation_channel_type
(
    id SMALLINT PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL
);
INSERT INTO calculation_channel_type (id, code, name)
VALUES
    (0, 'PRJ_CH_PLANE_Y', 'Сдвиг пути'),
    (1, 'PRJ_CH_PLANE_H', 'Проектная стрела в плане'),
    (2, 'PRJ_CH_PLANE_Ctrl_H', 'Проектная стрела контрольного стрелографа'),
    (3, 'PRJ_CH_LEVEL', 'Проектное возвышение'),
    (4, 'PRJ_CH_PROFILE_Y', 'Подъёмка пути'),
    (5, 'PRJ_CH_PROFILE_H', 'Проектная стрела в профиле'),
    (6, 'PRM_CH_REGIM_PRU', 'Режим ПРУ'),
    (7, 'PRM_CH_TEMPERATURE_FIX', 'Температура закрепления'),
    (8, 'RESERVED_8', 'Резервный канал'),
    (9, 'RESERVED_9', 'Резервный канал'),
    (10, 'RESERVED_10', 'Резервный канал'),
    (11, 'RESERVED_11', 'Резервный канал'),
    (12, 'RESERVED_12', 'Резервный канал'),
    (13, 'RESERVED_13', 'Резервный канал'),
    (14, 'PRJ_CH_CLEARANCE_Left', 'Габарит левый'),
    (15, 'PRJ_CH_CLEARANCE_Right', 'Габарит правый');

CREATE TABLE calculation_channel
(
    work_id BIGINT NOT NULL REFERENCES calculation (work_id),

    channel_index SMALLINT NOT NULL REFERENCES calculation_channel_type (id),
    value_type SMALLINT NOT NULL,
    physical_type_id SMALLINT NOT NULL REFERENCES channel_physical_type (id),
    scale REAL NOT NULL,

    data BYTEA,

    PRIMARY KEY (work_id, channel_index),

    CONSTRAINT chk_calculation_channel_index
        CHECK (channel_index BETWEEN 0 AND 15)
);


CREATE TABLE otvod_mode
(
    id SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);
INSERT INTO otvod_mode (id, name)
VALUES
    (0, 'По умолчанию'),
    (1, 'Давилка / ramp'),
    (2, 'Экстренный старт'),
    (3, 'Экстренный финиш'),
    (4, 'Для выправки');

CREATE TABLE otvod_source
(
    id SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);
INSERT INTO otvod_source (id, name)
VALUES
    (0, 'ВПР-машина'),
    (1, 'ЦП НИЦ Путеец'),
    (2, 'ПЦД'),
    (3, 'ПЧ');

CREATE TABLE otvod_geometry_type
(
    id SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);
INSERT INTO otvod_geometry_type (id, name)
VALUES
    (1, 'План'),
    (2, 'Уровень'),
    (4, 'Профиль');

CREATE TABLE calculation_otvod
(
    id                           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id                      BIGINT   NOT NULL REFERENCES calculation (work_id),

    otvod_index                  SMALLINT NOT NULL,

    -- Type
    is_left                      BOOLEAN  NOT NULL,
    is_right                     BOOLEAN  NOT NULL,
    is_non_linear                BOOLEAN  NOT NULL,
    is_proportional              BOOLEAN  NOT NULL,
    is_corrected                 BOOLEAN  NOT NULL,

    geometry_type_id             SMALLINT NOT NULL REFERENCES otvod_geometry_type (id),
    mode_id                      SMALLINT NOT NULL REFERENCES otvod_mode (id),
    source_id                    SMALLINT NOT NULL REFERENCES otvod_source (id),

    is_auto_by_clearance         BOOLEAN  NOT NULL,

    start_points_m               DOUBLE PRECISION[] NOT NULL,
    end_points_m                 DOUBLE PRECISION[] NOT NULL,
    start_coefficients           DOUBLE PRECISION[] NOT NULL,
    end_coefficients             DOUBLE PRECISION[] NOT NULL,
    start_initial_y_tenth_mm     SMALLINT[] NOT NULL,
    end_initial_y_tenth_mm       SMALLINT[] NOT NULL,

    delta_plane_left_tenth_mm    SMALLINT NOT NULL,
    delta_plane_right_tenth_mm   SMALLINT NOT NULL,
    delta_profile_left_tenth_mm  SMALLINT NOT NULL,
    delta_profile_right_tenth_mm SMALLINT NOT NULL,

    creation_time                TIMESTAMP,

    CONSTRAINT chk_otvod_points_arrays
        CHECK (
            cardinality(start_points_m) = 4
                AND cardinality(end_points_m) = 4
                AND cardinality(start_coefficients) = 4
                AND cardinality(end_coefficients) = 4
                AND cardinality(start_initial_y_tenth_mm) = 2
                AND cardinality(end_initial_y_tenth_mm) = 2
            )
);


CREATE TABLE calculation_cdt_point
(
id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

work_id BIGINT NOT NULL REFERENCES calculation (work_id),

step_index INTEGER NOT NULL,
is_project_axis BOOLEAN NOT NULL, -- false - измеренная ось Track_SRV, true - проектная ось Track_PRJ

x DOUBLE PRECISION NOT NULL,
y DOUBLE PRECISION NOT NULL,
h DOUBLE PRECISION NOT NULL
);