-- Default 1 is one because only file tags exist
ALTER TABLE tag_entity ADD tag_type integer not null;
UPDATE tag_entity SET tag_type = 1;

ALTER TABLE tag_entity DROP CONSTRAINT uc_tagentity_name;
ALTER TABLE tag_entity ADD CONSTRAINT uc_tagentity_name_tagtype UNIQUE (name, tag_type);

-- Create join
CREATE TABLE `folder_tags` (
  `tag_id` bigint NOT NULL,
  `folder_id` bigint NOT NULL,
  PRIMARY KEY (`tag_id`,`folder_id`),
  CONSTRAINT `constr_foldertags_folder_fk` FOREIGN KEY (`folder_id`) REFERENCES `media_folder` (`id`) ON DELETE CASCADE,
  CONSTRAINT `constr_foldertags_tag_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag_entity` (`id`) ON DELETE CASCADE
);