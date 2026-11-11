CREATE TABLE railway
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       SMALLINT    NOT NULL UNIQUE,
    name       VARCHAR(30) NOT NULL,
    short_name VARCHAR(6)
);

CREATE TABLE direction
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_id    INTEGER NOT NULL,

    railway_id   BIGINT  NOT NULL REFERENCES railway (id),
    code         SMALLINT,
    railway_code SMALLINT,
    name         VARCHAR(50)
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





