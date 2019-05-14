-- !Ups

-- Insert a country for testing
INSERT INTO CountryDefinition (name) VALUES ('Test Country');

-- Insert a destination to test getting
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM Destination;
DELETE FROM CountryDefinition;
