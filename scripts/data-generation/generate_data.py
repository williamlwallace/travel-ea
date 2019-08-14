import requests, hashlib, random, csv, re, os
from datetime import date, timedelta

"""
Script to generate example data for Travel EA database
Authors: Harrison Cook, William Wallace
"""

def setup_countries():
    """ Gets the list of countries from restcountries.eu API and returns a
    dictionary of countries mapped from their ISO Alpha-2 code to a tuple of
    their numeric codes and name."""
    URL = "https://restcountries.eu/rest/v2/all"
    
    r = requests.get(url=URL)
    data = r.json()
    
    country_insert = ["INSERT IGNORE INTO CountryDefinition (id, name) VALUES\n"]
    country_dict = {}
    country_template = "({0}, '{1}'),\n"
    
    for country in data:
        numeric_code = country["numericCode"]
        if not numeric_code == None:
            name = country["name"].replace("'", "''")
            country_dict[country["alpha2Code"]] = (numeric_code, name)
            country_insert.append(country_template.format(numeric_code, name))
        
    country_insert[-1] = country_insert[-1][:-2] + ";\n"
    return country_insert, country_dict


def generate_nationalities_and_passports(num_users, num_existing_users, countries):
    values = []
    template = "({0}, {1}),\n"
    
    country_tuples = list(countries.values())
    
    for i in range(num_users):
        user_id = i + num_existing_users + 1
        country_id = random.choice(country_tuples)[0]
        
        values.append(template.format(user_id, country_id))
        
    values[-1] = values[-1][:-2] + ";\n"
    return values

def generate_traveller_types(num_users, num_existing_users, num_traveller_types):
    traveller_types = ["INSERT INTO TravellerType (user_id, traveller_type_id) VALUES\n"]
    traveller_type_templates = "({0}, {1}),\n"
    
    for i in range(num_users):
        user_id = i + num_existing_users + 1
        
        used_traveller_types = set()
        for j in range(random.randint(1, 3)):
            traveller_type = random.randint(1, num_traveller_types)
            if not traveller_type in used_traveller_types:
                traveller_types.append(traveller_type_templates.format(user_id, traveller_type))
                used_traveller_types.add(traveller_type)
    
    traveller_types[-1] = traveller_types[-1][:-2] + ";\n"
    return traveller_types

def generate_users(num_existing_users, num_users):
    URL = "https://randomuser.me/api/?results=" + str(num_users)
    
    r = requests.get(url=URL)
    data = r.json()["results"]
    
    usernames = set()
        
    users = ["INSERT INTO User(username, password, salt, creation_date) VALUES\n"]
    profiles = ["INSERT INTO Profile(user_id, first_name, last_name, date_of_birth, gender, creation_date) VALUES\n"]
    
    user_template = "('{0}', '{1}', '{2}', '{3}'),\n"
    profile_template = "({0}, '{1}', '{2}', '{3}', '{4}', '{5}'),\n"
    
    for i in range(len(data)):
        result = data[i]
        
        email = result["email"].replace("'", "''")
        if email in usernames:
            email = email + str(random.getrandbits(32))
        usernames.add(email)
        password = hashlib.sha1((result["login"]["password"]).encode("utf-8")).hexdigest()
        salt = result["login"]["sha1"]
        creation_date  = result["registered"]["date"]
        users.append(user_template.format(email, password, salt, creation_date))
        
        user_id = i + num_existing_users + 1
        first_name = result["name"]["first"].capitalize().replace("'", "''")
        last_name = result["name"]["last"].capitalize().replace("'", "''")
        date_of_birth = result["dob"]["date"][:10]
        gender = result["gender"].capitalize()
        profiles.append(profile_template.format(user_id, first_name, last_name, date_of_birth, gender, creation_date))
    
    users[-1] = users[-1][:-2] + ";\n"
    profiles[-1] = profiles[-1][:-2] + ";\n"
    return users, profiles

#Need to insert used countries
def generate_destinations(countries):
    destinations = ["INSERT INTO Destination(user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES\n"]
    destination_info = []
    
    with open('Destinations.csv', newline='') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            if row[-1] == "XK":
                row[-1] = "RS" 
            destination_info.append((row[1], row[-1])) #Add the name and country id to destination info
            
            row[-1] = countries[row[-1]][0]
            
            row[1] = "'" + row[1].replace("'", "''") + "'" #Escape apostrophes in name and add quotes
            row[2] = "'" + row[2] + "'" #Add quotes to type
            row[3] = "'" + row[3].replace("'", "''") + "'" #Add quotes to district
            
            row.append("'" + str(random.randint(0, 1)) + "'") #Add is_public
            
            destinations.append("(" + ','.join(row) + "),\n") #Convert the list to a string and add to destinations
    
    destinations[-1] = destinations[-1][:-2] + ";\n"
    return destinations, destination_info

def generate_trips(num_trips, num_users):
    trips = ["INSERT INTO Trip (user_id, is_public) VALUES\n"]
    trip_template = "({0}, {1}),\n"
    
    for i in range(num_trips):
        user_id = random.randint(1, num_users)
        is_public = random.randint(0, 1)
        trips.append(trip_template.format(user_id, is_public))
        
    trips[-1] = trips[-1][:-2] + ";\n"
    return trips
    

