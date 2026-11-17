package StepDefinition;

import Api.ApiClient;
import Api.OrderApi;
import ApiDataBuilder.OrderApiDataBuilder;
import Context.AuthSession;
import Context.TestContext;
import Pojo.CreateOrder;
import Resources.GlobalEnum;
import Setup.ProductSetup;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.Method;

import java.io.IOException;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpOk;
import static Assertion.Assertions.assertHttpStatus;
import static Logger.Log.logInformation;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/** Step definitions for the Order API feature. */
public class OrderApiStepDefinition {

	private final TestContext testContext;
	private final ApiClient apiClient;
	private final ProductSetup productSetup;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext  state shared by the steps and hooks of the current scenario
	 * @param apiClient    client that sends prepared requests and checks the responses
	 * @param productSetup creates the product the order is placed for
	 */
	public OrderApiStepDefinition(TestContext testContext, ApiClient apiClient, ProductSetup productSetup) {
		this.testContext = testContext;
		this.apiClient = apiClient;
		this.productSetup = productSetup;
	}

	/**
	 * Create the product the order is placed for, through ProductSetup, which registers it for cleanup and stores
	 * its id in TestContext for the payload step.
	 *
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Given("a product exists in the store")
	public void aProductExistsInTheStore() throws IOException {
		logInformation("Create a product for the order");
		productSetup.createDefaultProduct(testContext.getAuthSession());
	}

	/**
	 * Build the Create Order request for the product created by the previous step, with the session set by the
	 * "the user is logged in" Background step.
	 *
	 * @param country the country to ship the order to, from the Examples table
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Given("Place Order payload with country {string}")
	public void placeOrderPayloadWithCountry(String country) throws IOException {
		logInformation("Build Create Order request for '" + country + "'");
		AuthSession session = testContext.getAuthSession();
		OrderApiDataBuilder orderApiDataBuilder = new OrderApiDataBuilder();
		CreateOrder createOrder = orderApiDataBuilder.createOrderPayload(country, testContext.getProductId());
		testContext.setRequestSpecification(new OrderApi().createOrderRequest(createOrder, session));
	}

	/**
	 * Read the new order id from the Create Order response, fetch that order, and check it is the one created.
	 * The fetch response replaces the create response in TestContext for the next step.
	 *
	 * @param apiEndPoint the GlobalEnum name of the fetch endpoint, e.g. "GET_ORDER"
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Then("the order created can be fetched using {string}")
	public void theOrderCreatedCanBeFetchedUsing(String apiEndPoint) throws IOException {
		String orderId = getJsonPath(testContext.getResponse(), GlobalEnum.CREATE_ORDER.getCreatedIdPath());
		logInformation("Get the created order " + orderId + " with " + apiEndPoint);
		testContext.setRequestSpecification(new OrderApi().getOrderRequest(orderId, testContext.getAuthSession()));
		apiClient.send(GlobalEnum.valueOf(apiEndPoint), Method.GET);

		logInformation("Verify the created order can be fetched");
		assertHttpOk(testContext.getResponse());

		logInformation("Verify the fetched order is the created one");
		assertEquals("Verify that the fetched order id matches", orderId,
		             getJsonPath(testContext.getResponse(), "data._id"));
	}

	/** Check that the order fetched by the previous step is linked to the product created by the setup step. */
	@Then("the fetched order contains the ordered product")
	public void theFetchedOrderContainsTheOrderedProduct() {
		logInformation("Verify the fetched order contains the ordered product");
		assertEquals("Verify that the fetched order contains the ordered product", testContext.getProductId(),
		             getJsonPath(testContext.getResponse(), "data.productOrderedId"));
	}

	/**
	 * Build the Delete Order request for the orderId set by the @DeleteOrder hook
	 * (OrderHooks.createOrderForDelete).
	 *
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Given("Delete Order request for the placed order")
	public void deleteOrderRequestForThePlacedOrder() throws IOException {
		logInformation("Build Delete Order request for order " + testContext.getOrderId());
		testContext.setRequestSpecification(
				new OrderApi().deleteOrderRequest(testContext.getOrderId(), testContext.getAuthSession()));
	}

	/**
	 * Fetch the order deleted by this scenario (its id set by the @DeleteOrder hook) and check the API reports it as
	 * not found: 400 with "Order not found".
	 *
	 * @param apiEndPoint the GlobalEnum name of the fetch endpoint, e.g. "GET_ORDER"
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Then("the deleted order can no longer be fetched using {string}")
	public void theDeletedOrderCanNoLongerBeFetchedUsing(String apiEndPoint) throws IOException {
		logInformation("Get the deleted order " + testContext.getOrderId() + " with " + apiEndPoint);
		testContext.setRequestSpecification(
				new OrderApi().getOrderRequest(testContext.getOrderId(), testContext.getAuthSession()));
		apiClient.send(GlobalEnum.valueOf(apiEndPoint), Method.GET);

		logInformation("Verify the deleted order is not found");
		assertHttpStatus(testContext.getResponse(), HTTP_BAD_REQUEST);

		logInformation("Verify the order not found message");
		assertEquals("Verify the order not found message", OrderApi.ORDER_NOT_FOUND_MESSAGE,
		             getJsonPath(testContext.getResponse(), "message"));
	}
}
