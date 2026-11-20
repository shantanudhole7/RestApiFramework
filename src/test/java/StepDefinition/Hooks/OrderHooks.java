package StepDefinition.Hooks;

import Api.LoginApi;
import Api.OrderApi;
import Context.AuthSession;
import Context.TestContext;
import Resources.GlobalEnum;
import Setup.OrderSetup;
import Setup.ProductSetup;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.List;

import static Api.OrderApi.ORDER_NOT_FOUND_MESSAGE;
import static Logger.Log.logInformation;
import static Logger.Log.logWarning;
import static Utilities.ResponseUtils.getJsonPath;
import static Utilities.Retry.sendWithRetry;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Cucumber hooks for orders: place an order for each Delete scenario, and delete every order a scenario created,
 * before its products are deleted.
 */
public class OrderHooks {

	private final TestContext testContext;
	private final ProductSetup productSetup;
	private final OrderSetup orderSetup;

	/**
	 * Cucumber injects the same TestContext the scenario's steps use (cucumber-picocontainer),
	 * so it sees the orders the scenario's steps created.
	 *
	 * @param testContext  state shared by the steps and hooks of the current scenario
	 * @param productSetup creates the product each Delete scenario's order is placed for
	 * @param orderSetup   places the order for each Delete scenario
	 */
	public OrderHooks(TestContext testContext, ProductSetup productSetup, OrderSetup orderSetup) {
		this.testContext = testContext;
		this.productSetup = productSetup;
		this.orderSetup = orderSetup;
	}

	/**
	 * Delete every order created in the scenario, whether the scenario passed or failed.
	 * Cleanup never fails the scenario: an order that can't be deleted is logged as a warning instead.
	 * A server or network error is retried first (Retry, cleanup.retry.* in Global.properties).
	 * Runs before product cleanup (for @After, a higher order runs first), so orders are deleted before
	 * the products they reference.
	 */
	@After(order = 20000)
	public void cleanUpCreatedOrders() {
		List<String> orderIds = testContext.getCreatedIds(GlobalEnum.CREATE_ORDER);
		if (orderIds.isEmpty()) {
			return;
		}

		OrderApi orderApi = new OrderApi();
		for (String orderId : orderIds) {
			try {
				Response response = sendWithRetry(() -> orderApi.deleteOrder(orderId, LoginApi.session()),
				                                  "delete order " + orderId);
				int statusCode = response.statusCode();
				if (statusCode == HTTP_OK) {
					logInformation("Cleanup: deleted order " + orderId);
				} else if (isOrderNotFound(response)) {
					logInformation("Cleanup: order " + orderId + " was already deleted");
				} else {
					String details = " | Status: " + statusCode + " | Body: " + response.asString();
					logWarning("Cleanup: could not delete order " + orderId + details);
				}
			} catch (Exception | AssertionError e) {
				// AssertionError too: the shared response spec throws it for a non-JSON response
				logWarning("Cleanup: could not delete order " + orderId + " | " + e.getMessage());
			}
		}
	}

	/**
	 * @param response response of a Delete Order call
	 * @return true if the API reports the order as not found (400 with "Order not found…")
	 */
	private boolean isOrderNotFound(Response response) {
		String message = getJsonPath(response, "message");
		return response.statusCode() == HTTP_BAD_REQUEST && message != null && message.startsWith(
				ORDER_NOT_FOUND_MESSAGE);
	}

	/**
	 * Place an order, with a product of its own, before each Delete Order scenario and keep its orderId,
	 * so every Delete scenario deletes an order of its own. Hooks run before the Background,
	 * so the hook gets the session itself.
	 *
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Before("@DeleteOrder")
	public void createOrderForDelete() throws IOException {
		logInformation("Hook: place an order for the Delete Order scenario");
		AuthSession session = LoginApi.session();
		productSetup.createDefaultProduct(session);
		orderSetup.placeDefaultOrder(session);
	}
}
