ALTER TABLE chatmessage RENAME COLUMN name TO message;
ALTER TABLE chatmessage ADD COLUMN timestamp TIMESTAMP;
