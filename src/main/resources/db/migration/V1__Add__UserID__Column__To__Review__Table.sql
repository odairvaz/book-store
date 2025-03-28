DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'review' AND column_name = 'user_id') THEN
            ALTER TABLE review ADD COLUMN user_id BIGINT;
            ALTER TABLE review ADD CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user_account(id);
        END IF;
    END $$;

UPDATE review SET user_id = (SELECT id FROM user_account WHERE id = 2) WHERE user_id IS NULL;
