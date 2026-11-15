package StepDefinition.Hooks;

import Api.LoginApi;
import Api.OrderApi;
import Context.TestContext;
import Resources.GlobalEnum;
import io.cucumber.java.After;
import io.restassured.response.Response;

import java.util.List;

import static Api.OrderApi.ORDER_NOT_FOUND_MESSAGE;
import static Logger.Log.logInformation;
import static Logger.Log.logWarning;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

/** Cucumber hooks for orders: delete every order a scenario created, before its products are deleted. */
public class OrderHooks {

	private final TestContext testContext;

	/**
	 * Cucumber injects the same TestContext the scenario's steps use (cucumber-picocontainer),
	 * so it sees the orders the scenario's steps created.
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public OrderHooks(TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Delete every order created in the scenario, whether the scenario passed or failed.
	 * Cleanup never fails the scenario: an order that can't be deleted is logged as a warning instead.
	 */
	@After
	public void cleanUpCreatedOrders() {
		List<String> orderIds = testContext.getCreatedIds(GlobalEnum.CREATE_ORDER);
		if (orderIds.isEmpty()) {
			return;
		}

		OrderApi orderApi = new OrderApi();
		for (String orderId : orderIds) {
			try {
				Response response = orderApi.deleteOrder(orderId, LoginApi.session());
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
}
