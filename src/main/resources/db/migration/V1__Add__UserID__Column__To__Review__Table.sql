ALTER TABLE review ADD COLUMN user_id BIGINT;
ALTER TABLE review ADD CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user_account(id);

UPDATE review SET user_id = (SELECT id FROM user_account WHERE id = 2) WHERE user_id IS NULL;
