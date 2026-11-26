-- correction.sql
CREATE TABLE correction
(
    work_id BIGINT PRIMARY KEY REFERENCES work (id),

    version SMALLINT NOT NULL,
    correction_time TIMESTAMP,

    channel_mask INTEGER NOT NULL,

    start_km INTEGER, start_m SMALLINT, start_mm SMALLINT,
    end_km INTEGER, end_m SMALLINT, end_mm SMALLINT,

    -- TODO Вроде это хранить лишнее
    --fragment_count INTEGER NOT NULL,
    --rfid_count INTEGER NOT NULL,

    against_picketage BOOLEAN,
    moving_backward BOOLEAN,
    left_press BOOLEAN,
    right_press BOOLEAN,
    short_chord BOOLEAN,
    intertrack_space SMALLINT,
    marker_at_front BOOLEAN,
    dls_enabled BOOLEAN,

    CONSTRAINT chk_correction_channel_mask
        CHECK (channel_mask BETWEEN 0 AND 65535),

    CONSTRAINT chk_correction_intertrack_space
        CHECK (intertrack_space BETWEEN 0 AND 3)
);



CREATE TABLE track_element_type
(
    id SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

INSERT INTO track_element_type (id, name)
VALUES
    (0, 'Не определён'),
    (1, 'Круговая'),
    (2, 'Переходная'),
    (4, 'Прямая');



CREATE TABLE correction_fragment
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES correction (work_id),
    fragment_index SMALLINT NOT NULL,

    machine_params_id BIGINT NOT NULL REFERENCES machine_params (id),

    start_time TIMESTAMP,
    end_time TIMESTAMP,

    start_km SMALLINT,
    start_picket SMALLINT,
    start_m REAL,

    end_km SMALLINT,
    end_picket SMALLINT,
    end_m REAL,

    -- IdPOS
    start_pos_step_tenths INTEGER NOT NULL,
    end_pos_step_tenths INTEGER NOT NULL,

    against_picketage BOOLEAN,
    moving_backward BOOLEAN,
    left_press BOOLEAN,
    right_press BOOLEAN,
    short_chord BOOLEAN,
    intertrack_space SMALLINT,
    marker_at_front BOOLEAN,
    dls_enabled BOOLEAN,
    setup_mark BOOLEAN,
    navigator_vpi BOOLEAN,

    channel_mask INTEGER NOT NULL,
    step_size_mm INTEGER NOT NULL,
    step_count BIGINT NOT NULL,

    -- tsSOFTWARE
    software_type VARCHAR(4),
    software_major SMALLINT,
    software_minor SMALLINT,
    software_build SMALLINT,
    software_revision SMALLINT,

    operator_text VARCHAR(97),
    remark_mask INTEGER,
    start_element_type_id SMALLINT REFERENCES track_element_type (id),
    end_element_type_id SMALLINT REFERENCES track_element_type (id),

    CONSTRAINT uq_correction_fragment_index
        UNIQUE (work_id, fragment_index),

    CONSTRAINT chk_fragment_pickets
        CHECK (
            start_picket BETWEEN 0 AND 9
                AND end_picket BETWEEN 0 AND 9
            ),

    CONSTRAINT chk_fragment_pos_order
        CHECK (start_pos_step_tenths <= end_pos_step_tenths),

    CONSTRAINT chk_fragment_intertrack_space
        CHECK (intertrack_space BETWEEN 0 AND 3),

    CONSTRAINT chk_fragment_channel_mask
        CHECK (channel_mask BETWEEN 0 AND 65535),

    CONSTRAINT chk_fragment_remark_mask
        CHECK (remark_mask BETWEEN 0 AND 65535)
);

CREATE TABLE correction_fragment_hardware
(
    fragment_id BIGINT PRIMARY KEY REFERENCES correction_fragment (id),

    mnk_major SMALLINT NOT NULL,
    mnk_minor SMALLINT NOT NULL,
    mnk_description VARCHAR(12),

    system_major SMALLINT NOT NULL,
    system_minor SMALLINT NOT NULL,
    system_description VARCHAR(12),

    rfid_major SMALLINT NOT NULL,
    rfid_minor SMALLINT NOT NULL,
    rfid_description VARCHAR(12),

    rfb_major SMALLINT NOT NULL,
    rfb_minor SMALLINT NOT NULL,
    rfb_description VARCHAR(12),

    step_ms_error_tenth SMALLINT NOT NULL,
    step_calibration_date DATE,

    aout_ms_error_tenth SMALLINT[] NOT NULL,
    aout_calibration_dates DATE[] NOT NULL,

    ain_ms_error_tenth SMALLINT[] NOT NULL,
    ain_calibration_dates DATE[] NOT NULL,

    CONSTRAINT chk_hardware_calibration_arrays
        CHECK (
            cardinality(aout_ms_error_tenth) = 8
                AND cardinality(aout_calibration_dates) = 8
                AND cardinality(ain_ms_error_tenth) = 8
                AND cardinality(ain_calibration_dates) = 8
            )
);


