-- AUTHOR: Matthew Minish, William Wallace, Ollie Sharplin, what about me?
-- MODIFIED: 9/7/2019 2.00PM

-- !Ups

-- Create User table
CREATE TABLE IF NOT EXISTS User
  (
    id                INT NOT NULL AUTO_INCREMENT,
    username          VARCHAR(64) NOT NULL,
    password          VARCHAR(128) NOT NULL,
    salt              VARCHAR(64) NOT NULL,
    admin             BOOLEAN NOT NULL DEFAULT false,
    deleted           BOOLEAN NOT NULL DEFAULT false,
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

-- Create the country definition table, which is static and defines all possible countries
CREATE TABLE IF NOT EXISTS CountryDefinition
  (
    id                INT NOT NULL,
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
    deleted           BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
  );

-- Create DestinationTravellerType table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS DestinationTravellerType
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    dest_id           INT NOT NULL,
    traveller_type_definition_id INT NOT NULL,
    FOREIGN KEY (dest_id) REFERENCES Destination(id) ON DELETE CASCADE,
    FOREIGN KEY (traveller_type_definition_id) REFERENCES TravellerTypeDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX destinationtravellertype_index (dest_id, traveller_type_definition_id),
    UNIQUE(dest_id, traveller_type_definition_id)
  );

-- Create DestinationTravellerTypePending table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS DestinationTravellerTypePending
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    dest_id           INT NOT NULL,
    traveller_type_definition_id INT NOT NULL,
    FOREIGN KEY (dest_id) REFERENCES Destination(id) ON DELETE CASCADE,
    FOREIGN KEY (traveller_type_definition_id) REFERENCES TravellerTypeDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX destinationtravellertypepending_index (dest_id, traveller_type_definition_id),
    UNIQUE(dest_id, traveller_type_definition_id)
  );

-- Create Trip table, which maps trips to users
CREATE TABLE IF NOT EXISTS Trip
  (
    id                INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    is_public         BIT NOT NULL DEFAULT 0,
    deleted           BOOLEAN NOT NULL DEFAULT false,
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
    deleted               BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (photo_id) REFERENCES Photo(guid) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX Destination_photo_index (photo_id, destination_id),
    UNIQUE(photo_id, destination_id)
  );

-- Create treasure hunt table, which stores the riddle and dates or a treasure hunt about a destination
CREATE TABLE IF NOT EXISTS TreasureHunt
  (
    id                    INT NOT NULL AUTO_INCREMENT,
    user_id               INT NOT NULL,
    destination_id        INT NOT NULL,
    riddle                VARCHAR(1024) NOT NULL,
    start_date            DATE NOT NULL,
    end_date              DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
  );

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(643, 'Russian Federation'),(246, 'Finland'),(398, 'Kazakhstan'),(496, 'Mongolia');

-- Add sample user
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User(username, password, salt) VALUES ('testUser@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=');
INSERT INTO User(username, password, salt) VALUES ('testUser2@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=');

-- Add sample data for TravellerTypeDefinitions
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker'), ('Functional/Business Traveller'), ('Groupies'), ('Thrillseeker'), ('Frequent Weekender'), ('Gap Year');

-- Add sample Profile
INSERT INTO Profile(user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES
    (1, 'Moffat', 'the', 'Proffat', '1990-01-01', 'Male'),
    (2, 'Kermit', 'the', 'Frog', '1995-07-18', 'Female'),
    (3, 'William', 'the', 'Conqueror', '1969-012-24', 'Other');
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1,1), (1,3);
INSERT INTO Passport (user_id, country_id) VALUES (1,246);
INSERT INTO Nationality (user_id, country_id) VALUES (1,246), (1,643);

