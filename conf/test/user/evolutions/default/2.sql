-- !Ups

-- Insert a user for testing
INSERT INTO User (username, password, salt, admin) VALUES ('dave@gmail.com', 'kI9dTQEMsmcbqxn9SBk/jUDHNz7dOBWg/rxxE2xv3cE=', 'L9vI0DLY0cmnLrXrPNKe81IHvGw5NpZ5DgxMcuAkoh4=', 1);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', 'password', 'salt', 1);

-- !Downs
-- Now delete all rows from tables
DELETE FROM User;