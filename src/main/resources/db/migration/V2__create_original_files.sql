CREATE TABLE original_files (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

        survey_filename VARCHAR(255) NOT NULL,
        survey_file BYTEA NOT NULL,

        calculation_filename VARCHAR(255) NOT NULL,
        calculation_file BYTEA NOT NULL,

        work_filename VARCHAR(255) NOT NULL,
        work_file BYTEA NOT NULL
);