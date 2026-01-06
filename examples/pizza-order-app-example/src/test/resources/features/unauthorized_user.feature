Feature: Unauthorized user visits the Pizza Order App "Italian Frisbee"
  As an unauthorized user
  I would like to check the current offers
  so that I can decide whether to order or not.

  Scenario: When starting the app, the landing page becomes visible
    Given the App is installed
    When I open the App
    Then I see the page "Landing"
    And the page "Landing" is complete
    And the page Landing shows at least 3 base pizzas

  Scenario: Per pizza, I can find out about additional ordering options.
    Given the page "Landing" is visible
    When I select the pizza "Quattro Stagioni"
    Then I see the page "Pizza Details"
    And I can see the following optional pizza toppings
      | Zwiebeln | 1,00€ |
      | Pilze    | 1,90€ |
      | Oliven   | 1,50€ |
    And I cannot see topping options like
      | bacon |
      | egg   |

  Scenario: I can prepare my order, but I cannot place it.
    Given the page "Pizza Details" is visible
    When I select topping option "Oliven"
    Then the price of my order is "12,50 €"
    And I can add my current Pizza selection to the basket

  Scenario: As an unauthorized user I cannot place my order.
    Given the basket is open
    When I try to order
    Then I see the page "Login"


