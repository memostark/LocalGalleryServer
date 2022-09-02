CREATE TABLE tag_entity (
  id BIGINT NOT NULL,
  creation_date datetime NOT NULL,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT pk_tagentity PRIMARY KEY (id)
);

CREATE TABLE `media_tags` (
  `tag_id` bigint NOT NULL,
  `media_id` bigint NOT NULL,
  PRIMARY KEY (`tag_id`,`media_id`),
  CONSTRAINT `constr_mediatags_media_fk` FOREIGN KEY (`media_id`) REFERENCES `media_file` (`id`),
  CONSTRAINT `constr_mediatags_tag_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag_entity` (`id`)
);