import requests, hashlib, random, csv, re, os, json
from datetime import date, timedelta

"""
Script to generate example data for Travel EA database
The requests package needs to be installed with 'pip install requests'

Authors: Harrison Cook, William Wallace
"""

def setup_countries():
    """Gets the list of countries from restcountries.eu API and returns two
    colletions.

    Returns:
    country_insert -- a list of strings to be inserted into the database
    country_dict -- a dictionary of countries mapped from their ISO Alpha-2 code
                    to a tuple containing their name ISO numeric codes.
    """
    
    URL = "https://restcountries.eu/rest/v2/all"
    
    r = requests.get(url=URL)
    data = r.json()
    
    country_insert = ["INSERT IGNORE INTO CountryDefinition (id, name) VALUES\n"]
    country_dict = {}
    country_template = "({0}, '{1}'),\n"
    
    for country in data:
        numeric_code = country["numericCode"]
        # In case of countries like Kosovo, 
        # which don't have ISO numeric codes yet
        if not numeric_code == None: 
            name = country["name"].replace("'", "''") # Escape apostrophes
            
            country_dict[country["alpha2Code"]] = (numeric_code, name)
            country_insert.append(country_template.format(numeric_code, name))
    
    # Make the last comma a semi-colon instead, with a newline for formatting
    country_insert[-1] = country_insert[-1][:-2] + ";\n"
    return country_insert, country_dict


def generate_nationalities_and_passports(num_users, num_existing_users, 
                                         countries):
    """Generates a list of nationalities and/or passports to be inserted into
    the database.
    
    Keyword arguments:
    num_users -- number of users being created by the script
    num_existing_users -- number of existing users in the evolutions
    countries -- dictionary with values consisting of a tuple of the country
                 name and ISO numeric code
    
    Returns:
    values -- a list of strings to be added to the SQL script
    """
    
    values = []
    template = "({0}, {1}),\n"
    
    country_tuples = list(countries.values())
    
    for i in range(num_users):
        user_id = i + num_existing_users + 1 # +1 because SQL ids start from 1
        
         # Choose a random country and select it's id
        country_id = random.choice(country_tuples)[0]
        
        values.append(template.format(user_id, country_id))
    
    # Make the last comma a semi-colon instead, with a newline for formatting
    values[-1] = values[-1][:-2] + ";\n"
    return values

def generate_traveller_types(num_users, num_existing_users, num_traveller_types):
    """Generates a list of traveller types to be inserted into the database.
    
    Keyword arguments:
    num_users -- number of users being created by the script
    num_existing_users -- number of existing users in the evolutions
    num_traveller_types -- number of existing traveller types in the evolutions
    
    Returns:
    traveller_types -- a list of strings to be added to the SQL script
    """
    
    traveller_types = ["INSERT INTO TravellerType (user_id, traveller_type_id) VALUES\n"]
    traveller_type_templates = "({0}, {1}),\n"
    
    for i in range(num_users):
        user_id = i + num_existing_users + 1 # +1 because SQL ids start from 1
        
        used_traveller_types = set() # The traveller types this user has been given
        for j in range(random.randint(1, 3)): # Gives each user 1-3 traveller types
            
            traveller_type = random.randint(1, num_traveller_types)
            
            if not traveller_type in used_traveller_types:
                traveller_types.append(traveller_type_templates.format(user_id,
                                                                       traveller_type))
                used_traveller_types.add(traveller_type)
                
    # Make the last comma a semi-colon instead, with a newline for formatting
    traveller_types[-1] = traveller_types[-1][:-2] + ";\n"
    return traveller_types

