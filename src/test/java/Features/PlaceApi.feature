@PlaceApi
Feature: Place API
  Add, get and delete places through the Place API.

  @AddPlace @Smoke
  Scenario Outline: Add a place and verify it can be fetched
    Given Add Place payload with "<Name>" "<Address>" "<Phone Number>" "<Language>" "<Website>" "<Types>"
    When User calls "ADD_PLACE" with "POST" http request
    Then the API call succeeds with status code 200
    And the place created maps to "<Name>" using "GET_PLACE"

    Examples:
      | Name         | Address         | Phone Number | Language | Website                    | Types    |
      | Test Place 1 | 123 Main Street | 9876543210   | English  | https://example.com/place1 | Engineer |
      | Test Place 2 | 456 Elm Street  | 1234567890   | Hindi    | https://example.com/place2 | Doctor   |


  @DeletePlace @Smoke
  Scenario: Delete a place
    Given a place exists
    And Delete Place payload for the created place
    When User calls "DELETE_PLACE" with "POST" http request
    Then the API call succeeds with status code 200
    And "status" in response body is "OK"