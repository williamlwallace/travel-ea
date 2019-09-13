-- !Ups
-- Create test users
INSERT INTO User (username, password, salt, admin) VALUES ('dave@gmail.com', 'kI9dTQEMsmcbqxn9SBk/jUDHNz7dOBWg/rxxE2xv3cE=', 'L9vI0DLY0cmnLrXrPNKe81IHvGw5NpZ5DgxMcuAkoh4=', 1);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', 'password', 'salt', 0);
INSERT INTO User (username, password, salt) VALUES ('tester3@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt) VALUES ('tester4@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt) VALUES ('tester5@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt, deleted) VALUES ('tester6@gmail.com', 'password', 'salt', 1);


-- Insert traveller types for testing
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 1');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 3');

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(1, 'Russian Federation'),(2, 'Finland'),(3, 'Kazakhstan');

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender, creation_date) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male', '2003-01-01');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender, creation_date) VALUES (2, 'Steve', 'Jimmy', 'Alan', '1486-11-05', 'Female', '2002-01-01');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender, creation_date) VALUES (3, 'Jim', 'Jimmy', 'Bob', '2001-11-05', 'Other', '2001-01-01');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender, creation_date) VALUES (4, 'YA BOI', 'Jimmy', 'SKINNY P', '1997-10-19', 'Male', '2000-01-01');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender, creation_date, deleted) VALUES (6, 'YA BOI 2', 'Jimmy', 'SKINNY P', '1997-10-19', 'Male', '2000-01-01', 1);

-- Add nationalities to profile
INSERT INTO Nationality (user_id, country_id) VALUES (1, 1);
INSERT INTO Nationality (user_id, country_id) VALUES (1, 2);
INSERT INTO Nationality (user_id, country_id) VALUES (2, 1);
INSERT INTO Nationality (user_id, country_id) VALUES (3, 2);
INSERT INTO Nationality (user_id, country_id) VALUES (4, 2);

-- Add passports to profile
INSERT INTO Passport (user_id, country_id) values (1, 1);
INSERT INTO Passport (user_id, country_id) values (1, 2);
INSERT INTO Passport (user_id, country_id) values (2, 1);
INSERT INTO Passport (user_id, country_id) values (3, 2);
INSERT INTO Passport (user_id, country_id) values (4, 1);

-- Add Traveller types to profile
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1, 1);
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1, 2);
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (2, 1);
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (3, 2);
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (4, 1);

-- Add followers to profiles
INSERT INTO FollowerUser (user_id, follower_id) VALUES (1, 2);
INSERT INTO FollowerUser (user_id, follower_id) VALUES (1, 3);
INSERT INTO FollowerUser (user_id, follower_id) VALUES (2, 1);

-- !Downs
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