def generate_users(num_users, num_existing_users, photos_filename):    
    """Generates a list of users and a list of profiles to be inserted into the
    database. Also writes a list of photos to be given to random users to a file.
    
    Keyword arguments:
    num_users -- number of users being created by the script
    num_existing_users -- number of existing users in the evolutions
    
    Returns:
    users -- a list of strings to be added to the SQL script
    profiles -- a list of strings to be added to the SQL script
    """

    URL = "https://randomuser.me/api/?results=" + str(num_users)
    r = requests.get(url=URL)
    data = r.json()["results"]
    
    usernames = set() # The usernames (emails) that have been used
    
    photos = {}
        
    users = ["INSERT INTO User(username, password, salt, creation_date) VALUES\n"]
    profiles = ["INSERT INTO Profile(user_id, first_name, last_name, date_of_birth, gender, creation_date) VALUES\n"]
    
    user_template = "('{0}', '{1}', '{2}', '{3}'),\n"
    profile_template = "({0}, '{1}', '{2}', '{3}', '{4}', '{5}'),\n"
    
    for i in range(len(data)):
        result = data[i]
        
        email = result["email"].replace("'", "''") # Escape apostrophes
        
        # If the email has been used already, add some random bits to it
        if email in usernames: 
            email = email + str(random.getrandbits(32)) 
        usernames.add(email)
        
        # This is so passwords and salts resemble roughly what they should
        password = hashlib.sha1((result["login"]["password"])
                                .encode("utf-8")).hexdigest()
        salt = result["login"]["sha1"]
        
        creation_date = result["registered"]["date"]
        creation_date = creation_date[:10] + " " + creation_date[11:-1]
        users.append(user_template.format(email, password, salt, creation_date))
        
        user_id = i + num_existing_users + 1 # +1 because SQL ids start from 1
        	
        # Escape apostrophes
        first_name = result["name"]["first"].capitalize().replace("'", "''")
        last_name = result["name"]["last"].capitalize().replace("'", "''")
        
        date_of_birth = result["dob"]["date"][:10]
        gender = result["gender"].capitalize()
        profiles.append(profile_template.format(user_id, first_name, last_name, date_of_birth, gender, creation_date))
        
        photo = result["picture"]["large"]
        photos.setdefault(photo, []).append(user_id)
    
    # For each photo, select 5 random users
    for key in photos:
        photos[key] = random.choices(photos[key], k=5)
        
    with open("photo_urls_and_users.json", 'w', newline='') as file:
        file.writelines(json.dumps(photos))
        
    # Make the last commas semi-codes instead, with a newline for formatting    
    users[-1] = users[-1][:-2] + ";\n"
    profiles[-1] = profiles[-1][:-2] + ";\n"
    
    return users, profiles

def generate_destinations(countries):
    """Generates a list of destinations to be inserted into the database.
    
    Keyword arguments:
    countries -- a dictionary with a tuple of country numeric codes and names
                  zmapped from the country's ISO alpha-2 code
    
    Returns:
    destinations -- a list of strings to be added to the SQL script
    destination_info -- a list of tuples containing the id of the country the 
                        destination references and the name of the destination
    """
    
    destinations = ["INSERT INTO Destination(user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES\n"]
    destination_info = []
    
    with open('Destinations.csv', newline='') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            # Replace all Kosovo codes with Serbia, since Kosovo doesn't have an
            # official ISO code yet
            if row[-1] == "XK":
                row[-1] = "RS" 
                
            # Add the name and country id to destination info
            destination_info.append((row[1], row[-1])) 
            
            # Change the country's ISO alpha-2 code to it's ISO numeric code
            row[-1] = countries[row[-1]][0]
            
            # Escape apostrophes and add quotes
            row[1] = "'" + row[1].replace("'", "''") + "'" 
            row[2] = "'" + row[2] + "'"
            row[3] = "'" + row[3].replace("'", "''") + "'"
            
            # Add is_public field
            row.append(random.randint(0, 1)) 
            
            # Convert the list to a string and add to destinations
            destinations.append("(" + ','.join(row) + "),\n")
    
    # Make the last commas semi-codes instead, with a newline for formatting        
    destinations[-1] = destinations[-1][:-2] + ";\n"
    return destinations, destination_info

