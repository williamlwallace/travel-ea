Feature: P2-MVP1 Photo captions

  Scenario: Uploading a photo and not setting a caption

    Given I am logged in
    And viewing my profile
    When I upload a valid photo
    Then when I view the photo, it will have the caption ''

  Scenario: Uploading a photo and setting a caption

    Given I am logged in
    And viewing my profile
    When I upload a valid photo with the caption 'Family on Tour'
    Then when I view the photo, it will have the caption 'Family on Tour'

  Scenario: Setting a caption on a photo

    Given I am logged in
    And viewing my profile
    When I upload a valid photo
    And set the photo caption to 'Sailing down the Avon river'
    Then when I view the photo, it will have the caption 'Sailing down the Avon river'
