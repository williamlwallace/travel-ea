Feature: Story 13

  Scenario: Make destination public
    Given I am logged in
    And I have created a private destination
    When I make my destination public
    Then The next time i retrieve it, it is public

  Scenario: Private destination
    Given I am logged in
    And I have created a private destination