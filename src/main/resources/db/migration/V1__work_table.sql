CREATE TABLE work
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    machine_id BIGINT NOT NULL REFERENCES machine (id),
    peregon_id BIGINT NOT NULL REFERENCES peregon (id),
    main_track_id BIGINT NOT NULL REFERENCES main_track (id),
    track_number VARCHAR(7),
    adm_code SMALLINT,

    start_km SMALLINT, start_m SMALLINT, start_mm SMALLINT,
    end_km SMALLINT, end_m SMALLINT, end_mm SMALLINT,

    step_size_mm INTEGER NOT NULL,
    step_count BIGINT NOT NULL
);

CREATE TABLE work_files
(
    id BIGINT PRIMARY KEY REFERENCES work(id),

    survey_filename      VARCHAR(255) NOT NULL,
    survey_file          BYTEA        NOT NULL,

    calculation_filename VARCHAR(255) NOT NULL,
    calculation_file     BYTEA        NOT NULL,

    work_filename        VARCHAR(255) NOT NULL,
    work_file            BYTEA        NOT NULL
);

CREATE TABLE geo_coord
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    work_id BIGINT NOT NULL REFERENCES work(id),

    point_index INTEGER NOT NULL,

    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude DOUBLE PRECISION NOT NULL,

    CONSTRAINT uq_survey_geo_point
        UNIQUE (work_id, point_index)
);

CREATE TABLE station_position
(
    work_id BIGINT NOT NULL REFERENCES work(id),

    station_order SMALLINT NOT NULL CHECK (station_order IN (1, 2)),

    station_id BIGINT NOT NULL REFERENCES station(id),

    --Исходный OS_PRIZ_ID — признак оси раздельного пункта
    axis_attribute_id SMALLINT,

    axis_km SMALLINT,
    axis_m SMALLINT,

    start_km SMALLINT,
    start_m SMALLINT,

    end_km SMALLINT,
    end_m SMALLINT,

    PRIMARY KEY (work_id, station_order)
);
