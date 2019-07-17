-- !Ups

-- Add sample users for testing
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', false);

-- Create profiles for users
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (2, 'Steve', 'Jimmy', 'Alan', '1486-11-05', 'Female');

-- Add default country definition to base destinations on
INSERT INTO CountryDefinition (name) VALUES ('Test Country');

-- Add sample destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 48.8583, 2.2945, 1, 0);

-- Add sample treasure hunt
-- Owned by admin
INSERT INTO TreasureHunt (user_id, destination_id, riddle, start_date, end_date) VALUES (1, 1, 'Big pointy thing in Paris', '2000-01-01 00:00:00.0', '2030-01-01 00:00:00.0');
-- Not owned by admin
INSERT INTO TreasureHunt (user_id, destination_id, riddle, start_date, end_date) VALUES (2, 1, 'Also found in Paris, Texas', '2000-01-01 00:00:00.0', '2030-01-01 00:00:00.0');

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM Destination;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM User;