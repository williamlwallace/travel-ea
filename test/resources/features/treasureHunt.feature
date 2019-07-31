Feature: Story 17: Treasure Hunts

Scenario: I can create a treasure hunt (AC3)
    Given I am logged in
    When I create a treasure hunt
    Then I can view my treasure hunt

Scenario: I can edit my treasure hunt (Ac3)
    Given I am logged in
    When I create a treasure hunt
    Then I can edit my treasure hunt
    And the details are updated

Scenario: A list of available treasure hunts is available (AC1)
    Given I am logged in
    And A treasure hunt has been created by someone else
    When I create a treasure hunt
    Then I can view a list of treasure hunts with both my hunt and the other hunt