-- !Ups

-- Create test users
INSERT INTO User (username, password, salt) VALUES ('tester1@gmail.com', 'password', 'salt');

-- Add sample data for country
INSERT INTO CountryDefinition (name) VALUES ('France'), ('England'), ('New Zealand'), ('Australia'), ('Germany'), ('United States');

-- Insert some destinations
INSERT INTO Destination (name, type, district, latitude, longitude, country_id) VALUES
    ('Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1),
    ('Stonehenge', 'Monument', 'Salisbury', 20.0, 30.0, 2),
    ('Sky Tower', 'Monument', 'Auckland', 40.0, 50.0, 3),
    ('Sydney Opera House', 'Monument', 'Sydney', 50.0, 60.0, 4),
    ('Brandenburg Gate', 'Monument', 'Berlin', 60.0, 70.0, 5),
    ('Statue of Liberty', 'Monument', 'New York', 70.0, 80.0, 6);

-- Insert a trip for test user
INSERT INTO Trip (user_id) VALUES (1);

-- Insert some trip data into trips
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 0, 1, NULL, NULL);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 1, 2, NULL, NULL);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 2, 3, NULL, NULL);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM Destination;
DELETE FROM CountryDefinition;
DELETE FROM User;