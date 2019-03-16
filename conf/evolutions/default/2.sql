-- !Ups

-- Add sample user
INSERT INTO User(username, password, salt) VALUES ('testUser@email.com', 'pass', 'salt');
INSERT INTO User(username, password, salt) VALUES ('testUser2@email.com', 'pass', 'salt');

-- Add sample data for country
INSERT INTO CountryDefinition (name) VALUES ('France'), ('England'), ('New Zealand'), ('Australia'), ('Germany'), ('United States');

-- Add sample data for TravellerTypeDefinitions
INSERT INTO TravellerTypeDefinition (description) VALUES ('backpacker'), ('functional/business traveller'), ('groupies'), ('thrillseeker'), ('frequent weekender'), ('gap year');

-- Add sample Profile
INSERT INTO Profile(user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'UserFirst', 'UserMiddle', 'UserLast', '1990-01-01', 'Male');
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1,1), (1,3);
INSERT INTO Passport (user_id, country_id) VALUES (1,1);
INSERT INTO Nationality (user_id, country_id) VALUES (1,1), (1,3);

-- Add sample data for destination
INSERT INTO Destination (name, type, district, latitude, longitude, country_id) VALUES
    ('Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1),
    ('Stonehenge', 'Monument', 'Salisbury', 20.0, 30.0, 2),
    ('Sky Tower', 'Monument', 'Auckland', 40.0, 50.0, 3),
    ('Sydney Opera House', 'Monument', 'Sydney', 50.0, 60.0, 4),
    ('Brandenburg Gate', 'Monument', 'Berlin', 60.0, 70.0, 5),
    ('Statue of Liberty', 'Monument', 'New York', 70.0, 80.0, 6);

-- Add sample data for trip
INSERT INTO Trip (user_id) VALUES (1);

-- Add sample tripData for the sample trip
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 0, 1, NULL, NULL);

-- !Downs
DELETE FROM TravellerType;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM TravellerTypeDefinition;
DELETE FROM TripData;
DELETE FROM Destination;
DELETE FROM Trip;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM User;