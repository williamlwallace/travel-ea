Feature: News Feed Events

  Scenario: New profile photo, checking event created for profile
    Given I am logged in
    And I upload a valid photo
    And I set it as my profile photo
    When I get the news feed events for profile 1
    Then There will be 1 news feed event for the profile
    And The first profile news feed event will have type 'NEW_PROFILE_PHOTO'

  Scenario: New cover photo
    Given I am logged in
    And I upload a valid photo
    And I set it as my cover photo
    When I get the news feed events for profile 1
    Then There will be 1 news feed event for the profile
    And The first profile news feed event will have type 'NEW_PROFILE_COVER_PHOTO'

  Scenario: New public photo, then cover photo
    Given I am logged in
    And I upload a valid photo
    And I make the photo with id 1 public
    And I set it as my cover photo
    When I get the news feed events for profile 1
    Then There will be 2 news feed event for the profile
    And The first profile news feed event will have type 'NEW_PROFILE_COVER_PHOTO'
    And The profile news feed event at index 1 will have type 'MULTIPLE_GALLERY_PHOTOS'

  Scenario: Uploaded public photo
    Given I am logged in
    And I upload a valid photo
    And I make the photo with id 1 public
    When I get the news feed events for profile 1
    Then There will be 1 news feed event for the profile
    And The first profile news feed event will have type 'MULTIPLE_GALLERY_PHOTOS'

  Scenario: Created new public trip
    Given I am logged in
    And I have created a public trip
    When I get the news feed events for profile 1
    Then There will be 1 news feed event for the profile
    And The first profile news feed event will have type 'CREATED_NEW_TRIP'

  Scenario: Created private destination, then set to public
    Given I am logged in
    And I have created a private destination
    And I make the destination with id 6 public
    When I get the news feed events for profile 2
    Then There will be 1 news feed event for the profile
    And The first profile news feed event will have type 'UPDATED_EXISTING_DESTINATION'

  Scenario: Created public destination
    Given I am logged in
    And I have created a public destination
    When I get the news feed events for profile 2
    Then There will be 1 news feed event for the profile
    And The first profile news feed event will have type 'CREATED_NEW_DESTINATION'

  Scenario: Linked photo to destination
    Given I am logged in
    And I have created a public destination
    And I upload a valid photo
    And I make the photo with id 1 public
    And I link the photo with id 1 to the destination with id 6
    When I get the news feed events for profile 1
    And I get the news feed events for destination 6
    Then There will be 2 news feed event for the profile
    And The first profile news feed event will have type 'MULTIPLE_DESTINATION_PHOTO_LINKS'
    And There will be 1 news feed event for the destination
    And The first destination news feed event will have type 'MULTIPLE_DESTINATION_PHOTO_LINKS'