import requests, hashlib, random, csv, re, os
from datetime import date, timedelta

def setup_countries():
    URL = "https://restcountries.eu/rest/v2/all"
    
    r = requests.get(url=URL)
    data = r.json()
    
    country_insert = ["INSERT IGNORE INTO CountryDefinition (id, name) VALUES\n"]
    country_dict = {}
    country_template = "({0}, '{1}'),\n"
    
    for country in data:
        numeric_code = country["numericCode"]
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
        
    users = ["INSERT INTO User(username, password, salt, creation_date) VALUES\n"]
    profiles = ["INSERT INTO Profile(user_id, first_name, last_name, date_of_birth, gender, creation_date) VALUES\n"]
    
    user_template = "('{0}', '{1}', '{2}', '{3}'),\n"
    profile_template = "({0}, '{1}', '{2}', '{3}', '{4}', '{5}'),\n"
    
    for i in range(len(data)):
        result = data[i]
        
        email = result["email"].replace("'", "''")
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
    destination_names = []
    
    with open('Destinations_100.csv', newline='') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            row[-1] = countries[row[-1]][0]
            if row[-1] is None:
                row[-1] = countries["RS"][0] 
            destination_names.append(row[1])
            
            row[1] = "'" + row[1].replace("'", "''") + "'"
            row[2] = "'" + row[2] + "'"
            row[3] = "'" + row[3].replace("'", "''") + "'"
            row.append("0")
            
            
            destinations.append("(" + ','.join(row) + "),\n")
    
    destinations[-1] = destinations[-1][:-2] + ";\n"
    return destinations, destination_names

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
            
    trip_data[-1] = trip_data[-1][:-2] + ";\n"
    return trip_data

def generate_treasure_hunts(num_treasure_hunts, num_users, num_destinations, destination_names):
    treasure_hunts = ["INSERT INTO TreasureHunt(user_id, destination_id, riddle, start_date, end_date) VALUES\n"]
    treasure_hunt_template = "({0}, {1}, '{2}', '{3}', '{4}'),\n"
    
    for i in range(num_treasure_hunts):
        user_id = random.randint(1, num_users)
        destination_id = random.randint(1, num_destinations)
        destination_name = destination_names[destination_id - 1]
        riddle = "Where is " +  destination_name[:3] + \
            re.sub("[a-z]", "_", destination_name[3:]) + "?"
        start_date_modifer = timedelta(days=random.randint(0, 100))
        start_date = str(date.today() + start_date_modifer)
        end_date = str(date.today() + start_date_modifer + timedelta(days=random.randint(5, 40)))
        
        treasure_hunts.append(treasure_hunt_template.format(user_id, destination_id, 
                                                            riddle, start_date, end_date))
    
    treasure_hunts[-1] = treasure_hunts[-1][:-2] + ";\n"
    
    return treasure_hunts

def main():
    
    num_existing_users = 3
    num_existing_traveller_types = 7
    num_existing_trips = 1
    num_users = 100 #Will create 2x this amount, max 5000
    num_trips = 500
    num_destinations = 100 #Max 1000
    num_treasure_hunts = 200
    evolutions_num = "3"
    
    print("Generating countries...")
    country_insert, country_dict = setup_countries()
    
    print("Done\nGenerating users and profiles...")
    users, profiles = generate_users(num_existing_users, num_users)
    #users2, profiles2 = generate_users(num_existing_users, num_users)
    
    print("Done\nGenerating nationalities and passports...")
    values = generate_nationalities_and_passports(num_users, num_existing_users, country_dict)
    
    print("Done\nGenerating traveller types...")
    traveller_types = generate_traveller_types(num_users, num_existing_users, num_existing_traveller_types)
    
    print("Done\nGenerating destinations...")
    destinations, destination_names = generate_destinations(country_dict)
    
    print("Done\nGenerating trips...")
    trips = generate_trips(num_trips, num_users)
    
    print("Done\nGenerating trip data...")
    trip_data = generate_trip_data(num_existing_trips, num_trips, num_destinations)
    
    print("Done\nGenerating treasure hunts...")
    treasure_hunts = generate_treasure_hunts(num_treasure_hunts, num_users, num_destinations, destination_names)
    print("Done\nGeneration complete!\nPopulating file...")
    
    filename = "../../conf/evolutions/default/" + evolutions_num + ".sql"
    
    drops = """DELETE FROM TreasureHunt;
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
        #file.writelines(users2)
        file.writelines(profiles)
        #file.writelines(profiles2)
        file.writelines(["INSERT INTO Nationality (user_id, country_id) VALUES\n"])
        file.writelines(values)
        file.writelines(["INSERT INTO Passport (user_id, country_id) VALUES\n"])
        file.writelines(values)
        file.writelines(traveller_types)
        file.writelines(destinations)
        file.writelines(trips)
        file.writelines(trip_data)
        file.writelines(treasure_hunts)
        file.writelines(["\n-- !Downs\n" + drops])
        
    
    print("Complete!")

if __name__ == "__main__":
    main()
