ALTER TABLE book ADD COLUMN IF NOT EXISTS image_data oid;

ALTER TABLE book ALTER COLUMN image_data TYPE oid USING image_data::oid;
