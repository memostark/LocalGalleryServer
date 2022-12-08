ALTER TABLE media_folder ADD cover_file_id BIGINT;
ALTER TABLE media_folder
ADD CONSTRAINT FK_CoverFileFolder FOREIGN KEY (cover_file_id) REFERENCES media_file(id);