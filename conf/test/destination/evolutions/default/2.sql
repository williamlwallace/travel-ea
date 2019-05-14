-- !Ups

-- Insert a country for testing
INSERT INTO CountryDefinition (name) VALUES ('Test Country');

-- Add sample user for testing
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', 'password', 'salt', 1);

-- Insert a destination to test getting
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 48.8583, 2.2945, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (2, 'Eiffel Tower', 'Monument', 'Paris', 48.8586, 2.2947, 0);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (2, 'The Eiffel Tower', 'Monument', 'Paris', 48.8586, 2.2947, 0);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM Destination;
DELETE FROM User;
DELETE FROM CountryDefinition;
