Feature: Driver initialization

  Scenario: Appium server is not running
    Given Appium is not running
    When I request an Appium driver
    Then an EConfigException is thrown