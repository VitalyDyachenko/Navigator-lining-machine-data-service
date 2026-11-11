CREATE TABLE work
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
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