CREATE TABLE correction_channel_type
(
    id SMALLINT PRIMARY KEY,
    is_project BOOLEAN NOT NULL,
    name VARCHAR(100) NOT NULL
);

INSERT INTO correction_channel_type (id, is_project, name)
VALUES
    (0, FALSE, 'Стрелограф рабочий'),
    (1, FALSE, 'Стрелограф контрольный'),
    (2, FALSE, 'Уровень рабочий'),
    (3, FALSE, 'Уровень задний'),
    (4, FALSE, 'Уровень передний'),
    (5, FALSE, 'Профиль левый'),
    (6, FALSE, 'Профиль правый'),
    (7, FALSE, 'Шаблон'),
    (8, TRUE, 'Сдвиг пути'),
    (9, TRUE, 'Проектная стрела в плане'),
    (10, TRUE, 'Проектная стрела контрольного стрелографа'),
    (11, TRUE, 'Проектное возвышение'),
    (12, TRUE, 'Подъёмка пути'),
    (13, TRUE, 'Проектная стрела в профиле'),
    (14, TRUE, 'Режим ПРУ'),
    (15, TRUE, 'Температура закрепления');

CREATE TABLE correction_fragment_channel
(
    fragment_id BIGINT NOT NULL REFERENCES correction_fragment (id),

    channel_index SMALLINT NOT NULL REFERENCES correction_channel_type (id),
    data BYTEA,

    PRIMARY KEY (fragment_id, channel_index),

    CONSTRAINT chk_correction_channel_index
        CHECK (channel_index BETWEEN 0 AND 15)
);


CREATE TABLE correction_pru_cycle
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    fragment_id BIGINT NOT NULL REFERENCES correction_fragment (id),

    pos_step_tenths INTEGER NOT NULL,
    recorded_time TIME NOT NULL,
    cycle_duration_tenth_sec SMALLINT NOT NULL,
    cycle_number SMALLINT NOT NULL,

    is_right_press BOOLEAN NOT NULL,

    alignment_enabled BOOLEAN NOT NULL,
    left_lift_enabled BOOLEAN NOT NULL,
    right_lift_enabled BOOLEAN NOT NULL,

    delta_alignment SMALLINT NOT NULL,
    delta_left_lift SMALLINT NOT NULL,
    delta_right_lift SMALLINT NOT NULL,

    alert_regim BOOLEAN NOT NULL,
    alert_skip_track BOOLEAN NOT NULL,
    alert_rollback BOOLEAN NOT NULL,

    CONSTRAINT chk_pru_cycle_duration
        CHECK (cycle_duration_tenth_sec BETWEEN 0 AND 127),

    CONSTRAINT chk_pru_cycle_number
        CHECK (cycle_number BETWEEN 0 AND 3)
);



CREATE TABLE correction_marker
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    fragment_id BIGINT NOT NULL REFERENCES correction_fragment (id),

    type_id SMALLINT NOT NULL REFERENCES marker_type (id),
    step_index BIGINT NOT NULL
);

CREATE TABLE correction_rfid
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    fragment_id BIGINT NOT NULL REFERENCES correction_fragment (id),

    timestamp_ms BIGINT NOT NULL,
    sync_pulse_count BIGINT NOT NULL,
    tag_identifier BIGINT NOT NULL,
    data_block BIGINT NOT NULL,

    odometer_delta_len_m REAL,
    odometer_scale_adj REAL,

    block_mask SMALLINT NOT NULL,
    point_count SMALLINT NOT NULL,
    has_error BOOLEAN NOT NULL,

    control_distance_m DOUBLE PRECISION NOT NULL,

    CONSTRAINT chk_correction_rfid_state
        CHECK (
            block_mask BETWEEN 0 AND 15
                AND point_count BETWEEN 0 AND 2047
            )
);


CREATE TABLE correction_control_point
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    fragment_id BIGINT NOT NULL REFERENCES correction_fragment (id),

    regim_code SMALLINT NOT NULL,
    temp_ctrl SMALLINT NOT NULL,
    recorded_time TIME NOT NULL,

    interface_regulators SMALLINT[] NOT NULL,
    external_regulators SMALLINT[] NOT NULL,

    left_rail_temp_c SMALLINT NOT NULL,
    right_rail_temp_c SMALLINT NOT NULL,
    air_temp_c SMALLINT NOT NULL,

    CONSTRAINT chk_ctrl_point_regim
        CHECK (regim_code BETWEEN 0 AND 8191),

    CONSTRAINT chk_ctrl_point_temp_ctrl
        CHECK (temp_ctrl BETWEEN 0 AND 3),

    CONSTRAINT chk_ctrl_point_regulators
        CHECK (
            cardinality(interface_regulators) = 8
                AND cardinality(external_regulators) = 5
            )
);