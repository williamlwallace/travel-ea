-- !Ups

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(1, 'Russian Federation'),(2, 'Finland'),(3, 'Kazakhstan');

-- Create test users
INSERT INTO User (username, password, salt) VALUES ('tester1@gmail.com', 'password', 'salt');

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');

-- Add photo
INSERT INTO Photo (user_id, filename, thumbnail_filename, caption, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test.jpeg', './public/storage/photos/test/thumbnails/test.jpeg', 'test caption', 1, 1);
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test2.jpeg', './public/storage/photos/test/thumbnails/test2.jpeg', 0, 0);
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test3.jpeg', './public/storage/photos/test/thumbnails/test3.jpeg', 1, 0);

-- Insert a destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 48.8583, 2.2945, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Leaning tower of pizza', 'Monument', 'italy', 47.69, 2.2947, 1);

-- Add photo-destination link
INSERT INTO DestinationPhoto (photo_id, destination_id) VALUES (1, 2);

INSERT INTO Tag (name) VALUES ('Russia'), ('sports'), ('#TravelEA');
INSERT INTO PhotoTag (tag_id, photo_id) VALUES (1, 2);
INSERT INTO PhotoTag (tag_id, photo_id) VALUES (2, 2);

-- !Downs
-- Now delete all rows from tables (DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE)
DELETE FROM UsedTag;
DELETE FROM PhotoTag;
DELETE FROM TripTag;
DELETE FROM DestinationTag;
DELETE FROM Tag;
DELETE FROM TreasureHunt;
DELETE FROM DestinationPhoto;
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM DestinationTravellerTypePending;
DELETE FROM DestinationTravellerType;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM TravellerTypeDefinition;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM Photo;
DELETE FROM User;