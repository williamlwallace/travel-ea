# User Manual - Manual Testing

This document outlines user manual testing for TravelEA

## Start Page - Logging in and Signing up

- When opening the web app, the user will be taken to the start page at /
- If the user is logged in, they will be taken to /home instead and their email address
  will be displayed on the navbar.
- If the user is not logged in they will be taken to the start page if they try to access
  any other page.
- On the start page, two buttons will be displayed; Log In and Sign Up.

#### Logging in:

- Clicking on Log In will display a drop down form asking for an email and password.
- The two buttons will now display as Cancel and Log In
- Clicking cancel will hide the log in form and display the log in sign up buttons again
- Pressing the Log In button before filling out the email and/or password fields
  will display warnings that the fields must be filled out
- Entering an email that isn't registered will display an error message in red saying
  "Email: <the email address> is not registered!"
- Entering a registered email address with an incorrect password will display the error
  message "Incorrect password!"
- Entering a registered email with its corresponding password will log the user in
  and take them to the home page at /home

#### Signing up:

- Clicking on Sign Up will display a drop down form asking for an email, password and
  to repeat the password.
- The two buttons will now display as Cancel and Register
- Clicking cancel will hide the sign up form and display the log in sign up buttons again
- Pressing the register button before filling out the fields will display warnings that
  the fields must be filled out
- Entering an email that is already linked to an account will display an error message
  in red saying "Email: <the email address> is already in use!"
- If the password and repeat password fields do not match, an error message will be
  displayed reading "Password not matching!" and disabling the register button.
- If the email is valid and the passwords are matching, the account will be created
  and the user will be taken to the profile page at /profile to create their profile
  
  #### Create Profile:
  - Profile page is automatically opened with the create new profile card showing.
  - User enters First Middle and Last name.
  - If first or last name are missing and user attempts to create profile, a small alert box appears 
  asking the user to fill in the field
  - Gender and DOB are both cllickable drop downs with a placeholder initially selected. Scene will not
  move on until these are selected
  - Nationalities, Passport and Traveller Types are clickable drop downs like gender and DOB, but these three
  are multi select, when one option is selectedm it appears as a removable tag. These tags keep apearing next to
  each other as subsequent values are selected. The selected value is removed from the list. If the selected tags are 
  too long for one line, they form a second line etc.
  -Nationalities and Traveller Types are required so attempting to create without any selected will not do anyting. 

## Destinations Page

- When the user clicks the destination tab in the navbar they will be taken to the destination 
  page at /destinations.
- The destination page allows users to view all user created destinations and also create new ones.
- On the destinations page there is a button for creating a new destination and below that is a 
  table of all the users' previously entered destinations.
  
#### Creating a new destination:

- Clicking on the 'Create New Destination' button will display a modal popup with six fields the
  user will have to complete including: Name, Type, District, Latitude, Longitude and Country.
- The form requires all fields to be entered. If the user does not fill out one of the fields and
  attempts to click the create button the form will prompt the user 'Please fill in this field' 
  on the corresponding field.
- The latitude and longitude fields are required to be numeric decimal values only. If the user
  enters anything other than this and attempts to click the create button an error message will display.
- If all the fields are filled and match the required format, when the user clicks the create
  button the popup will close, the page will be reloaded and the created destination will be 
  automatically added to the destination table.
  
#### The destination table:

- The destination table lists all user created destinations.
- It can be sorted both up and down by all the different attributes by clicking on the desired
  attribute to sort.
- A search bar located at the top right of the table allows users to search by any key word, the
  corresponding matches to the entered key word will be displayed in the table.

## Profile page

- The profile page, accessible through clicking on the email on the navbar and then selecting profile.
- On the page is a form, which appears as a spread out version of the profile card
- The values can be edited
- A button to update profile is at the bottom, this does nothing if required fields are not selected
- A profile Updated message is displayed if the profile is successfully updated
- The profile fields are pre filled with relevant values

## Trip page

- When the user clicks the 'Trips' tab on the navbar they will be taken to the Trips page at /trips
- The trips page allows the user to see a list of all their trips so that they can remember what they have
  done. This includes all upcoming and past trips. The user is also able to sort through these trips by any
  of the table headers as well as including a search functionality at the top right of the table.
- Above the trips table is a 'Create New Trip' button which will take the user to the create a trip page at
  /trips/create.
 
## Create a Trip page

- Upon clicking the 'Create New Trip' button the user will be taken to the create trips page at /trips/create
- This page lets the user create; a new destination, add a destination to the trip, edit the arrival and departure 
times of destinations, and rearrange the order of traverse.
- When the user has finalised their Trip, pressing the 'Done' button will add the Trip to their current Trips,
 which can be viewed under the 'Trips' tab in the navigation bar.

#### Create new destination:

- Pressing the 'Create Destination' button will open a modal popup as seen in the destinations page
 to create a new destination without having to go back to the destinations page.

#### Add destination:

- Pressing 'Add Destination' will take you to a table with all the previously added destinations with
 an 'Add' button next to each destination.
- After adding a destination, a new card will appear with the added destinations details.

#### Editing destination times:

- Each destination card will have an edit button which will open a modal popup to allow the user to 
edit arrival and departure date and times.
- Having a departure time earlier than an arrival time will give the user an error.

#### Order of destinations:

- The order of traverse can be rearranged by dragging and dropping the destination cards to the desired
order of travel.
- If two of the same destination are placed next to each other, an error will display upon pressing 'Done'.

## People Page

- Once on the people page, the user will see a table displaying a list of other travellers details including;
    first name, last name, gender, age, nationalities and traveller types.
- The user can interact with this table in various ways including; sorting by columns, changing number of entries 
  displayed per page, and using the search bar to quickly find travellers with specific attributes.
- The table also has a filter button that allows the user to perform a more precise search and display only the filtered results in the table.
- When the filter button is clicked, a modal with popup with 5 different attributes the user can filter by. This includes nationality, gender, 
age range (minimum age and maximum age) and also traveller type. A user can enter as many of these fields as they require and when apply is
clicked the filtered results will be displayed in the table. If the user clicks cancel, the modal will close and the table will remain the same.