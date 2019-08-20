Feature: Profile searching and pagination

  Scenario: Getting profiles with no filters

    Given I am logged in and some profiles exist
    When I search profiles
    Then I get 10 results
    And There are 501 total pages

  Scenario: Getting profiles with pageSize and pageNum specified
    Given I am logged in and some profiles exist
    And Get page number 1
    And Use page size 10
    When I search profiles
    Then I get 10 results
    And There are 501 total pages

  Scenario: Getting profiles with pageSize and pageNum increased
    Given I am logged in and some profiles exist
    And Get page number 5
    And Use page size 100
    When I search profiles
    Then I get 100 results
    And There are 51 total pages

  Scenario: Getting profiles on a page that doesn't exist
    Given I am logged in and some profiles exist
    And Get page number 1000
    And Use page size 10
    When I search profiles
    Then I get 0 results
    And There are 501 total pages

  Scenario: Getting profiles and sorting by first name ascending
    Given I am logged in and some profiles exist
    And Get page number 1
    And Use page size 10
    And Sort by "first_name"
    And Set ascending order of results to "true"
    When I search profiles
    Then I get 10 results
    And There are 501 total pages
    And The first profile has first name "Aada", last name "Neva"

  Scenario: Getting profiles and sorting by first name descending
    Given I am logged in and some profiles exist
    And Get page number 1
    And Use page size 10
    And Sort by "first_name"
    And Set ascending order of results to "false"
    When I search profiles
    Then I get 10 results
    And There are 501 total pages
    And The first profile has first name "یلدا", last name "علیزاده"