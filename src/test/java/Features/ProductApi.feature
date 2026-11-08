@ProductApi
Feature: Product API
  Create and delete products in the e-commerce store as a logged-in user.

  Background:
    Given the user is logged in

  @CreateProduct @Smoke
  Scenario Outline: Create a product and verify it can be fetched
    Given Create Product payload with "<Name>" "<Category>" "<Sub Category>" "<Description>" "<Price>" "<For>"
    When User calls "CREATE_PRODUCT" with "POST" http request
    Then the API call succeeds with status code 201
    And "message" in response body is "Product Added Successfully"
    And the product created can be fetched using "GET_PRODUCT"

    Examples:
      | Name           | Category | Sub Category | Description | Price | For   |
      | Test Product 1 | fashion  | T-Shirt      | Full        | 999   | Men   |
      | Test Product 2 | fashion  | Shoes        | Sneaker     | 1999  | Women |

  @DeleteProduct @Smoke
  Scenario: Delete a product and verify the delete succeeds
    # The @DeleteProduct hook creates the product to delete
    Given Delete Product request for the created product
    When User calls "DELETE_PRODUCT" with "DELETE" http request
    Then the API call succeeds with status code 200
    And "message" in response body is "Product Deleted Successfully"
    And the deleted product can no longer be fetched using "GET_PRODUCT"