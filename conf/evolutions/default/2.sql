-- !Ups

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
  (643, 'Russian Federation'), (246, 'Finland'), (398, 'Kazakhstan'), (496, 'Mongolia'), (764, 'Thailand'), (826, 'United Kingdom'), (250, 'France'), (784, 'United Arab Emirates'),
  (702, 'Singapore'), (840, 'United States of America'), (458, 'Malaysia'), (392, 'Japan'), (792, 'Turkey'), (410, 'South Korea'), (682, 'Saudi Arabia'), (344, 'Hong Kong'),
  (484, 'Mexico'), (348, 'Hungary'), (032, 'Argentina'), (710, 'South Africa'), (158, 'Taiwan'), (036, 'Australia'), (554, 'New Zealand'), (208, 'Denmark'), (276, 'Germany'),
  (818, 'Egypt'), (380, 'Italy'), (203, 'Czech Republic'), (630, 'Puerto Rico'), (124, 'Canada'), (076, 'Brazil'), (156, 'China'), (524, 'Nepal');

-- Add sample user
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User(username, password, salt) VALUES ('testUser@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=');
INSERT INTO User(username, password, salt) VALUES ('testUser2@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=');

-- Add sample data for TravellerTypeDefinitions
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker'), ('Luxury'), ('Functional/Business Traveller'), ('Groupies'), ('Thrillseeker'), ('Frequent Weekender'), ('Gap Year');

-- Add sample Profile
INSERT INTO Profile(user_id, first_name, middle_name, last_name, date_of_birth, gender, creation_date) VALUES
    (1, 'Moffat', 'the', 'Proffat', '1990-01-01', 'Male', '2001-01-01'),
    (2, 'Kermit', 'the', 'Frog', '1995-07-18', 'Female', '2002-01-02'),
    (3, 'William', 'the', 'Conqueror', '1969-012-24', 'Other', '2003-01-03');
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1,1), (1,3), (2,2), (3,4);
INSERT INTO Passport (user_id, country_id) VALUES (1,246);
INSERT INTO Nationality (user_id, country_id) VALUES (1,246), (1,643), (2, 246), (3, 643);

