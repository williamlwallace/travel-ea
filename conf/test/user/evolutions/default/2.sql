-- !Ups

-- Insert a user for testing
INSERT INTO User (username, password, salt) VALUES ('dave@gmail.com', 'password', 'salt');

-- !Downs
-- Now delete all rows from tables
DELETE FROM User;