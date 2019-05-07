Feature: Story 11

  Scenario: A registered user can upload one or more personal photos (AC1)

    Given I am logged in
    And viewing my profile
    And I have no photos
    When I upload a valid photo
    Then the number of photos i have will be 1

  Scenario: A user can view all there photos (AC3)

    Given I am logged in
    And viewing my profile
    When I upload a valid photo
    Then I can view all my photos