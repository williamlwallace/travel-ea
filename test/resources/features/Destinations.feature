Feature: Story 13

  Scenario: Make destination public
    Given I am logged in
    And I have created a private destination
    When I make my destination public
    Then The next time i retrieve it, it is public

  Scenario: Private destination
    Given I am logged in
    And I have created a private destination

  Scenario: If a public destination is created and I have the same destination in my private list of destinations, it will automatically be merged with the public one. (AC6)
    Given I am logged in
    And I have created a private destination
    When A public destination is created which is the same as my private destination
    Then My private destination is automatically merged with the public one
