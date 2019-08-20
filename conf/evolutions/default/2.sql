-- !Ups

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(643, 'Russian Federation'),(246, 'Finland'),(398, 'Kazakhstan'),(496, 'Mongolia');

-- Add sample user
INSERT INTO User(username, password, salt, admin) VALUES ('admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User(username, password, salt, admin) VALUES ('testUser@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', true);
INSERT INTO User(username, password, salt) VALUES ('testUser2@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=');

INSERT INTO User(username, password, salt, creation_date) VALUES
('saïd.koeiman@example.com', '7c77014100ae15283064279e72dff8f1be825ddb', '0cd848d2124ca1a793053a80f8b859fd446eaf0e', '2015-08-31T21:49:52Z'),
('eliott.joly@example.com', '72d628afcba6276ef21c659ca04551993677b447', '70dd5476cb1f2b844eaa10fc4ed910c96a51654e', '2004-07-05T11:59:46Z'),
('lucas.pelletier@example.com', '6b33a4dee55670bb643785ffc66c519ab5a0cb6e', '412149ff61673718efc7d5136b7d97734c9f5903', '2003-05-17T03:46:42Z'),
('farah.grønlie@example.com', 'f9d5f98af12c3d457e0aac3d237bb2fa69ba310d', '81ed6b673586cf3f79b3ed5ee777a2c6d3b095d1', '2007-08-15T23:27:49Z'),
('louise.schmidt@example.com', 'bcb58c91e18656345fafda2f2914264135091014', '0fd7db67d191529c54ffdbd9bd29518312198371', '2006-05-21T13:51:37Z'),
('ottmar.bergen@example.com', 'ac1cea310fc7b7e6f8c3105f9852e15ae3e6ce3e', '75eab187612304c287196d5fdffa1bd348d5163b', '2005-12-26T04:34:58Z'),
('benjamin.bienert@example.com', '1e115feeab9474b9d680e5528024201af6e7722f', '898caf13a62d1fe4d54b17513b0a76408eb444de', '2010-07-28T17:39:18Z'),
('eldirene.nascimento@example.com', '18a98c35f49808b45edadc75fb1b25ebfd4037d6', '108ccf64a8263e67741bcc1bdeb43af9d5f087ad', '2017-04-08T11:49:24Z'),
('bernd.dupont@example.com', 'f8fd5fa7675349b5c4bdd55d271ca94e845580cf', 'c4d00c26a98fa45e05b9d94364bcfc663a335b95', '2009-10-29T04:03:58Z'),
('ariane.grewal@example.com', 'a374df9df08b4948837d4c8049671f84f9d74bcd', 'cf1e2ff31ff7798e7aea597d3f6bf0aa774dbd4c', '2006-07-02T11:58:27Z'),
('ella.hansen@example.com', '3e6b71ea580132a61c1bcec211ef9615e7fd8a88', 'fa6a70debbe61538bdb0db518491748b7f800145', '2007-06-23T16:17:05Z'),
('halvor.pihl@example.com', '5e07e3a09df9cfee0ca0cc71c142cf5511535601', '00b8ade3febcae14c5adaf1ddd6a40947a1e5176', '2014-12-05T01:28:56Z'),
('maria.petersen@example.com', '3863c58e1809046ae6fc1bfce53c2b39697bfaf0', 'eba6dba69aeff32cd851d8afb8999813888a71cd', '2006-09-19T16:28:15Z'),
('ronald.hill@example.com', '977e877fed983f7d2bc7ce00989555f4372ae0c4', '45bcb9c5d802d3fea2f6028913f8e737e0acb651', '2016-09-10T16:24:35Z'),
('nihal.özdoğan@example.com', '8755e6091050fea8ce7f540d816bd0076dc80264', 'a094b0dfc0b057b8a22caaf512ee3e7e92e73af4', '2009-01-19T13:30:26Z'),
('jorge.campos@example.com', 'a4aed34f4966dc8688b8e67046bf8b276626e284', '4c8d7c31e731156ace99269d38b1ed6329c979f9', '2012-01-30T16:31:00Z'),
('sanni.neva@example.com', '400cb45d51731820c682f85d177e8ac10917c124', 'a1c7c6940b61dab66829ea46ae457c52142151e7', '2013-12-27T12:20:35Z'),
('emilia.bull@example.com', '23a0b5e4fb6c6e8280940920212ecd563859cb3c', 'dbdbbfde03ffb4fe9d62eeab0141c70bf0919da1', '2007-06-13T20:18:25Z'),
('hans-rudolf.rolland@example.com', 'd9d4b393c73d73fa13fd6f1f2ae8ccb6a90f1112', '3e69813441fa65ebb21cf42b0b28632cdbc063c9', '2003-04-07T07:04:36Z'),
('vildan.arıcan@example.com', 'f2ea4d54f2c839ae6895d8a2ccd15163f33983f9', 'a13cc4ac94e04b00e8907e2ff6f4c75deaf94222', '2008-10-24T16:30:14Z'),
('jimmie.johnston@example.com', 'a82548336cc8b6c0d33b9f012c054a5f68dfa527', '56c91a2854b20fb9b7db25598f5067a91753015a', '2004-08-07T11:59:50Z'),
('nathan.slawa@example.com', 'dc9ed0c98af68ca27c5dc3630a91a1ce44baa40c', '4a2235bc26c0599e6a3a1b6c4ecb8a43965e19a0', '2015-10-26T23:39:32Z'),
('david.gerard@example.com', 'e0d1a862d8f31af605ecef8c92857b8938ba622e', '36b33068ddbd167e9efa2ce976ee31df2f0d36a4', '2003-02-02T04:13:56Z'),
('irene.hale@example.com', 'f5929d41da4cbdc5f824e0573c96012bd6b997c3', 'cf3c5929cbfd5b9cb9ddb7b2d0960d24ad32337d', '2007-05-14T17:21:43Z'),
('giray.karaböcek@example.com', '79fbc1e56b538b0ac058616070bfa7ddb8a7d8db', '6872e77a4d9298242bdf1e0c77b087fb41dc44bb', '2008-01-20T09:08:06Z'),
('elenore.faller@example.com', '2b43fb8b7a234825d50dd49ce7892d78a59da8f3', '3bb0d580551facfa688ff79e20eb6c989a5f126b', '2002-04-23T04:33:03Z'),
('britney.king@example.com', '410223c695bc16f3e341f069e1cb6f81ef25db20', 'ca9973fd53c306d4c6d0b96a7953a04f468c3281', '2016-03-08T23:26:58Z'),
('piper.cooper@example.com', '9f38cb5f09df862f1ec8891616fd0f4980a8d4de', '7fb8b2a3ac6595a333e2a662ff3ad52c66c7bcba', '2005-08-14T10:42:43Z'),
('dave.cunningham@example.com', 'fa7c781f9469a8989eeb919d18930b16d241a266', 'ab96468c8d810071d449d656db53f65ef767c9eb', '2015-11-06T11:52:58Z'),
('ugne.tveita@example.com', '336c6d2cdd0cd62780ff7d675627a28718fc7be5', 'f60fc1103145dcd4f63702ca9f08ecec85ec522a', '2013-06-08T19:10:00Z'),
('leandra.mutlu@example.com', '4ec844dae165816ebad5cb5ed77840e2484047d6', 'b3cd66963fd966b70a289c8aa0980f4707d744db', '2017-10-07T10:51:41Z'),
('henk-jan.vanolffen@example.com', '0798ebecfb7bc4dfb9d0445c2fa030eb64e1fa1c', '11f9e1e6769f0c8e243b7e033c6219f334ac69d1', '2002-07-26T04:04:53Z'),
('marius.østervold@example.com', '2d115725bc6ffc0164d2ab5c3dbfc97371e3c8f9', '6d5236faa053eb875740a7ccd1761e4fcd9675f2', '2011-03-11T23:40:48Z'),
('angie.fuller@example.com', '2b0b4fb77523f35f1f09f723e46229a59cb29d62', '9142b3bf1c9ee729b5ced7af62abf70ea61166cc', '2010-11-19T16:14:13Z'),
('katie.holland@example.com', '98b9f3569789ee531b401291240ecdef0357a127', '1acdef68bd497edceede9c1964569df04b5b760e', '2005-05-30T22:00:22Z'),
('marius.kristensen@example.com', '82eeba1f2783a67ccb954e1fe51e2ee863084784', '3ea5b31f4cd34cbc4d8743455964f521ae3f791f', '2015-06-10T02:47:06Z'),
('boubker.stelwagen@example.com', 'd7966074b3d619b43ee1c6296ae5332c48d6cb1c', '19fbebac59c1a6b244c425c2fc6d9b577461cb48', '2007-10-23T10:30:00Z'),
('giani.moura@example.com', '2b225155eb9153b0925d57727fdbd3ab70a6c202', 'f3417a7a70783201801e90e9a5bef6d9d0f22944', '2015-05-30T04:21:11Z'),
('yanis.guillaume@example.com', 'e57a14bb5a3ccdc260d173d989d187d86d4aabfa', '7ec01ac91405e48bf8d00e8d0c605fcbf2de5c21', '2017-03-26T18:18:38Z'),
('mads.mortensen@example.com', 'be97b88037216671b1130cf7a605622a0119713e', 'dad5e72618e55c9505e9b101de1e35a0f3bd50df', '2007-09-22T16:22:36Z'),
('marinette.renard@example.com', '3a1ac43777d4009419d8c14c1600d0bf2611e3f2', '1aca27e11c1912463d4a62720930f0dfc323ee6b', '2004-11-06T11:47:46Z'),
('morris.harris@example.com', 'd5c73ea2a96bbc909d3187e583a318ea8957c144', '23556d62ac220823a4c35bb5c8284f38e72a981c', '2004-04-08T18:30:30Z'),
('isolino.silveira@example.com', 'e6cf9b33d6e9be2ab7b5e02b946075d6af6dcfbc', 'f9fd45d3d2e53fffa4bd12146595afc5a1ca662f', '2003-03-10T12:49:10Z'),
('marshall.stone@example.com', 'ec6ae2faaf89c1679bf5b9970ce0e80e46e8b12e', '118cbcf281ef2016b0ad2c5150b2100a71b54a30', '2016-07-03T07:48:03Z');

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
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 1, 2, NULL, NULL);
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 2, 3, NULL, NULL);

-- Add sample photos
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test2.jpeg', './public/storage/photos/test/thumbnails/test2.jpeg', 0, 0);

-- Add sample tags
INSERT INTO Tag (name) VALUES ('Russia'), ('sports'), ('#TravelEA');
INSERT INTO DestinationTag (tag_id, destination_id) VALUES (2, 1), (1, 1);
INSERT INTO TripTag (tag_id, trip_id) VALUES (3, 1);
INSERT INTO PhotoTag (tag_id, photo_id) VALUES (1, 1);
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