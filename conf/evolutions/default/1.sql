-- AUTHOR: Matthew Minish
-- CREATED: 23/2/2019 5.30pm
-- USAGE: To execute this file, open sqlite DB file via command line (e.g sqlite3 team302.db) then execute .read <this-filename>

# --- !Ups

-- Create User table
CREATE TABLE IF NOT EXISTS User
  (
    uid INTEGER PRIMARY KEY AUTOINCREMENT,
    username          TEXT,
    password          TEXT,
    salt              TEXT
  );

-- Create Profile table
CREATE TABLE IF NOT EXISTS Profile
  (
    uid               INTEGER PRIMARY KEY,
    firstName         TEXT,
    middleName        TEXT,
    lastName          TEXT,
    dateOfBirth       DATE,
    gender            TEXT,
    FOREIGN KEY(uid) REFERENCES User(uid)
  );

-- Create Nationality table, which specifies nationalities for users
CREATE TABLE IF NOT EXISTS Nationality
  (
    uid               INTEGER,
    countryId         INTEGER,
    FOREIGN KEY(uid) REFERENCES User(uid),
    FOREIGN KEY(countryId) references CountryDefinition(id),
    PRIMARY KEY(uid, countryId)
  );

-- Create Passport table, which specifies passports of users
CREATE TABLE IF NOT EXISTS Passport
  (
    GUID              INT NOT NULL AUTO_INCREMENT,
    uid               INTEGER,
    countryId         INTEGER,
    FOREIGN KEY(uid) REFERENCES User(uid),
    FOREIGN KEY(countryId) references CountryDefinition(id),
    PRIMARY KEY(uid, countryId)
  );

-- Create the traveller type definitions table (as above, static-ish table with all possible values)
CREATE TABLE IF NOT EXISTS TravellerTypeDefinition
  (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    description       TEXT
  );

-- Create TravellerType table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS TravellerType
  (
    uid               INTEGER,
    travellerTypeId   INTEGER,
    FOREIGN KEY(uid) REFERENCES User(uid),
    FOREIGN KEY(travellerTypeId) REFERENCES TravellerTypeDefinition(id),
    PRIMARY KEY(uid, travellerTypeId)
  );

-- Create Destination table
CREATE TABLE IF NOT EXISTS Destination(
id                INTEGER PRIMARY KEY AUTOINCREMENT,
name              TEXT,
type              TEXT, -- We may want to make a separate table which stores these
district          TEXT,
latitude          DOUBLE,
longitude         DOUBLE,
countryId         INTEGER,
FOREIGN KEY(countryId) REFERENCES CountryDefinition(id)
);

-- Create Trip table, which maps trips to users
CREATE TABLE IF NOT EXISTS Trip(
id                INTEGER PRIMARY KEY AUTOINCREMENT,
uid               INTEGER,
FOREIGN KEY(uid) REFERENCES User(uid)
);

-- Create TripData table, which stores the actual data (i.e destinations, times, etc.) of all trips
CREATE TABLE IF NOT EXISTS TripData(
tripId            INTEGER,
position          INTEGER,
destinationId     INTEGER,
arrivalTime       DATETIME,
departureTime     DATETIME,
FOREIGN KEY(tripID) REFERENCES Trip(id),
FOREIGN KEY(destinationId) REFERENCES Destination(id),
PRIMARY KEY(tripId, position)
);

CREATE TABLE IF NOT EXISTS CountryDefinition(
id                INTEGER PRIMARY KEY,
name              TEXT
);

-- Add sample user
INSERT INTO User(username, password, salt) VALUES ('testUser@email.com', 'pass', 'salt');
INSERT INTO User(username, password, salt) VALUES ('testUser2@email.com', 'pass', 'salt');

-- Add sample data for country
INSERT INTO CountryDefinition (name) VALUES ('France'), ('England'), ('New Zealand'), ('Australia'), ('Germany'), ('United States');

-- Add sample data for TravellerTypeDefinitions
INSERT INTO TravellerTypeDefinition (description) VALUES ('backpacker'), ('functional/business traveller'), ('groupies'), ('thrillseeker'), ('frequent weekender'), ('gap year');

-- Add sample Profile
INSERT INTO Profile(uid, firstName, middleName, lastName, dateOfBirth, gender) VALUES (1, 'UserFirst', "UserMiddle", "UserLast", "1990-01-01", "Male");
INSERT INTO TravellerType (uid, travellerTypeId) VALUES (1,1), (1,3);
INSERT INTO Passport (uid, countryId) VALUES (1,1);
INSERT INTO Nationality (uid, countryId) VALUES (1,1), (1,3);

-- Add sample data for destination
INSERT INTO Destination (name, type, district, latitude, longitude, countryId) VALUES
    ('Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1),
    ('Stonehenge', 'Monument', 'Salisbury', 20.0, 30.0, 2),
    ('Sky Tower', 'Monument', 'Auckland', 40.0, 50.0, 3),
    ('Sydney Opera House', 'Monument', 'Sydney', 50.0, 60.0, 4),
    ('Brandenburg Gate', 'Monument', 'Berlin', 60.0, 70.0, 5),
    ('Statue of Liberty', 'Monument', 'New York', 70.0, 80.0, 6);

-- Add sample data for trip
INSERT INTO Trip (uid) VALUES (1);

-- Add sample tripData for the sample trip
INSERT INTO TripData (tripId, position, destinationId, arrivalTime, departureTime) VALUES (1, 0, 1, NULL, NULL);

-- !Downs
DROP TABLE User;
DROP TABLE Profile;
DROP TABLE Nationality;
DROP TABLE TravellerTypeDefinition;
DROP TABLE TravellerType;
DROP TABLE Destination;
DROP TABLE Trip;
DROP TABLE TripData;
