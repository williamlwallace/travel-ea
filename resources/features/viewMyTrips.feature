Feature: View my trips
  Scenario:
    Given I am logged in
    And viewing my profile
    And have created some trips
    When I click view my trips
    Then a list of trips is shown
    And it shows all of my trips
    And only my trips