-- !Ups

-- Add sample users for testing
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', false);

-- Create profiles for users
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (2, 'Steve', 'Jimmy', 'Alan', '1486-11-05', 'Female');

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(1, 'Russian Federation'),(2, 'Finland'),(3, 'Kazakhstan');

-- Add sample destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 48.8583, 2.2945, 1, 0);

-- Add sample treasure hunts, one the logged in user, the other not
INSERT INTO TreasureHunt (user_id, riddle, destination_id, start_date, end_date, deleted) VALUES (1, 'Your own Riddle', 1, '2031-05-29', '2031-07-12', false);
INSERT INTO TreasureHunt (user_id, riddle, destination_id, start_date, end_date, deleted) VALUES (2, 'A public Riddle', 1, '2031-05-29', '2031-07-12', false);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM TreasureHunt;
DELETE FROM Destination;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM User;