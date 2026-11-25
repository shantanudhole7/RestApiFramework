@OrderApi @owner:Shantanu @feature:Order
Feature: Order API
  Place and delete orders in the e-commerce store as a logged-in user.

  Background:
    Given the user is logged in

  @CreateOrder @Smoke @P1 @critical
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

  @DeleteOrder @Smoke @P2 @major
  Scenario: Delete an order and verify the delete succeeds
    # The @DeleteOrder hook places the order to delete (OrderHooks.createOrderForDelete)
    Given Delete Order request for the placed order
    When User calls "DELETE_ORDER" with "DELETE" http request
    Then the API call succeeds with status code 200
    And "message" in response body is "Orders Deleted Successfully"
    And the deleted order can no longer be fetched using "GET_ORDER"
