@PlaceApi
Feature: Place API
  Add, get and delete places through the Place API.

  @AddPlace @Smoke
  Scenario: Add a place
    Given Add Place payload with "Test Place 1" "123 Main Street" "9876543210" "English" "https://example.com" "Engineer"
    When User calls "ADD_PLACE" with "POST" http request
    Then the API call succeeds with status code 200
    And the place created maps to "Test Place 1" using "GET_PLACE"