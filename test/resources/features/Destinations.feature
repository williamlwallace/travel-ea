Feature: Story 13

  Scenario: Make destination public
    Given I am logged in
    And I have created a private destination
    When I make my destination public
    Then The next time i retrieve it, it is public

  Scenario: Private destination
    Given I am logged in
    And I have created a private destination
    Then The next time I retrieve all public destinations, my private destination is not among them