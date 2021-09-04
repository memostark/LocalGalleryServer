CREATE TABLE IF NOT EXISTS media_folder (
  id BIGINT AUTO_INCREMENT NOT NULL,
  name VARCHAR(255) NULL,
  CONSTRAINT pk_mediafolder PRIMARY KEY (id)
);

ALTER TABLE media_folder ADD CONSTRAINT uc_mediafolder_name UNIQUE (name);

CREATE TABLE IF NOT EXISTS media_file (
  id BIGINT AUTO_INCREMENT NOT NULL,
  filename VARCHAR(255) NULL,
  width INT NULL,
  height INT NULL,
  folder_id BIGINT NULL,
  lenght INT NOT NULL,
  CONSTRAINT pk_mediafile PRIMARY KEY (id)
);

ALTER TABLE media_file ADD CONSTRAINT FK_MEDIAFILE_ON_FOLDER FOREIGN KEY (folder_id) REFERENCES media_folder (id);
