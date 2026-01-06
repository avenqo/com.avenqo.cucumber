Feature: Coffee Machine Mood Behavior
  The office coffee machine behaves strangely on Mondays.
  Everybody wants coffee, but the machine has emotions.

  Background:
    Given the coffee machine is switched on

  Scenario: Happy machine on a normal day
    Given today is "Tuesday"
    When I order a "cappuccino"
    Then the machine should serve a "cappuccino"
    And the machine should respond with "Have a wonderful day!"

  Scenario: Grumpy machine on Monday
    Given today is "Monday"
    When I order an "espresso"
    Then the machine should refuse the order
    And the machine should display "Nope. It's Monday."

  Scenario Outline: Random mood swings
    Given today is "<weekday>"
    When I order a "<coffee>"
    Then the machine should respond according to its mood:
      | Monday    | "Not today!"      |
      | Friday    | "Weekend vibes!"  |
      | Tuesday   | "Sure thing!"     |
      | Saturday  | "System offline"  |

    Examples:
      | weekday  | coffee     |
      | Monday   | latte      |
      | Tuesday  | flat white |
      | Friday   | espresso   |
      | Saturday | americano  |
