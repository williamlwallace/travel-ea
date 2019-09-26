# TravelEA - SENG302 Team Proffat

### Team members: 
- Campbell Mercer-Butcher `(cme68)`
- Claudia Field `(crf49)`
- Harrison Cook `(hgc25)`
- Matthew Minish `(mhm76)`
- Max Andrew `(mla138)`
- Ollie Sharplin `(osh17)`
- Ryan Chen `(rch141)`
- William Wallace `(wwa52)`

## Using TravelEA

### Online
- Open a browser and navigate to `travelea.online:443`
- You should now be viewing the TravelEA website
- Login or sign up to use TravelEA

### Locally
- If running on a UC lab computer, ensure Python Internet Enabler is running and enabled
- Clone the git repository onto your computer
- Navigate into the folder `team-400/`
- Open a command window / terminal within this directory
- Enter the command `npm i`
- Enter the command `npm run generate-data-local`
- Enter the command `git lifs pull` and enter your details
- Enter the command `sbt dist`
- Extract the contents of the file named `seng302-team-400-proffat-*.*.*.zip` file in `team-400/target/universal/`
- Navigate into the extracted folder and then into the `bin` directory
- Run the executable to start the server
    - On UNIX systems use the Bash script
    - On Windows systems use the .bat script
- Wait for the message `p.c.s.AkkaHttpServer - Listening for HTTP on /0:0:0:0:0:0:0:0:9000`
- Open your preferred browser and type `http://localhost:9000` in as a url then press `enter`
- You should now be viewing the TravelEA website
- Login or sign up to use TravelEA

#### Database
By default, running the application locally will use an H2 in-memory database. To switch to a MySQL database, go to the `conf` directory and open `application.conf`, comment out lines 351 & 352 and un-comment out lines 345-348. 
You can change what MySQL database you are using by changing the `test` in line 346 to `prod`.

Be aware that using one of the MySQL databases will likely cause you to run into issues due to the differences in the auto-generated example data in `3.sql`. These issues cannot be solved without wiping the database.


## Admin and Test User credentials
To log on as the main administrator for TravelEA, use the following credentials:
- Username: `admin@travelea.co.nz`
- Password: `admin`

To log on as test users for TravelEA, use the following:
- Username: `testUser@email.com`
- Password: `admin`

- Username: `testUser2@email.com`
- Password: `admin`

There are 20 detailed users with example data, you can login to any of them with the following emails, they all share the same password of `123`

Detailed Users:
* williamwallace@email.com
* maxandrew@gmail.com
* ryanchen@hotmail.com
* claudiafield@email.com
* campbellmbutcher@gmail.com
* harrisoncook@yahoo.co.nz
* matthewminish@gmail.com
* olliesharplin@email.com
* jimmyjeong@gmail.com
* vladimirkosh@email.com
* emmaferguson@gmail.com
* belleanderson@fire.co.nz
* oliknopp@yahoo.co.nz
* rosey.the.posey@xtra.co.nz
* ameliaheart@email.com
* genghiskhan@thehorde.mongolia
* vincentdiesel@yahoo.com
* dora@explorers.net
* johndear@email.com
* jackryan@shadowrecruits.wordpress.com


## Troubleshooting
#### Destinations page not loading
##### Locally: 
Usually this occurs when the user is behind a firewall blocking some access to the internet. On UC machines, enable Python Internet Enabler to solve this.
##### Online:
If using the `travelea.online` address, try using the `http://csse-s302g4.canterbury.ac.nz` address instead.

#### Google Maps not displaying correctly (online)
If using the `travelea.online` address, try using the `http://csse-s302g4.canterbury.ac.nz` address instead.

#### User uploaded images not displaying (locally)
##### On Unix:
This may be due to the location of the running directory. Try moving it to a directory with no spaces in the path.
##### On Windows:
Uploaded photos usually don't work on Windows machines. There is no solution at this time.