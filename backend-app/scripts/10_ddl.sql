CREATE TABLE region
(
  region_id          SERIAL PRIMARY KEY,
  region_name        VARCHAR(100) NOT NULL,
  creation_timestamp TIMESTAMP    NOT NULL
);

CREATE TABLE location
(
  location_id   BIGSERIAL PRIMARY KEY,
  location_name VARCHAR(200) NOT NULL,
  region_id     BIGINT       NOT NULL,
  note          TEXT,
  FOREIGN KEY (region_id) REFERENCES region (region_id)
);

CREATE TABLE batch_processing
(
  batch_name VARCHAR(20) PRIMARY KEY,
  last_execution_date_time TIMESTAMP
);

CREATE TABLE batch_processing_file
(
  batch_processing_file_id BIGSERIAL PRIMARY KEY,
  batch_name VARCHAR(20) NOT NULL,
  file_name VARCHAR(300) NOT NULL
);
