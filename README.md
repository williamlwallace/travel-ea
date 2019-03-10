# SENG302 Project Template
Basic Play project using sbt build and basic GitLab CI.
It is a requirement that your product can be packaged to a deliverable form using the `sbt dist` command.
Remember to set up your GitLab CI server (refer to the student guide for instructions).

## Basic Project Structure
* app/ Your application source
* doc/ User and design documentation
* doc/examples/ Demo example files for use with your application
* conf/ configuration files required to ensure the project builds properly

## How to run
Start the Play app:
```bash
sbt run
```
And open <http://localhost:9000/>

## Todo
* In the `build.sbt` file, you will need to ensure you update the name (on line 1) to your appropriate Team Number and Name
* Remember to set up your GitLab CI server (refer to the student guide on Learn for instructions).

### Reference
* [Play documentation](https://playframework.com/documentation/latest/Home](https://playframework.com/documentation/latest/Home)
* [EBean](https://www.playframework.com/documentation/latest/JavaEbean) is a Java ORM library that uses SQL.The documentation can be found [here](https://ebean-orm.github.io/).
* For Java Forms, Please see [here](<https://playframework.com/documentation/latest/JavaForms>).

