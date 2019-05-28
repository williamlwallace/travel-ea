# TravelEA - SENG302 Team Proffat

## Using TravelEA

### Online
- Open a browser and navigate to `travelea.online:443`
- You should now be viewing the TravelEA website
- Login or sign up to use TravelEA

### Locally
- Clone the git repository onto your computer
- Navigate into the folder `team-400/`
- Open a command window / terminal within this directory
- Type `sbt dist` into the command window / terminal and press `enter`
- Extract the contents of the named `seng302-team-400-proffat-*.*.*.zip` file in `team-400/target/universal/`
- Navigate into the extracted folder and then into the `bin` directory
- Run the executable to start the server
    - On UNIX systems use the Bash script
    - On Windows systems use the .bat script
- Wait for the message `p.c.s.AkkaHttpServer - Listening for HTTP on /0:0:0:0:0:0:0:0:9000`
- Open your preferred browser and type `http://localhost:9000` in as a url then press `enter`
- You should now be viewing the TravelEA website
- Login or sign up to use TravelEA

## Basic Project Structure
 - `app/` Your application source
 - `doc/` User and design documentation
 - `doc/examples/` Demo example files for use with your application
## Documentation


### Play

Play documentation is here:

[https://playframework.com/documentation/latest/Home](https://playframework.com/documentation/latest/Home)

### EBean

EBean is a Java ORM library that uses SQL:

[https://www.playframework.com/documentation/latest/JavaEbean](https://www.playframework.com/documentation/latest/JavaEbean)

and the documentation can be found here:

[https://ebean-orm.github.io/](https://ebean-orm.github.io/)

### Forms

Please see <https://playframework.com/documentation/latest/JavaForms>