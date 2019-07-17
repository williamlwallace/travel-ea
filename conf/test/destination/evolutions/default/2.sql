-- !Ups

-- Insert a country for testing
INSERT INTO CountryDefinition (name) VALUES ('Test Country');

--Add traveller types for testing
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 1');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Ollie');

-- Add sample user for testing
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', false);

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (2, 'Steve', 'Jimmy', 'Alan', '1486-11-05', 'Female');

-- Insert some destinations to test getting
-- User 1 private destinations
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 48.8583, 2.2945, 1, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 48.8586, 2.2947, 1, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'The Eiffel Tower', 'Monument', 'Paris', 48.8586, 2.2947, 1, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Sky Tower', 'Monument', 'Auckland', -36.8484, 174.76000, 1, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Britomart Monument', 'Monument', 'Akaroa', -43.81546, 172.94883, 1, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'London Eye', 'Monument', 'London', 56.3453, 23.94883, 1, 0);

-- User 2 private destinations
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (2, 'Tower Bridge', 'Monument', 'London', 51.50333132, -0.071999712, 1, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (2, 'The Eiffel Tower', 'Monument', 'Paris', 48.8586, 2.2947, 1, 0);

-- Public destinations
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Public dest one', 'Monument', 'Paris', 48.8583, 2.2945, 1, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Public dest two', 'Monument', 'Paris', 48.8586, 2.2947, 1, 1);

-- Add photo
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, is_profile) VALUES (1, './public/storage/photos/test/test.jpeg', './public/storage/photos/test/thumbnails/test.jpeg', 1, 1);

-- Add photo-destination link that can be merged
INSERT INTO DestinationPhoto (photo_id, destination_id) VALUES (1, 2);

-- Add traveller types to destination
INSERT INTO DestinationTravellerType(dest_id, traveller_type_definition_id) VALUES (1, 1);
INSERT INTO DestinationTravellerType(dest_id, traveller_type_definition_id) VALUES (1, 3);
INSERT INTO DestinationTravellerTypePending(dest_id, traveller_type_definition_id) VALUES (1, 2);
INSERT INTO DestinationTravellerTypePending(dest_id, traveller_type_definition_id) VALUES (1, 1);

-- Insert a trip that uses destinations that will be merged
INSERT INTO Trip (user_id) VALUES (2);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES
  (1, 0, 4, null, null), (1, 1, 5, null, null), (1, 2, 2, null, null)

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM TripData;
DELETE FROM DestinationTravellerType;
DELETE FROM DestinationTravellerTypePending;
DELETE FROM Photo;
DELETE FROM DestinationPhoto;
DELETE FROM Destination;
DELETE FROM Profile;
DELETE FROM User;
DELETE FROM TravellerTypeDefinition;
DELETE FROM CountryDefinition;
