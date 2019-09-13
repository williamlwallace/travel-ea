-- !Ups

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES (1, 'Russian Federation'),(2, 'Finland'),(3, 'Kazakhstan');

INSERT INTO CountryDefinition (id, name) VALUES (496, 'Mongolia');

-- Add traveller types for testing
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

-- Deleted destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public, deleted) VALUES (1, 'Deleted dest one', 'Oops', 'Oh no', 0, 0, 1, 1, true);


-- Add photo
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test.jpeg', './public/storage/photos/test/thumbnails/test.jpeg', 1, 1);
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test.jpeg', './public/storage/photos/test/thumbnails/test.jpeg', 1, 1);
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test.jpeg', './public/storage/photos/test/thumbnails/test.jpeg', 1, 1);

-- Add photo-destination link that can be merged
INSERT INTO DestinationPhoto (photo_id, destination_id) VALUES (1, 2);
INSERT INTO PendingDestinationPhoto (photo_guid, dest_id) VALUES (2, 2);
INSERT INTO PendingDestinationPhoto (photo_guid, dest_id) VALUES (1, 2);


-- Add traveller types to destination
INSERT INTO DestinationTravellerType(dest_id, traveller_type_definition_id) VALUES (1, 1);
INSERT INTO DestinationTravellerType(dest_id, traveller_type_definition_id) VALUES (1, 3);
INSERT INTO DestinationTravellerTypePending(dest_id, traveller_type_definition_id) VALUES (1, 2);
INSERT INTO DestinationTravellerTypePending(dest_id, traveller_type_definition_id) VALUES (1, 1);

-- Insert a trip that uses destinations that will be merged
INSERT INTO Trip (user_id) VALUES (2);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES
  (1, 0, 4, null, null), (1, 1, 5, null, null), (1, 2, 2, null, null);

-- Add sample tags
INSERT INTO Tag (name) VALUES ('NZ'), ('sports'), ('music');
INSERT INTO DestinationTag (tag_id, destination_id) VALUES (2, 1), (2, 4), (3, 1);

INSERT INTO NewsFeedEvent (user_id, dest_id, event_type, ref_id, created) VALUES (1, null, 'NEW_PROFILE_PHOTO', 1, '2019-09-01 00:00:00');
INSERT INTO NewsFeedEvent (user_id, dest_id, event_type, ref_id, created) VALUES (2, null, 'NEW_PROFILE_PHOTO', 2, '2019-09-01 00:00:01');
INSERT INTO NewsFeedEvent (user_id, dest_id, event_type, ref_id, created) VALUES (2, 2, 'LINK_DESTINATION_PHOTO', 3, '2019-09-01 00:00:02');

-- Add following of a destination for testing
INSERT INTO FollowerDestination(destination_id, follower_id) VALUES (9, 2);

-- !Downs
DELETE FROM NewsFeedEvent;
DELETE FROM UsedTag;
DELETE FROM PhotoTag;
DELETE FROM TripTag;
DELETE FROM DestinationTag;
DELETE FROM Tag;
DELETE FROM TreasureHunt;
DELETE FROM PendingDestinationPhoto;
DELETE FROM DestinationPhoto;
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM DestinationTravellerTypePending;
DELETE FROM DestinationTravellerType;
DELETE FROM FollowerDestination;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM TravellerTypeDefinition;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM Photo;
DELETE FROM User;