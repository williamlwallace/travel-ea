Feature: Story 10: Trips displayed on profiles

#  Scenario: Sorting by first date (AC1)
#    Given I am logged in
#    And I have no trips
#    And I have created at least two trips with a date
#    And I have created at least one trip without a date
#    When I view my profile
#    Then a list of trips is shown
#    And it shows all of my trips
#    And only my trips
#    And my trips are sorted by first date
#    And trips with no date are at the bottom
#
#  Scenario: Trips details are displayed when click on (AC1)
#    Given I am logged in
#    And I have no trips
#    And I have created at least one trip
#    And viewing my profile
#    When I click on a trip
#    Then the details of the trip are displayed
#    And I can edit the trip

  Scenario: View all of my trips (AC3)
    Given I am logged in
    And viewing my profile
    And I have no trips
    And I have created some trips
    When I click view my trips
    Then a list of trips is shown
    And it shows all of my trips
    And only my trips

#  Scenario: View other travellers trips (AC4)
#    Given I am logged in
#    And viewing another user profile
#    And they have at least one public trip
#    When I click on a trip
#    Then the details of the trip are displayed
#    And I cannot edit the trip