def generate_trips(num_trips, num_users):
    """Generates a list of trips to be inserted into the database.
    
    Keyword arguments:
    num_users -- number of users being created by the script
    num_trips -- number of trips being created by the script
    
    Returns:
    trips -- a list of strings to be added to the SQL script
    """
    
    trips = ["INSERT INTO Trip (user_id, is_public) VALUES\n"]
    trip_template = "({0}, {1}),\n"
    
    for i in range(num_trips):
        user_id = random.randint(1, num_users)
        is_public = random.randint(0, 1)
        trips.append(trip_template.format(user_id, is_public))
    
    # Make the last commas semi-codes instead, with a newline for formatting        
    trips[-1] = trips[-1][:-2] + ";\n"
    return trips
    

def generate_trip_data(num_existing_trips, num_trips, num_destinations):   
    """Generates a list of tripData to be inserted into the database.
    
    Keyword arguments:
    num_existing_trips -- number of trips in the evolutions
    num_trips -- number of trips being created by the script
    num_destinations -- number of destinations being created by the script
    
    
    Returns:
    trip_data -- a list of strings to be added to the SQL script
    trip_destination_data -- a dictionary mapping a trip id to all the
                             destinations visited along the trip
    """
    
    trip_data = ["INSERT INTO TripData (trip_id, position, destination_id) VALUES\n"]
    trip_data_template = "({0}, {1}, {2}),\n"
    
    trip_destination_data = {}
    
    for i in range(num_trips):
        trip_id = num_existing_trips + i + 1 # +1 because SQL ids start from 1
        
        used_destinations = set() # The destinations this trip has been given
        
        for j in range(random.randint(2, 4)):
            position = j + 1 # +1 because SQL ids start from 1
            destination_id = random.randint(1, num_destinations)
            
            # Ensure that each destination is unique
            while destination_id in used_destinations:
                destination_id = random.randint(1, num_destinations)
                
            used_destinations.add(destination_id)
            
            trip_data.append(trip_data_template.format(trip_id, position, destination_id))
            trip_destination_data.setdefault(trip_id, []).append(destination_id)
    
    # Make the last commas semi-codes instead, with a newline for formatting        
    trip_data[-1] = trip_data[-1][:-2] + ";\n"
    return trip_data, trip_destination_data

def generate_treasure_hunts(num_treasure_hunts, num_users, num_destinations,
                            destination_info, country_dict, 
                            num_existing_destinations):
    """Generates a list of treasure hunts to be inserted into the database.
    
    Keyword arguments:
    num_treasure_hunts -- number of treasure hunts being created by the script
    num_users -- number of trips being created by the script
    num_destinations -- number of destinations being created by the script
    destination_info -- a list of tuples containing the id of the country the 
                        destination references and the name of the destination
    country_dict -- a dictionary with a tuple of country numeric codes and names
                    mapped from the country's ISO alpha-2 code
    num_existing_destinations -- number of existing destinations in the 
                                 evolutions
    
    Returns:
    treasure_hunts -- a list of strings to be added to the SQL script
    """
    
    treasure_hunts = ["INSERT INTO TreasureHunt(user_id, destination_id, riddle, start_date, end_date) VALUES\n"]
    treasure_hunt_template = "({0}, {1}, '{2}', '{3}', '{4}'),\n"
    
    for i in range(num_treasure_hunts):
        user_id = random.randint(1, num_users)
        destination_id = random.randint(1, num_destinations)
        destination_name = destination_info[destination_id - 1][0]
        
        # Create the riddle with escaped apostrophes
        # Riddle is in the form: "What is Chr_________? Which is in New Zealand
        riddle = "What is " +  destination_name[:3].replace("'", "''") + \
            re.sub("[a-z]", "_", destination_name[3:]).replace("'", "''") + "?" \
            + " Which is in " + \
            country_dict[destination_info[destination_id - 1][1]][1].replace("'", "''")
        
        # Generate semi-random start and end dates
        start_date_modifer = timedelta(days=random.randint(0, 100))
        start_date = str(date.today() + start_date_modifer)
        end_date = str(date.today() + start_date_modifer + timedelta(days=random.randint(5, 40)))
        
        treasure_hunts.append(treasure_hunt_template
                              .format(user_id, 
                                      destination_id + num_existing_destinations,
                                      riddle, start_date, end_date))
    
    # Make the last commas semi-codes instead, with a newline for formatting        
    treasure_hunts[-1] = treasure_hunts[-1][:-2] + ";\n"
    return treasure_hunts

