Feature: News Feed Events

  Scenario: New profile photo, checking event created for profile
    Given I am logged in
    And I upload a valid photo
    And I set it as my profile photo
    When I get the news feed events for profile 1
    Then There will be 1 news feed event
    And The first news feed event will have type 'NEW_PROFILE_PHOTO'

  Scenario: Uploaded public photo
    Given I am logged in
    And I upload a valid photo
    And I make the photo with id 1 public
    When I get the news feed events for profile 1
    Then There will be 1 news feed event
    And The first news feed event will have type 'UPLOADED_USER_PHOTO'
