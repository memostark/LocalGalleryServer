-- Default 1 is one because
ALTER TABLE tag_entity ADD tag_type integer not null;
UPDATE tag_entity SET tag_type = 1;