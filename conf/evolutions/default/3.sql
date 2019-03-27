-- !Ups

-- Add sample user
INSERT INTO User(username, password, salt) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=');
INSERT INTO User(username, password, salt) VALUES ('testUser@email.com', 'pass', 'salt');
INSERT INTO User(username, password, salt) VALUES ('testUser2@email.com', 'pass', 'salt');

-- Add sample data for TravellerTypeDefinitions
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker'), ('Functional / Business traveller'), ('Groupies'), ('Thrill-seeker'), ('Frequent weekender'), ('Gap year');

-- Add sample Profile
INSERT INTO Profile(user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES ((SELECT id FROM User LIMIT 1), 'UserFirst', 'UserMiddle', 'UserLast', '1990-01-01', 'Male');
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES ((SELECT id FROM User LIMIT 1),(SELECT id FROM CountryDefinition LIMIT 1)), ((SELECT id FROM User LIMIT 1),(SELECT id FROM CountryDefinition LIMIT 1) + 2);
INSERT INTO Passport (user_id, country_id) VALUES ((SELECT id FROM User LIMIT 1),(SELECT id FROM CountryDefinition LIMIT 1) );
INSERT INTO Nationality (user_id, country_id) VALUES ((SELECT id FROM User LIMIT 1),(SELECT id FROM CountryDefinition LIMIT 1) + 5), ((SELECT id FROM User LIMIT 1),(SELECT id FROM CountryDefinition LIMIT 1) + 2);

-- Add sample data for destination
INSERT INTO Destination (name, type, district, latitude, longitude, country_id) VALUES
    ('Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, (SELECT id FROM CountryDefinition LIMIT 1)),
    ('Stonehenge', 'Monument', 'Salisbury', 20.0, 30.0, (SELECT id FROM CountryDefinition LIMIT 1) + 1),
    ('Sky Tower', 'Monument', 'Auckland', 40.0, 50.0, (SELECT id FROM CountryDefinition LIMIT 1) + 2),
    ('Sydney Opera House', 'Monument', 'Sydney', 50.0, 60.0, (SELECT id FROM CountryDefinition LIMIT 1) + 3),
    ('Brandenburg Gate', 'Monument', 'Berlin', 60.0, 70.0, (SELECT id FROM CountryDefinition LIMIT 1) + 4),
    ('Statue of Liberty', 'Monument', 'New York', 70.0, 80.0, (SELECT id FROM CountryDefinition LIMIT 1) + 5);

-- Add sample data for trip
INSERT INTO Trip (user_id) VALUES ((SELECT id FROM User LIMIT 1));

-- Add sample tripData for the sample trip
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES ((SELECT id FROM Trip LIMIT 1), 0, (SELECT id FROM Destination LIMIT 1), NULL, NULL);

-- !Downs
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM Destination;
DELETE FROM Nationality;
DELETE FROM Passport;
DELETE FROM TravellerType;
DELETE FROM Profile;
DELETE FROM TravellerTypeDefinition;
DELETE FROM CountryDefinition;
DELETE FROM User;
