ALTER TABLE media_folder
DROP FOREIGN KEY FK_CoverFileFolder;
ALTER TABLE media_folder
ADD CONSTRAINT FK_CoverFileFolder FOREIGN KEY (cover_file_id) REFERENCES media_file(id) ON DELETE SET NULL;