def generate_tags(trip_destination_data, destination_info,
                  num_destinations, num_existing_destinations, 
                  existing_tags, country_dict):
    """Generates a list of tags, destination tags and trip tags to be inserted 
    into the database.
    
    Keyword arguments:
    trip_destination_data -- a dictionary mapping a trip id to all the
                             destinations visited along the trip
    destination_info -- a list of tuples containing the id of the country the 
                        destination references and the name of the destination
    num_destinations -- number of destinations being created by the script
    num_existing_destinations -- number of existing destinations in the 
                                 evolutions
    existing_tags -- number of existing tags in the evolutions
    country_dict -- a dictionary with a tuple of country numeric codes and names
                    mapped from the country's ISO alpha-2 code
    
    Returns:
    tags -- a list of strings to be added to the SQL script, which includes all
            three types of tags
    """    
    
    tags = ["INSERT INTO Tag(id, name) VALUES\n"]
    used_tags = existing_tags.copy()
    destination_tags = ["INSERT INTO DestinationTag(tag_id, destination_id) VALUES\n"]
    trip_tags = ["INSERT INTO TripTag(tag_id, trip_id) VALUES\n"]
    
    tag_template = "({0}, {1}),\n"
       
    current_tag_id = len(used_tags) + 1
    
    for i in range(num_destinations):
        destination_name = destination_info[i][0]
        country_name = country_dict[destination_info[i][1]][1]
        
        new_tags = set()
        new_tags.add(destination_name)
        new_tags.update(destination_name.split())
        new_tags.add(country_name)
        new_tags.update(country_name.split())
        
        for tag in new_tags:
            tag_id = current_tag_id
            
            # Escape apostrophes, and remove double escaped apostrophes
            # This can occur due to country names already being escaped
            tag = "'" + tag.replace("'", "''") + "'"
            tag = tag.replace("''''", "''")
            
            # Use an existing tag (id) if it exists, otherwise add a new one
            if tag in used_tags:
                tag_id = used_tags[tag]
            else:
                used_tags[tag] = tag_id
                current_tag_id += 1
                            
            destination_tags.append(tag_template.format(tag_id, i + num_existing_destinations + 1))
            
    for trip_id in trip_destination_data:
        # The list of destination ids along a trip
        trip_destinations = trip_destination_data[trip_id]
        
        new_tags = set()
        for destination in trip_destinations:
            dest_info = destination_info[destination - 1]
            destination_name = dest_info[0]
            country_name = country_dict[dest_info[1]][1]
            
            new_tags.add(destination_name)
            new_tags.add(country_name)
            
        for tag in new_tags:
            tag_id = current_tag_id
            
            # Escape apostrophes, and remove double escaped apostrophes
            # This can occur due to country names already being escaped            
            tag = "'" + tag.replace("'", "''") + "'"
            tag = tag.replace("''''", "''")            
            
            # Use an existing tag (id) if it exists, otherwise add a new one            
            if tag in used_tags:
                tag_id = used_tags[tag]
            else:
    
                used_tags[tag] = tag_id
                current_tag_id += 1
            
            trip_tags.append(tag_template.format(tag_id, trip_id))        
    
    # Remove existing tags so they are not inserted again
    for existing_tag in existing_tags:
        used_tags.pop(existing_tag)
    
    for tag_name, tag_id in used_tags.items():
        tags.append(tag_template.format(tag_id, tag_name))
        
    
    # Make the last commas semi-codes instead, with a newline for formatting            
    tags[-1] = tags[-1][:-2] + ";\n"
    destination_tags[-1] = destination_tags[-1][:-2] + ";\n"
    trip_tags[-1] = trip_tags[-1][:-2] + ";\n"
    
    # Merge the three tag lists together
    tags.extend(destination_tags)
    tags.extend(trip_tags)
    return tags
    
    
