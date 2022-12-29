ALTER TABLE media_file
DROP FOREIGN KEY FK_MEDIAFILE_ON_FOLDER;
ALTER TABLE media_file
ADD CONSTRAINT FK_MEDIAFILE_ON_FOLDER FOREIGN KEY (folder_id) REFERENCES media_folder (id) ON DELETE CASCADE;

ALTER TABLE media_tags
DROP FOREIGN KEY constr_mediatags_media_fk;
ALTER TABLE media_tags
ADD CONSTRAINT constr_mediatags_media_fk FOREIGN KEY (media_id) REFERENCES media_file (id) ON DELETE CASCADE;

ALTER TABLE media_tags
DROP FOREIGN KEY constr_mediatags_tag_fk;
ALTER TABLE media_tags
ADD CONSTRAINT constr_mediatags_tag_fk FOREIGN KEY (tag_id) REFERENCES tag_entity (id) ON DELETE CASCADE;