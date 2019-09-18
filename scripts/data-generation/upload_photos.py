import json, requests, sys, getopt, os
from PIL import Image

"""
Script to populate photos into the database, which is done by making requests
to the TravelEA API.
"""

def import_photos_dict(photos_filename):
    """Imports the photos dict from a file and returns the contents as a 
    dictionary
    
    Keyword arguments:
    photos_filename -- the name of the file containing the photo information
    """
    with open(photos_filename, "r", newline='') as file:
        return json.loads(file.read())

def get_photo_data(URL):
    response = requests.get(URL)
    #size = 400, 266 #pixels
    baseheight = 350
    if response.status_code == 200:
        open("tempImage.jpeg", "wb").write(response.content)
        img = Image.open("tempImage.jpeg")
        #hpercent = (baseheight / float(img.size[1]))
        #wsize = int((float(img.size[0]) * float(hpercent)))
        #img = img.resize((wsize, baseheight), Image.ANTIALIAS)
        #img.thumbnail(size, Image.ANTIALIAS)
        img = img.resize((baseheight, baseheight), Image.ANTIALIAS)
        img.save("tempImage.jpeg")
        
    
    return response.status_code

def post_photo(cookie, data, port):
    URL = "http://localhost:" + port + "/api/photo"
    headers = {"cookie": "JWT-Auth=" + cookie}
    response = requests.post(URL, files=data, headers=headers)
    #response = requests.post('http://httpbin.org/post', files=data, headers=headers)
    #print(response.json())
    return response.status_code
    
    
def make_profile_picture(user_id, photo_id, port, cookie):
    URL = "http://localhost:" + port + "/api/photo/" + str(photo_id) + "/profile"
    headers = {"cookie": "JWT-Auth=" + cookie}
    
    response = requests.put(URL, headers=headers, json=json.dumps(1))
    
    return response.status_code

def get_and_post_photos(photos_dict, port, cookie, num_existing_photos):
    failed = []
    data = {"isTest": ("", "false")}
    
    photo_id = num_existing_photos
    
    for photo in photos_dict:
        data["profilePhotoName"] = ("", photo)
        #data["publicPhotoFileNames"] = ("", "[" + photo + "]")
        print("Retrieving photo..")        
        if get_photo_data(photo) == 200:
            #print("Photo retrieved")
            for user in photos_dict[photo]:
                data["userUploadId"] = ("", str(1)) #Think this should be user
                data["files"] = (photo, open('tempImage.jpeg', 'rb'), "image/jpeg")
                #print("POSTing photo to TravelEA..")
                if post_photo(cookie, data, port) == 201:
                    #print("Successfully POSTed photo to TravelEA")
                    photo_id += 1
                    #print("Attempting to make uploaded photo the user's profile picture")
                    if make_profile_picture(user, photo_id, port, cookie) != 200:
                        print("Failed to make uploaded photo the user's profile picture")
                        failed.append(("Failed to make profile picture", photo, user))
                    else:
                        print("Successfully made the uploaded photo the user's profile picture")
                else:
                    print("Failed to POST photo to TravelEA: ")                
                    failed.append(("Failed to upload photo", photo, user))
                        
        else:
            print("Failed to retrieve photo")            
            failed.append(("Failed to get photo", photo))
            
    return failed
        

def main(argv):
    """Main function"""
    port = ""
    photos_filename = ""
    
    def error():
        print("usage: upload_photos.py (optional: -p <port> -f <filename.json>)")
        sys.exit(2)             
    
    # Get the inputs and ensure the options have their required arguments
    try:
        opts, args = getopt.getopt(argv, "p:f:", ["port=", "filename="])
    except getopt.GetoptError:
        error()
    
    for opt, arg in opts:
        if opt in ("-p", "--port"):
            if not arg.isnumeric():
                error()
            port = arg
        elif opt in ("-f", "--filename"):
            if arg.lower().endswith(".json"):
                photos_filename = arg
            else:
                error()

    if port == "":
        port = "9000"     
        
    if photos_filename == "":
        photos_filename = "photo_urls_and_users.json"
        
        
    admin_cookie = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw"
    num_existing_photos = 1
     
    photos_dict = import_photos_dict(photos_filename)
    errors = get_and_post_photos(photos_dict, port, admin_cookie, num_existing_photos)
    print("Errors: " + str(errors))
    
    if os.path.exists("tempImage.jpeg"):
        os.remove("tempImage.jpeg")     

if __name__ == "__main__":
    main(sys.argv[1:])