-- Add sample data for destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES
    (1, 'Ust-Tsilemsky Wilderness', 'Wilderness', 'Komi Republic', 66, 52.0, 643, 1),
    (1, 'Reka Oyvozh', 'River', 'Leshukonsky District', 64.5, 49.0, 643, 1),
    (1, 'Reka Palkin', 'River', 'Verkhnetoyemskiy Rayon', 63.0, 46.0, 643, 1),
    (1, 'Velsky Wilderness', 'Wilderness', 'Arkhangelsk Oblast', 61.5, 43.0, 643, 1),

    (1, 'Kaivulampi', 'Lake', 'Posio', 66, 28.0, 246, 1),
    (1, 'Goro Kostomuksha', 'Town', 'Republic of Karelia', 64.5, 31.0, 643, 1),
    (1, 'Reka Kumsa', 'River', 'Republic of Karelia', 63.0, 34.0, 643, 1),
    (1, 'Vytegorskiy Rayon Wilderness', 'Wilderness', 'Vologda Oblast', 61.5, 37.0, 643, 1),

    (1, 'Reka Kubena', 'River', 'Vologda Oblast', 60.0, 40.0, 643, 1),
    (1, 'Anan''ino', 'Town', 'Yaroslavl Oblast', 57.5, 40.0, 643, 1),
    (1, 'Malinovka', 'Town', 'Ryazan Oblast', 55.0, 40.0, 643, 1),
    (1, 'Gryazi', 'Town', 'Lipetsk Oblast', 52.5, 40.0, 643, 1),
    (1, 'Tserkov'' Spasa Preobrazheniya', 'Town', 'Voronezh Oblast', 50.0, 40.0, 643, 1),


    (1, 'Gorod Inta', 'Town', 'Komi Republic', 66.0, 60.0, 643, 1),
    (1, 'Ozero Shuryshkarskiy Sor', 'Lake', 'Yamalo-Nenets Autonomous Okrug', 66.0, 65.0, 643, 1),
    (1, 'Nadymsky Wilderness', 'Wilderness', 'Yamalo-Nenets Autonomous Okrug', 66.0, 70.0, 643, 1),
    (1, 'Nadymsky Wilderness', 'Wilderness', 'Yamalo-Nenets Autonomous Okrug', 66.0, 75.0, 643, 1),
    (1, 'Krasnoselkupsky Wilderness', 'Wilderness', 'Yamalo-Nenets Autonomous Okrug', 66.0, 80.0, 643, 1),

    (1, 'Nizhny Tagil', 'City', 'Sverdlovsk Oblast', 58.0, 60.0, 643, 1),
    (1, 'Reka Bol''shaya Zemlyanaya', 'River', 'Sverdlovsk Oblast', 58.0, 65.0, 643, 1),
    (1, 'Irtysh River', 'River', 'Tyumen Oblast', 58.0, 70.0, 643, 1),
    (1, 'Tarsky Wilderness', 'Wilderness', 'Omsk Oblast', 58.0, 75.0, 643, 1),

    (1, 'Karabutak', 'Town', 'Karabutak', 50.0, 60.0, 398, 1),
    (2, 'Amangeldi Wilderness', 'Wilderness', 'Amangeldi District', 50.0, 65.0, 398, 1),
    (1, 'Unnamed River', 'River', 'Nura District', 50.0, 70.0, 398, 1),
    (1, 'Karkaraly Wilderness', 'Wilderness', 'Karkaraly District', 50.0, 75.0, 398, 1),
    (1, 'Unnamed Riverside', 'Riverside', 'East Kazakhstan Province', 50.0, 80.0, 398, 1),

    (1, 'Beryozovsky Wilderness', 'Wilderness', 'Khanty-Mansi Autonomous Okrug', 63.4, 60.0, 643, 1),
    (1, 'Reka Talitsa', 'River', 'Sverdlovsk Oblast', 60.8, 60.0, 643, 1),
    (1, 'Gornoural''skiy Outer Limits', 'City Outer Limits', 'Sverdlovsk Oblast', 58.1, 60.0, 643, 1),
    (1, 'Reka Bol''shoy Kialim', 'River', 'Chelyabinsk Oblast', 55.4, 60.0, 643, 1),
    (1, 'Bredinsky Wasteland', 'Wasteland', 'Chelyabinsk Oblast', 52.7, 60.0, 643, 1),


    (1, 'Evenkiysky Wilderness', 'Wilderness', 'Krasnoyarsk Krai', 66.0, 90.0, 643, 1),
    (1, 'Evenkiysky Hills', 'Hills', 'Krasnoyarsk Krai', 66.0, 95.0, 643, 1),
    (1, 'Evenkiysky Marsh', 'Marsh', 'Krasnoyarsk Krai', 66.0, 100.0, 643, 1),
    (1, 'Evenkiysky Wilderness', 'Wilderness', 'Krasnoyarsk Krai', 66.0, 105.0, 643, 1),
    (1, 'Aykhal Outer Limits', 'City Outer Limits', 'Sakha Republic', 66.0, 110.0, 643, 1),

    (1, 'Yeniseysky Forest', 'Forest', 'Krasnoyarsk Krai', 58.0, 90.0, 643, 1),
    (1, 'Motyginsky Forest', 'Forest', 'Krasnoyarsk Krai', 58.0, 95.0, 643, 1),
    (1, 'Reka Tarasova-Chudova', 'River', 'Irkutsk Oblast', 58.0, 100.0, 643, 1),
    (1, 'Katangsky Farmland', 'Farmland', 'Irkutsk Oblast', 58.0, 105.0, 643, 1),

    (1, 'Reka Msgen-Buren', 'River', 'Tuva', 50.0, 90.0, 643, 1),
    (1, 'Altan Els Desert', 'Desert', 'Altan Els', 50.0, 95.0, 496, 1),
    (1, 'Alag-Erdene Wasteland', 'Wasteland', 'Alag-Erdene', 50.0, 100.0, 496, 1),
    (1, 'Tsagaannuur Riverbed', 'Riverbed', 'Tsagaannuur', 50.0, 105.0, 496, 1),
    (1, 'Krasnochikoyskiy Forest', 'Forest', 'Zabaykalsky Krai', 50.0, 110.0, 643, 1),

    (1, 'Turukhansky Forest', 'Forest', 'Krasnoyarsk Krai', 63.4, 90.0, 643, 1),
    (1, 'Turukhansky Dense Forest', 'Dense Forest', 'Krasnoyarsk Krai', 60.8, 90.0, 643, 1),
    (1, 'Reka Seredkina', 'River', 'Krasnoyarsk Krai', 58.1, 90.0, 643, 1),
    (1, 'Agrofirma Uchumskaya', 'Area', 'Krasnoyarsk Krai', 55.4, 90.0, 643, 1),
    (1, 'A161 Rest Area', 'Rest Area', 'Republic of Khakassia', 52.7, 90.0, 643, 1),


    (1, 'Tian Shan Mountain Range', 'Mountain Range', 'Sakha Republic', 66.0, 140.0, 643, 1),
    (1, 'Tian Shan Riverbed', 'Sakha Republic', 'Paris', 66.0, 145.0, 643, 1),
    (1, 'Verkhnekolymsky Hills', 'Hills', 'Sakha Republic', 66.0, 150.0, 643, 1),

    (1, 'Reka Dyanyshka', 'River', 'Sakha Republic', 66.0, 130.0, 643, 1),
    (1, 'Unnamed Lake', 'Lake', 'Sakha Republic', 66.0, 125.0, 643, 1),
    (1, 'Reka Allara-Sala', 'River', 'Sakha Republic', 66.0, 120.0, 643, 1),

    (1, 'Verkhoyansky Hills', 'Hills', 'Sakha Republic', 66.0, 135.0, 643, 1),
    (1, 'Unnamed Tomponskiy River', 'River', 'Sakha Republic', 63.4, 135.0, 643, 1),
    (1, 'Ust-Maysky Dirt Road', 'Road', 'Sakha Republic', 60.8, 135.0, 643, 1),
    (1, 'Ayano-Maysky Valley', 'Valley', 'Khabarovsk Krai', 58.1, 135.0, 643, 1),
    (1, 'Tuguro-Chumikansky Mountains', 'Mountains', 'Khabarovsk Krai', 55.4, 135.0, 643, 1),
    (1, 'Imeni Poliny Osipenko Mountains', 'Mountains', 'Khabarovsk Krai', 52.7, 135.0, 643, 1),
    (1, 'Khabarovsky Artic Forest', 'Forest', 'Khabarovsk Krai', 50.0, 135.0, 643, 1);


-- Add traveller type to destination
INSERT INTO DestinationTravellerType(dest_id, traveller_type_definition_id) VALUES (10, 3);

-- Add sample data for trip
INSERT INTO Trip (user_id) VALUES (1);

-- Add sample tripData for the sample trip
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 0, 1, NULL, NULL);

-- !Downs
DROP TABLE TreasureHunt;
DROP TABLE DestinationPhoto;
DROP TABLE Photo;
DROP TABLE DestinationTravellerType;
DROP TABLE DestinationTravellerTypePending;
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