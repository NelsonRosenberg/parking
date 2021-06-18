-----------------------------------------------
-- Create Initial Database
-----------------------------------------------

CREATE SEQUENCE rate_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE rate (
   id         BIGINT NOT NULL,
   start_time VARCHAR,
   end_time   VARCHAR,
   price      INTEGER,
   tz         VARCHAR,
   PRIMARY KEY (id)
);

CREATE TABLE rate_days (
   rate_id BIGINT NOT NULL,
   days    VARCHAR
);

ALTER TABLE rate_days
   ADD CONSTRAINT rate_id_fk
   FOREIGN KEY (rate_id)
   REFERENCES rate;

