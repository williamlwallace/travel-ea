-- !Ups

-- Insert a country for testing
INSERT INTO CountryDefinition (name) VALUES ('Test Country');

-- Create test users
INSERT INTO User (username, password, salt) VALUES ('tester1@gmail.com', 'password', 'salt');

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');

-- Add photo
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, is_profile) VALUES (1, './public/storage/photos/test/test.jpeg', './public/storage/photos/test/thumbnails/test.jpeg', 1, 1);

-- Insert a destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 48.8583, 2.2945, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Leaning tower of pizza', 'Monument', 'italy', 47.69, 2.2947, 1);

-- Add photo-destination link
INSERT INTO DestinationPhoto (photo_id, destination_id) VALUES (1, 2);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM DestinationPhoto;
DELETE FROM Photo;
DELETE FROM Destination;
DELETE FROM Profile;
DELETE FROM User;
