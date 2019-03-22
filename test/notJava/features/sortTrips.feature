Feature: Sort Trips
  Scenario:
  Given I have at least two trips
  And at least one trip has a date
  And at least one trip has no date
  When I view my trips
  Then my trips without a date will be displayed below trips with a date