def main():
    """Main function which collates data and writes it to a evolutions sql file
    """
    
    # Data existing in the evolutions already
    num_existing_users = 3
    num_existing_traveller_types = 7
    num_existing_trips = 1
    num_existing_destinations = 64
    existing_tags = {"'Russia'": 1, "'sports'": 2, "'#TravelEA'": 3}
    
    # New data
    num_users = 5000 # Max 5000
    num_trips = 500
    num_destinations = 1000 # Max 1000
    num_treasure_hunts = 200
    evolutions_num = "3"
    
    photos_filename = "photo_urls_and_users.json"    
    
    print("Generating countries...")
    country_insert, country_dict = setup_countries()
    
    print("Done\nGenerating users and profiles...")
    users, profiles = generate_users(num_users, num_existing_users, photos_filename)
    
    print("Done\nGenerating nationalities and passports...")
    values = generate_nationalities_and_passports(num_users, num_existing_users,
                                                  country_dict)
    
    print("Done\nGenerating traveller types...")
    traveller_types = generate_traveller_types(num_users, num_existing_users,
                                               num_existing_traveller_types)
    
    print("Done\nGenerating destinations...")
    destinations, destination_info = generate_destinations(country_dict)
    
    print("Done\nGenerating trips...")
    trips = generate_trips(num_trips, num_users)
    
    print("Done\nGenerating trip data...")
    trip_data, trip_destination_data = generate_trip_data(num_existing_trips, 
                                                          num_trips, 
                                                          num_destinations)
    
    print("Done\nGenerating tags...")
    tags = generate_tags(trip_destination_data, destination_info, 
                         num_destinations, num_existing_destinations, 
                         existing_tags, country_dict)
    
    print("Done\nGenerating treasure hunts...")
    treasure_hunts = generate_treasure_hunts(num_treasure_hunts, num_users,
                                             num_destinations, destination_info,
                                             country_dict, num_existing_destinations)
    
    print("Done\nGeneration complete!\nPopulating file...")
    
    filename = "../../conf/evolutions/default/" + evolutions_num + ".sql"
    
    drops = """DELETE FROM TripTag;
DELETE FROM DestinationTag;
DELETE FROM Tag;
DELETE FROM TreasureHunt;
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM Profile;
DELETE FROM User;
DELETE FROM CountryDefinition;"""
    
    # Wipes existing example data files
    if os.path.exists(filename):
        os.remove(filename) 
    
    # Write all the generated data to a SQL file
    with open(filename, "a", newline="") as file:
        file.writelines(["-- !Ups\n"])
        file.writelines(country_insert)
        file.writelines(users)
        file.writelines(profiles)
        file.writelines(["INSERT INTO Nationality (user_id, country_id) VALUES\n"])
        file.writelines(values)
        file.writelines(["INSERT INTO Passport (user_id, country_id) VALUES\n"])
        file.writelines(values)
        file.writelines(traveller_types)
        file.writelines(destinations)
        file.writelines(trips)
        file.writelines(trip_data)
        file.writelines(treasure_hunts)
        file.writelines(tags)
        file.writelines(["\n-- !Downs\n" + drops])
    
    print("Complete!")

if __name__ == "__main__":
    main()
