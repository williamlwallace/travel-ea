-- !Ups

-- Insert a country for testing
INSERT INTO CountryDefinition (name) VALUES ('Test Country');

-- Add sample user for testing
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);

-- Insert a destination to test getting
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM Destination;
DELETE FROM User;
DELETE FROM CountryDefinition;
