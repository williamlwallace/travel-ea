-- !Ups

-- Insert users for testing
INSERT INTO User (username, password, salt, admin) VALUES ('dave@gmail.com', 'kI9dTQEMsmcbqxn9SBk/jUDHNz7dOBWg/rxxE2xv3cE=', 'L9vI0DLY0cmnLrXrPNKe81IHvGw5NpZ5DgxMcuAkoh4=', 1);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', 'password', 'salt', 0);
INSERT INTO User (username, password, salt, admin, deleted) VALUES ('deleted@gmail.com', 'n0t4p455w0rd', 's4lt', 0, true);

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (2, 'Steve', 'Jimmy', 'Alan', '1486-11-05', 'Female');

-- Add sample tags
INSERT INTO Tag (name) VALUES ('Russia'), ('sports'), ('#TravelEA');
INSERT INTO UsedTag (tag_id, user_id) VALUES (3, 1), (2, 1), (1, 1), (2, 2);

-- Add a follower to first user
INSERT INTO FollowerUser (user_id, follower_id) VALUES (1,2);

-- !Downs
-- Now delete all rows from tables (DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE)
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
DELETE FROM FollowerUser;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM TravellerTypeDefinition;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM Photo;
DELETE FROM User;