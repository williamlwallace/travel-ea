-- !Ups
-- Create test users
INSERT INTO User (username, password, salt, admin) VALUES ('dave@gmail.com', 'kI9dTQEMsmcbqxn9SBk/jUDHNz7dOBWg/rxxE2xv3cE=', 'L9vI0DLY0cmnLrXrPNKe81IHvGw5NpZ5DgxMcuAkoh4=', 1);
INSERT INTO User (username, password, salt, admin) VALUES ('tester1@gmail.com', 'password', 'salt', 1);
INSERT INTO User (username, password, salt, admin) VALUES ('tester2@gmail.com', 'password', 'salt', 1);

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');

-- Insert countries for testing
INSERT INTO CountryDefinition (name) VALUES ('France');
INSERT INTO CountryDefinition (name) VALUES ('New Zealand');

-- Create 2 test destinations for trips
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Sky Tower', 'Monument', 'Auckland', -36.8484, 174.76000, 2);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Britomart Monument', 'Monument', 'Akaroa', -43.81546, 172.94883, 2);

-- Create 2 Trips for testing
INSERT INTO Trip (user_id) VALUES (1);
INSERT INTO Trip (user_id) VALUES (2);

-- Add destinations to the Trips
-- Trip 1
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 0, 1,'2019-04-12 13:59:00', '2019-04-13 08:00:00');
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 1, 2,'2019-04-14 13:59:00', '2019-04-16 08:00:00');
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 2, 3,'2019-04-17 13:59:00', '2019-04-18 08:00:00');
-- Trip 2
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (2, 2, 1,'2019-04-22 13:59:00', '2019-04-23 08:00:00');
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (2, 1, 2,'2019-04-25 13:59:00', '2019-04-26 08:00:00');

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM Destination;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM User;