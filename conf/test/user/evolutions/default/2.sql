-- !Ups

-- Insert a user for testing
INSERT INTO User (username, password, salt) VALUES ('dave@gmail.com', 'ByT98//U0kAJsniaZyDXOsm7p4/3ALXUAs1Y9lsIyo0=', '5w8mhT42p9uS2f716RLMQTG8r+/+nfeMbwQGBpGqMao=');

-- !Downs
-- Now delete all rows from tables
DELETE FROM User;