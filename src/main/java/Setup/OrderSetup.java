package Setup;

import Api.OrderApi;
import ApiDataBuilder.OrderApiDataBuilder;
import Context.AuthSession;
import Context.TestContext;
import Pojo.CreateOrder;
import Resources.GlobalEnum;
import io.restassured.response.Response;

import java.io.IOException;

import static Assertion.Assertions.assertHttpStatus;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_CREATED;

/**
 * Places orders that scenarios need as setup, and registers them for cleanup.
 * Orders are placed for the scenario's product, so ProductSetup must create one first.
 */
public class OrderSetup {

	private final TestContext testContext;

	/**
	 * Cucumber injects the same TestContext the scenario's steps and hooks use (cucumber-picocontainer),
	 * so orders placed here are registered for that scenario's cleanup.
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public OrderSetup(TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Place an order for the scenario's product, register it for cleanup, check it was placed,
	 * and store its id in TestContext as the scenario's order.
	 *
	 * @param session the logged-in session
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public void placeDefaultOrder(AuthSession session) throws IOException {
		OrderApiDataBuilder orderApiDataBuilder = new OrderApiDataBuilder();
		CreateOrder createOrder = orderApiDataBuilder.defaultCreateOrderPayload(testContext.getProductId());
		Response response = new OrderApi().createOrder(createOrder, session);

		String orderId = getJsonPath(response, GlobalEnum.CREATE_ORDER.getCreatedIdPath());
		if (orderId != null) {
			// Track before the status check, so the order is cleaned up even if the check fails
			testContext.addCreatedId(GlobalEnum.CREATE_ORDER, orderId);
		}
		assertHttpStatus(response, HTTP_CREATED);
		testContext.setOrderId(orderId);
	}
}
