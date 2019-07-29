-- !Ups
-- Create test users
INSERT INTO User (username, password, salt) VALUES ('tester1@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt) VALUES ('tester2@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt) VALUES ('tester3@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt) VALUES ('tester4@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt) VALUES ('tester5@gmail.com', 'password', 'salt');


-- Insert traveller types for testing
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 1');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 3');

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(1, 'Russian Federation'),(2, 'Finland'),(3, 'Kazakhstan');

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (2, 'Steve', 'Jimmy', 'Alan', '1486-11-05', 'Female');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (3, 'Jim', 'Jimmy', 'Bob', '2001-11-05', 'Other');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (4, 'YA BOI', 'Jimmy', 'SKINNY P', '1997-10-19', 'Male');


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

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM TravellerType;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM Profile;
DELETE FROM CountryDefinition;
DELETE FROM TravellerTypeDefinition;
DELETE FROM User;
