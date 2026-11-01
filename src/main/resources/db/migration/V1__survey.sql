CREATE TABLE survey
(
    work_id BIGINT PRIMARY KEY REFERENCES work (id),

    machine_id BIGINT NOT NULL REFERENCES machine (id),
    peregon_id BIGINT NOT NULL REFERENCES peregon (id),
    main_track_id BIGINT NOT NULL REFERENCES main_track (id),

    version SMALLINT NOT NULL,
    survey_time TIMESTAMP,

    track_number VARCHAR(7),
    adm_code SMALLINT,

    channel_mask INTEGER NOT NULL,
    step_size_mm INTEGER NOT NULL,
    step_count BIGINT NOT NULL,

    -- =========== Flags
    against_picketage  BOOLEAN,
    moving_backward    BOOLEAN,
    press              SMALLINT, --0-влево, 1-вправо, 3-по обоим ниткам
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


CREATE TABLE survey_station_position
(
    work_id BIGINT NOT NULL REFERENCES survey(work_id),

    station_order SMALLINT NOT NULL CHECK (station_order IN (1, 2)),

    station_id BIGINT NOT NULL REFERENCES station(id),

    --Исходный OS_PRIZ_ID — признак оси раздельного пункта
    axis_attribute_id SMALLINT,

    axis_km INTEGER,
    axis_m SMALLINT,

    start_km INTEGER,
    start_m SMALLINT,

    end_km INTEGER,
    end_m SMALLINT,

    PRIMARY KEY (work_id, station_order)
);



CREATE TABLE survey_fragment
(
    work_id BIGINT PRIMARY KEY REFERENCES survey(work_id),

    start_step_index INTEGER,

    start_km SMALLINT,
    start_m SMALLINT,
    start_mm SMALLINT,

    end_km SMALLINT,
    end_m SMALLINT,
    end_mm SMALLINT,

    direction_station_code BIGINT,
    park_code SMALLINT,
    track_number VARCHAR(10),
    against_picketage BOOLEAN,
    name VARCHAR(51)
);


