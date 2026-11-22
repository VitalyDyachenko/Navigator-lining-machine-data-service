-- railway.sql
CREATE TABLE railway
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       SMALLINT    NOT NULL UNIQUE,
    name       VARCHAR(30) NOT NULL,
    short_name VARCHAR(6)
);

CREATE TABLE direction
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_id INTEGER NOT NULL,

    railway_id BIGINT NOT NULL REFERENCES railway(id),

    code SMALLINT,
    mag_code SMALLINT,
    name VARCHAR(50),

    start_station_source_id INTEGER,
    end_station_source_id INTEGER
);


CREATE TABLE main_track
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_id         INTEGER NOT NULL,

    direction_id      BIGINT NOT NULL REFERENCES direction (id),

    number            VARCHAR(7),
    gauge_width_id    SMALLINT,
    main_track_source_id_osn INTEGER
);

--TODO NOT NULL

CREATE TABLE station
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_id     INTEGER NOT NULL,
    code          INTEGER,
    department_id INTEGER,
    okato_id      INTEGER,
    type_id       SMALLINT,
    name          VARCHAR(38)
);


CREATE TABLE peregon
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_id           INTEGER NOT NULL,

    direction_id        BIGINT  NOT NULL REFERENCES direction (id),

    start_station_id    BIGINT  NOT NULL REFERENCES station (id),
    end_station_id      BIGINT  NOT NULL REFERENCES station (id),

    exploitation_length DOUBLE PRECISION,
    name                VARCHAR(64)
);


CREATE TABLE machine_type
(
    id   SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

INSERT INTO machine_type (id, name)
VALUES
    (0, 'Рихтовочная/Путеизмеритель'),
    (1, 'ВПО'),
    (2, 'ВПР'),
    (3, 'ВПРС'),
    (4, 'ДСП');

-- TODO могут ли меняться геометрические параметры у машин

CREATE TABLE machine
(
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    number BIGINT,
    name   VARCHAR(24),
    type_id SMALLINT REFERENCES machine_type(id)
);

CREATE TABLE machine_params
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    machine_id BIGINT NOT NULL REFERENCES machine (id),

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
            ),

    CONSTRAINT uq_machine_params
        UNIQUE (
                machine_id,
                continuous_correction,
                profile_enabled,
                plane_satellite,
                tamping_sleepers,
                alignment_arms_mm,
                leveling_arms_mm,
                trolley_arms_mm,
                gauge_width_mm
            )
);




