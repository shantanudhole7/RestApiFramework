package StepDefinition;

import Api.ApiClient;
import Api.OrderApi;
import Api.ProductApi;
import ApiDataBuilder.OrderApiDataBuilder;
import ApiDataBuilder.ProductApiDataBuilder;
import Context.AuthSession;
import Context.TestContext;
import Pojo.CreateOrder;
import Pojo.Product;
import Resources.GlobalEnum;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.io.IOException;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpOk;
import static Assertion.Assertions.assertHttpStatus;
import static Logger.Log.logInformation;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_CREATED;

/** Step definitions for the Order API feature. */
public class OrderApiStepDefinition {

	private final TestContext testContext;
	private final ApiClient apiClient;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 * @param apiClient   client that sends prepared requests and checks the responses
	 */
	public OrderApiStepDefinition(TestContext testContext, ApiClient apiClient) {
		this.testContext = testContext;
		this.apiClient = apiClient;
	}

	@Given("a product exists in the store")
	public void aProductExistsInTheStore() throws IOException {
		logInformation("Create a product for the order");
		AuthSession session = testContext.getAuthSession();
		ProductApiDataBuilder productApiDataBuilder = new ProductApiDataBuilder();

		Product product = productApiDataBuilder.defaultCreateProductPayload(session.getUserId());
		Response response = new ProductApi().createProduct(product, session);

		String productId = getJsonPath(response, GlobalEnum.CREATE_PRODUCT.getCreatedIdPath());

		if (productId != null) {
			// Track before the status check, so the product is cleaned up even if the check fails
			testContext.addCreatedId(GlobalEnum.CREATE_PRODUCT, productId);
		}
		assertHttpStatus(response, HTTP_CREATED);
		testContext.setProductId(productId);
	}

	@Given("Place Order payload with country {string}")
	public void placeOrderPayloadWithCountry(String country) throws IOException {
		logInformation("Build Create Order request for '" + country + "'");
		AuthSession session = testContext.getAuthSession();
		OrderApiDataBuilder orderApiDataBuilder = new OrderApiDataBuilder();
		CreateOrder createOrder = orderApiDataBuilder.createOrderPayload(country, testContext.getProductId());
		testContext.setRequestSpecification(new OrderApi().createOrderRequest(createOrder, session));
	}

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

	@Then("the fetched order contains the ordered product")
	public void theFetchedOrderContainsTheOrderedProduct() {
		logInformation("Verify the fetched order contains the ordered product");
		assertEquals("Verify that the fetched order contains the ordered product", testContext.getProductId(),
		             getJsonPath(testContext.getResponse(), "data.productOrderedId"));
	}
}