def generate_trip_data(num_existing_trips, num_trips, num_destinations):
    trip_data = ["INSERT INTO TripData (trip_id, position, destination_id) VALUES\n"]
    trip_data_template = "({0}, {1}, {2}),\n"
    
    trip_destination_data = {}
    
    for i in range(num_trips):
        trip_id = num_existing_trips + i + 1
        
        used_destinations = set()
        
        for j in range(random.randint(1, 4)):
            position = j + 1
            destination_id = random.randint(1, num_destinations)
            while destination_id in used_destinations:
                destination_id = random.randint(1, num_destinations)
            used_destinations.add(destination_id)
            
            trip_data.append(trip_data_template.format(trip_id, position, destination_id))
            trip_destination_data.setdefault(trip_id, []).append(destination_id)
            
    trip_data[-1] = trip_data[-1][:-2] + ";\n"
    return trip_data, trip_destination_data

def generate_treasure_hunts(num_treasure_hunts, num_users, num_destinations, destination_info, country_dict, num_existing_destinations):
    treasure_hunts = ["INSERT INTO TreasureHunt(user_id, destination_id, riddle, start_date, end_date) VALUES\n"]
    treasure_hunt_template = "({0}, {1}, '{2}', '{3}', '{4}'),\n"
    
    for i in range(num_treasure_hunts):
        user_id = random.randint(1, num_users)
        destination_id = random.randint(1, num_destinations)
        destination_name = destination_info[destination_id - 1][0]
        riddle = "What is " +  destination_name[:3].replace("'", "''") + \
            re.sub("[a-z]", "_", destination_name[3:]).replace("'", "''") + "?" \
            + " Which is in " + country_dict[destination_info[destination_id - 1][1]][1].replace("'", "''")
        start_date_modifer = timedelta(days=random.randint(0, 100))
        start_date = str(date.today() + start_date_modifer)
        end_date = str(date.today() + start_date_modifer + timedelta(days=random.randint(5, 40)))
        
        treasure_hunts.append(treasure_hunt_template.format(user_id, destination_id + num_existing_destinations, riddle, start_date, end_date))
    
    treasure_hunts[-1] = treasure_hunts[-1][:-2] + ";\n"
    
    return treasure_hunts

def generate_tags(trip_destination_data, destination_info,
                  num_destinations, num_existing_destinations, 
                  existing_tags, country_dict):
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
            
            tag = "'" + tag.replace("'", "''") + "'"
            tag = tag.replace("''''", "''")
            
            if tag in used_tags:
                tag_id = used_tags[tag]
            else:
                used_tags[tag] = tag_id
                current_tag_id += 1
                
            #print((tag_id, used_tags[tag], tag))
            
            destination_tags.append(tag_template.format(tag_id, i + num_existing_destinations + 1))
            
    for trip_id in trip_destination_data:
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
            tag = "'" + tag.replace("'", "''") + "'"
            tag = tag.replace("''''", "''")            
            
            if tag in used_tags:
                tag_id = used_tags[tag]
            else:
    
                used_tags[tag] = tag_id
                current_tag_id += 1
            
            trip_tags.append(tag_template.format(tag_id, trip_id))        
    
    
    for existing_tag in existing_tags:
        used_tags.pop(existing_tag)
    
    for tag_name, tag_id in used_tags.items():
        tags.append(tag_template.format(tag_id, tag_name))
        
        
    tags[-1] = tags[-1][:-2] + ";\n"
    destination_tags[-1] = destination_tags[-1][:-2] + ";\n"
    trip_tags[-1] = trip_tags[-1][:-2] + ";\n"
    
    tags.extend(destination_tags)
    tags.extend(trip_tags)
    return tags
    
    
def main():
    
    num_existing_users = 3
    num_existing_traveller_types = 7
    num_existing_trips = 1
    num_existing_destinations = 64
    existing_tags = {"'Russia'": 1, "'sports'": 2, "'#TravelEA'": 3}
    num_users = 5000 #Max 5000
    num_trips = 500
    num_destinations = 1000 #Max 1000
    num_treasure_hunts = 200
    evolutions_num = "3"
    
    print("Generating countries...")
    country_insert, country_dict = setup_countries()
    
    print("Done\nGenerating users and profiles...")
    users, profiles = generate_users(num_existing_users, num_users)
    
    print("Done\nGenerating nationalities and passports...")
    values = generate_nationalities_and_passports(num_users, num_existing_users, country_dict)
    
    print("Done\nGenerating traveller types...")
    traveller_types = generate_traveller_types(num_users, num_existing_users, num_existing_traveller_types)
    
    print("Done\nGenerating destinations...")
    destinations, destination_info = generate_destinations(country_dict)
    
    print("Done\nGenerating trips...")
    trips = generate_trips(num_trips, num_users)
    
    print("Done\nGenerating trip data...")
    trip_data, trip_destination_data = generate_trip_data(num_existing_trips, num_trips, num_destinations)
    
    print("Done\nGenerating tags...")
    tags = generate_tags(trip_destination_data, destination_info, num_destinations, num_existing_destinations, existing_tags, country_dict)
    
    print("Done\nGenerating treasure hunts...")
    treasure_hunts = generate_treasure_hunts(num_treasure_hunts, num_users, num_destinations, destination_info, country_dict, num_existing_destinations)
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
    
    #Wipes existing example data file
    if os.path.exists(filename):
        os.remove(filename)    
    
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
