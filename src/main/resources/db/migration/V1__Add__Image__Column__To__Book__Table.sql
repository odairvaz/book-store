
ALTER TABLE book ALTER COLUMN image_data TYPE oid USING image_data::oid;
