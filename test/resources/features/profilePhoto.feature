Feature: Story 12: Profile pictures

  Scenario: Upload a profile Picture (AC2)
    Given I am logged in
    And viewing my profile
    When I upload a valid photo
    Then I can set it as my profile photo

  Scenario: Upload a profile Picture creates a thumbnail (AC4)
    Given I am logged in
    And viewing my profile
    When I upload a valid photo
    And I set it as my profile photo
    Then A thumbnail is created

  Scenario: Display profile pic (AC1)
    Given I am logged in
    And viewing my profile
    When I upload a valid photo
    And I set it as my profile photo
    Then It is returned as my profile picture