-- AUTHOR: Matthew Minish, William Wallace, what about me?
-- MODIFIED: 14/3/2019 2.00PM

-- !Ups

-- Create User table
CREATE TABLE IF NOT EXISTS User
  (
    id                INT NOT NULL AUTO_INCREMENT,
    username          VARCHAR(64) NOT NULL,
    password          VARCHAR(128) NOT NULL,
    salt              VARCHAR(64) NOT NULL,
    admin             BOOLEAN NOT NULL DEFAULT false,
    creation_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username)
  );

-- Create Profile table
CREATE TABLE IF NOT EXISTS Profile
  (
    user_id           INT NOT NULL AUTO_INCREMENT,
    first_name        VARCHAR(64) NOT NULL,
    middle_name       VARCHAR(64),
    last_name         VARCHAR(64) NOT NULL,
    date_of_birth     DATE,
    gender            VARCHAR(32),
    creation_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS CountryDefinition
  (
    id                INT NOT NULL AUTO_INCREMENT,
    name              VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    INDEX name_index (name)
  );

-- Create Nationality table, which specifies nationalities for users
CREATE TABLE IF NOT EXISTS Nationality
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    country_id        INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX nationality_index (user_id, country_id),
    UNIQUE (user_id, country_id)
  );

-- Create Passport table, which specifies passports of users
CREATE TABLE IF NOT EXISTS Passport
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    country_id        INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX passport_index (user_id, country_id),
    UNIQUE (user_id, country_id)
  );

-- Create the traveller type definitions table (as above, static-ish table with all possible values)
CREATE TABLE IF NOT EXISTS TravellerTypeDefinition
  (
    id                INT NOT NULL AUTO_INCREMENT,
    description       VARCHAR(2048) NOT NULL,
    PRIMARY KEY (id)
  );

-- Create TravellerType table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS TravellerType
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    traveller_type_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (traveller_type_id) REFERENCES TravellerTypeDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX travellertype_index (user_id, traveller_type_id),
    UNIQUE(user_id, traveller_type_id)
  );

-- Create Destination table
CREATE TABLE IF NOT EXISTS Destination
  (
    id                INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL, -- The owner of the destination, moved to master admin when public
    name              VARCHAR(128) NOT NULL,
    type              VARCHAR(128) NOT NULL, -- We may want to make a separate table which stores these
    district          VARCHAR(128) NOT NULL,
    latitude          DOUBLE NOT NULL,
    longitude         DOUBLE NOT NULL,
    country_id        INT NOT NULL,
    is_public         BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
  );

-- Create DestinationTravellerType table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS DestinationTravellerType
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    dest_id           INT NOT NULL,
    traveller_type_id INT NOT NULL,
    is_pending        BIT NOT NULL,
    FOREIGN KEY (dest_id) REFERENCES Destination(id) ON DELETE CASCADE,
    FOREIGN KEY (traveller_type_id) REFERENCES TravellerTypeDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX travellertype_index (dest_id, traveller_type_id),
    UNIQUE(dest_id, traveller_type_id)
  );

-- Create Trip table, which maps trips to users
CREATE TABLE IF NOT EXISTS Trip
  (
    id                INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    is_public         BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    PRIMARY KEY (id),
    INDEX user_id_index (user_id)
  );

-- Create TripData table, which stores the actual data (i.e destinations, times, etc.) of all trips
CREATE TABLE IF NOT EXISTS TripData
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    trip_id           INT NOT NULL,
    position          INT NOT NULL,
    destination_id    INT NOT NULL,
    arrival_time      DATETIME,
    departure_time    DATETIME,
    FOREIGN KEY (trip_id) REFERENCES Trip(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX tripdata_index (trip_id, position),
    INDEX destination_id_index (destination_id)
  );

-- Create Photo table, which stores the filenames and details for all photos
CREATE TABLE IF NOT EXISTS Photo
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    user_id               INT NOT NULL,
    filename              VARCHAR(256) NOT NULL,
    thumbnail_filename    VARCHAR(256) NOT NULL,
    is_public             BOOLEAN NOT NULL,
    uploaded              DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_profile            BOOLEAN NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    PRIMARY KEY (guid)
  );

    -- Create DestinationPhotos table, which specifies the photos of a Destinations
CREATE TABLE IF NOT EXISTS DestinationPhoto
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    photo_id              INT NOT NULL,
    destination_id        INT NOT NULL,
    FOREIGN KEY (photo_id) REFERENCES Photo(guid) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX Destination_photo_index (photo_id, destination_id),
    UNIQUE(photo_id, destination_id)
  );


-- Add countries
INSERT INTO CountryDefinition (name) VALUES
('France'),('England'),('New Zealand'),('Australia'),('Germany'),('United States');

-- Add sample user
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User(username, password, salt) VALUES ('testUser@email.com', 'pass', 'salt');
INSERT INTO User(username, password, salt) VALUES ('testUser2@email.com', 'pass', 'salt');

-- Add sample data for TravellerTypeDefinitions
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker'), ('Functional/Business Traveller'), ('Groupies'), ('Thrillseeker'), ('Frequent Weekender'), ('Gap Year');

-- Add sample Profile
INSERT INTO Profile(user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'UserFirst', 'UserMiddle', 'UserLast', '1990-01-01', 'Male');
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1,1), (1,3);
INSERT INTO Passport (user_id, country_id) VALUES (1,1);
INSERT INTO Nationality (user_id, country_id) VALUES (1,1), (1,3);

-- Add sample data for destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES
    (1, 'Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1, 1),
    (1, 'Stonehenge', 'Monument', 'Salisbury', 20.0, 30.0, 2, 1),
    (1, 'Sky Tower', 'Monument', 'Auckland', 40.0, 50.0, 3, 1),
    (1, 'Sydney Opera House', 'Monument', 'Sydney', 50.0, 60.0, 4, 1),
    (1, 'Brandenburg Gate', 'Monument', 'Berlin', 60.0, 70.0, 5, 1),
    (1, 'Statue of Liberty', 'Monument', 'New York', 70.0, 80.0, 6, 1);

-- Add sample data for trip
INSERT INTO Trip (user_id) VALUES (1);

-- Add sample tripData for the sample trip
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 0, 1, NULL, NULL);

-- !Downs
DROP TABLE DestinationPhoto;
DROP TABLE Photo;
DROP TABLE DestinationTravellerType
DROP TABLE TravellerType;
DROP TABLE Passport;
DROP TABLE Nationality;
DROP TABLE TravellerTypeDefinition;
DROP TABLE TripData;
DROP TABLE Destination;
DROP TABLE Trip;
DROP TABLE CountryDefinition;
DROP TABLE Profile;
DROP TABLE User;