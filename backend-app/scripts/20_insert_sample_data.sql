-- REGION
INSERT INTO region (region_name, creation_timestamp)
VALUES ('北海道', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('東北', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('関東', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('中部', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('近畿', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('中国', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('四国', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('九州', current_timestamp);

INSERT INTO region (region_name, creation_timestamp)
VALUES ('沖縄', current_timestamp);

-- LOCATION
INSERT INTO location (location_name, region_id, note)
VALUES ('美ら海水族館', (SELECT region_id FROM region WHERE region_name = '沖縄'),
  '沖縄の代表的な水族館で、ジンベエザメをはじめ、様々な沖縄の海の生き物を見ることができます。');

INSERT INTO location (location_name, region_id, note)
VALUES ('首里城', (SELECT region_id FROM region WHERE region_name = '沖縄'),
  '琉球王朝の王城で、世界遺産の1つです。');

-- BATCH_PROCESSING
INSERT INTO batch_processing (batch_name)
values ('SAMPLE_APP_BATCH');
