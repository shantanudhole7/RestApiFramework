package StepDefinition;

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
import io.restassured.response.Response;

import java.io.IOException;

import static Assertion.Assertions.assertHttpStatus;
import static Logger.Log.logInformation;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_CREATED;

/** Step definitions for the Order API feature. */
public class OrderApiStepDefinition {

	private final TestContext testContext;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public OrderApiStepDefinition(TestContext testContext) {
		this.testContext = testContext;
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
	public void theOrderCreatedCanBeFetchedUsing(String apiEndPoint) {
		throw new io.cucumber.java.PendingException();
	}

	@Then("the fetched order contains the ordered product")
	public void theFetchedOrderContainsTheOrderedProduct() {
		throw new io.cucumber.java.PendingException();
	}
}