-- Add sample data for destination
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES
    (1, 'Bangkok', 'City', 'Central Thailand', 13.7, 100.5, 764, 1),
    (1, 'London', 'City', 'London Region', 51.3, 0.0, 826, 1),
    (1, 'Paris', 'City', 'Île-de-France', 48.8, 2.4, 250, 1),
    (1, 'Dubai', 'City', 'Dubai Region', 25.3, 55.3, 784, 1),
    (1, 'Singapore', 'City', 'Central Region', 1.3, 103.8, 702, 1),
    (1, 'New York City', 'City', 'New York', 40.6, -73.8, 840, 1),
    (1, 'Kuala Lumpur', 'City', 'Selangor', 3.1, 101.6, 458, 1),
    (1, 'Tokyo', 'City', 'Kanto', 35.6, 139.6, 392, 1),
    (1, 'Istanbul', 'City', 'Marmara', 41.0, 28.9, 792, 1),
    (1, 'Seoul', 'City', 'Seoul Capital Area', 37.5, 126.9, 410, 1),
    (1, 'Phuket', 'City', 'Phuket Province', 8.0, 98.3, 764, 1),
    (1, 'Mecca', 'City', 'Hejazi', 21.4, 39.8, 682, 1),
    (1, 'Hong Kong', 'City', 'Southern China', 22.5, 114.3, 344, 1),
    (1, 'San Antonia', 'City', 'Texas', 29.4, -98.5, 840, 1),
    (1, 'Pueblo', 'City', 'East Central Mexico', 19.0, -97.9, 484, 1),
    (1, 'Honolulu', 'City', 'Hawaii', 21.3, -157.8, 840, 1),
    (1, 'Budapest', 'City', 'Central Hungary', 47.5, 19.0, 348, 1),
    (1, 'Buenos Aires', 'City', 'Buenos Aires', -34.6, -58.4, 032, 1),
    (1, 'Cape Town', 'City', 'Western Cape', -33.9, 18.4, 710, 1),
    (1, 'Moscow', 'City', 'Central Russia', 55.8, 37.7, 643, 1),
    (1, 'Taipei', 'City', 'Northern Taiwan', 25.0, 121.5, 158, 1),

    (1, 'Sydney', 'City', 'New South Wales', -33.9, 151.1, 036, 1),
    (1, 'Gold Coast', 'City', 'Queensland', -28.0, 153.4, 036, 1),
    (2, 'Melbourne', 'City', 'Victoria', -37.8, 145.0, 036, 1),
    (1, 'Perth', 'City', 'Western Australia', -32.0, 115.9, 036, 1),
    (1, 'Brisbane', 'City', 'Queensland', -27.5, 153.0, 036, 1),
    (1, 'Adelaide', 'City', 'South Australia', -34.9, 138.6, 036, 1),

    (1, 'Auckland', 'City', 'Auckland', -36.9, 174.8, 554, 1),
    (1, 'Tauranga', 'City', 'Bay of Plenty', -37.8, 176.1, 554, 1),
    (1, 'Mount Maunganui', 'Mountain', 'Tauranga', -37.6, 176.2, 554, 1),
    (1, 'Rotorua', 'City', 'Bay of Plenty', -38.0, 176.2, 554, 1),
    (1, 'Wellington', 'City', 'Wellington', -41.3, 174.8, 554, 1),
    (1, 'Christchurch', 'City', 'Canterbury', -43.5, 172.7, 554, 1),
    (1, 'Dunedin', 'City', 'Otago', -45.9, 170.5, 554, 1),
    (1, 'Wanaka', 'Town', 'Otago', -44.8, 169.1, 554, 1),
    (1, 'Queenstown', 'Town', 'Otago', -45.0, 168.7, 554, 1),
    (1, 'Hamilton', 'City', 'Waikato', -37.8, 175.3, 554, 1),

    (1, 'Miami', 'City', 'Florida', 25.8, -80.1, 840, 1),
    (1, 'Copenhagen', 'City', 'Copenhagen Region', 55.7, 12.5, 208, 1),
    (1, 'Berlin', 'City', 'Brandenburg', 52.5, 13.4, 276, 1),
    (1, 'Cairo', 'City', 'Cairo Region', 30.0, 31.2, 818, 1),
    (1, 'Great Pyramid of Giza', 'Monument', 'Cairo Region', 30.0, 31.1, 818, 1),
    (1, 'Grand Canyon', 'Canyon', 'Arizona', 36.3, -112.6, 840, 1),
    (1, 'Venice', 'City', 'Veneto', 45.4, 12.3, 380, 1),
    (1, 'Rome', 'City', 'Lazio', 41.9, 12.5, 380, 1),
    (1, 'Prague', 'City', 'Prague Region', 50.0, 14.4, 203, 1),
    (1, 'San Juan', 'City', 'San Juan Region', 18.4, -66.0, 630, 1),
    (1, 'Dublin', 'City', 'Leinster', 53.4, -6.2, 826, 1),
    (1, 'Toronto', 'City', 'Ontario', 43.7, -79.4, 124, 1),
    (1, 'Rio de Janeiro', 'City', 'Rio de Janeiro', -25.9, -43.1, 076, 1),
    (1, 'Las Vegas', 'City', 'Nevada', 36.1, -115.1, 840, 1),

    (1, 'Eiffel Tower', 'Monument', 'Île-de-France', 48.8, 2.3, 250, 1),
    (1, 'Hollywood Sign', 'Landmark', 'Los Angeles', 34.1, -118.3, 840, 1),
    (1, 'Mount Fuji', 'Volcano', 'Honshu', 35.4, 138.7, 392, 1),
    (1, 'Big Ben', 'Tower', 'London', 51.1, -0.2, 826, 1),
    (1, 'Burj Khalifa', 'Skyscraper', 'Dubai', 25.1, 55.3, 784, 1),
    (1, 'Colosseum', 'Monument', 'Rome', 41.9, 12.5, 380, 1),
    (1, 'Times Square', 'Landmark', 'New York', 40.8, -74.0, 840, 1),
    (1, 'Buckingham Palace', 'Palace', 'London', 51.5014, -0.1419, 826, 1),
    (1, 'Leaning Tower of Pisa', 'Tower', 'Italy', 43.7230, 10.3966, 380, 1),
    (1, 'Forbidden City', 'Landmark', 'Beijing', 39.9169, 116.3907, 156, 1),
    (1, 'Sydney Opera House', 'Landmark', 'Sydney', -33.8568, 151.2153, 036, 1),
    (1, 'Mount Everest', 'Mountain', 'Himalayas', 27.9881, 86.9250, 524, 1),
    (1, 'Great Wall of China', 'Landmark', 'China', 40.4319, 116.5704, 156, 1);


-- Add traveller type to destination
INSERT INTO DestinationTravellerType(dest_id, traveller_type_definition_id) VALUES (10, 3);

-- Add sample data for trip
INSERT INTO Trip (user_id, is_public) VALUES (1, true);
INSERT INTO Trip (user_id, is_public) VALUES (1, true);

-- Add sample tripData for the sample trip
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 0, 1, NULL, NULL);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 1, 2, NULL, NULL);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 2, 3, NULL, NULL);

INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (2, 0, 4, NULL, NULL);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (2, 1, 5, NULL, NULL);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (2, 2, 6, NULL, NULL);

-- Add sample tags
INSERT INTO Tag (name) VALUES ('Russia'), ('sports'), ('#TravelEA');
INSERT INTO DestinationTag (tag_id, destination_id) VALUES (2, 1), (1, 1);
INSERT INTO TripTag (tag_id, trip_id) VALUES (3, 1);
INSERT INTO UsedTag (tag_id, user_id) VALUES (3, 1), (2, 1), (1, 1), (2, 2);

-- !Downs
DELETE FROM UsedTag;
DELETE FROM PhotoTag;
DELETE FROM TripTag;
DELETE FROM DestinationTag;
DELETE FROM Tag;
DELETE FROM TreasureHunt;
DELETE FROM PendingDestinationPhoto;
DELETE FROM DestinationPhoto;
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM DestinationTravellerTypePending;
DELETE FROM DestinationTravellerType;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM TravellerTypeDefinition;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM Photo;
DELETE FROM User;