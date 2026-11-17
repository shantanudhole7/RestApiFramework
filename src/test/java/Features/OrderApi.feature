@OrderApi
Feature: Order API
  Place and delete orders in the e-commerce store as a logged-in user.

  Background:
    Given the user is logged in

  @CreateOrder @Smoke
  Scenario Outline: Create an order and verify it can be fetched
    Given a product exists in the store
    And Place Order payload with country "<Country>"
    When User calls "CREATE_ORDER" with "POST" http request
    Then the API call succeeds with status code 201
    And "message" in response body is "Order Placed Successfully"
    And the order created can be fetched using "GET_ORDER"
    And the fetched order contains the ordered product

    Examples:
      | Country |
      | India   |

  @DeleteProduct @Smoke
  Scenario: Delete a product and verify the delete succeeds
    # The @DeleteProduct hook creates the product to delete (ProductHooks.createProductForDelete)
    Given Delete Product request for the created product
    When User calls "DELETE_PRODUCT" with "DELETE" http request
    Then the API call succeeds with status code 200
    And "message" in response body is "Product Deleted Successfully"
    And the deleted product can no longer be fetched using "GET_PRODUCT"
