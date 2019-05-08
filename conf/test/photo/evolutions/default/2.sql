-- !Ups

-- Create test users
INSERT INTO User (username, password, salt) VALUES ('tester1@gmail.com', 'password', 'salt');

-- Insert a photo for testing
INSERT INTO Photo (user_id, file_name, public_photo, profile_photo) VALUES (1, 'Test File', 1, 1);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM Photo;
DELETE FROM User;
