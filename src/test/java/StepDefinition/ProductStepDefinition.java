package StepDefinition;

import Api.LoginApi;
import Api.ProductApi;
import Context.AuthSession;
import Context.TestContext;
import Pojo.Product;
import Resources.TestDataBuilder;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.io.IOException;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpOk;
import static Assertion.Assertions.assertHttpStatus;
import static Logger.Log.logInformation;
import static Resources.Utils.getJsonPath;

/**
 * Step definitions for the Product API feature.
 */
public class ProductStepDefinition {

	private final TestContext testContext;
	private final StepDefinition commonSteps;

	/** Status this API returns for a product that doesn't exist: 400 Bad Request, not the conventional 404. */
	private static final int HTTP_BAD_REQUEST = 400;

	/** Message this API returns when getting a product that doesn't exist. */
	private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found";

	/**
	 * Cucumber injects the scenario's shared state and the shared step definitions (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 * @param commonSteps the shared steps, such as the http request step, for this scenario
	 */
	public ProductStepDefinition(TestContext testContext, StepDefinition commonSteps) {
		this.testContext = testContext;
		this.commonSteps = commonSteps;
	}

	/**
	 * Use the run's logged-in session: LoginApi logs in on the first call of the run, then reuses the session.
	 *
	 * @throws IOException if Global.properties cannot be read
	 */
	@Given("the user is logged in")
	public void theUserIsLoggedIn() throws IOException {
		logInformation("Use the logged-in e-commerce session");
		testContext.setAuthSession(new LoginApi().session());
	}

	@Given("Create Product payload with {string} {string} {string} {string} {string} {string}")
	public void createProductPayloadWith(String name, String category, String subCategory, String description,
	                                     String price, String productFor) throws IOException {
		TestDataBuilder testDataBuilder = new TestDataBuilder();
		AuthSession session = testContext.getAuthSession();
		logInformation("Build Create Product request for '" + name + "'");
		Product product = testDataBuilder.createProductPayload(name, category, subCategory, description, price,
		                                                       productFor, session.getUserId());
		testContext.setRequestSpecification(new ProductApi().createProductRequest(product, session));
	}

	/**
	 * Fetch the product created by the previous Create Product call and verify it is the same product.
	 * Reads productId from the last response, then calls the given endpoint with GET.
	 *
	 * @param apiEndPoint endpoint name from GlobalEnum used to fetch the product, e.g. "GET_PRODUCT"
	 * @throws IOException if Global.properties cannot be read
	 */
	@Then("the product created can be fetched using {string}")
	public void theProductCreatedCanBeFetchedUsing(String apiEndPoint) throws IOException {
		testContext.setProductId(getJsonPath(testContext.getResponse(), "productId"));

		logInformation("Get the created product " + testContext.getProductId() + " with " + apiEndPoint);
		testContext.setRequestSpecification(new ProductApi().getProductRequest(testContext.getProductId(),
		                                                                       testContext.getAuthSession()));
		commonSteps.userCallsWithHttpRequest(apiEndPoint, "GET");
		assertHttpOk(testContext.getResponse());

		logInformation("Verify that the fetched product is the created one");
		assertEquals("Verify that the fetched product id matches", testContext.getProductId(),
		             getJsonPath(testContext.getResponse(), "data._id"));
	}

	/**
	 * Build the Delete Product request for the productId set by the @DeleteProduct hook
	 * (Hooks.createProductForDelete).
	 *
	 * @throws IOException if Global.properties cannot be read
	 */
	@Given("Delete Product request for the created product")
	public void deleteProductRequestForTheCreatedProduct() throws IOException {
		logInformation("Build Delete Product request for product " + testContext.getProductId());
		testContext.setRequestSpecification(new ProductApi().deleteProductRequest(testContext.getProductId(),
		                                                                          testContext.getAuthSession()));
	}

	/**
	 * Fetch the product deleted earlier in the scenario and verify the API no longer finds it.
	 * Reads productId from TestContext (set by the @DeleteProduct hook) and calls the given endpoint with GET.
	 *
	 * @param apiEndPoint endpoint name from GlobalEnum used to fetch the product, e.g. "GET_PRODUCT"
	 * @throws IOException if Global.properties cannot be read
	 */
	@Then("the deleted product can no longer be fetched using {string}")
	public void theDeletedProductCanNoLongerBeFetchedUsing(String apiEndPoint) throws IOException {
		logInformation("Get the deleted product " + testContext.getProductId() + " with " + apiEndPoint);
		testContext.setRequestSpecification(new ProductApi().getProductRequest(testContext.getProductId(),
		                                                                       testContext.getAuthSession()));
		commonSteps.userCallsWithHttpRequest(apiEndPoint, "GET");

		logInformation("Verify the deleted product is not found");
		assertHttpStatus(testContext.getResponse(), HTTP_BAD_REQUEST);
		assertEquals("Verify the product not found message", PRODUCT_NOT_FOUND_MESSAGE,
		             getJsonPath(testContext.getResponse(), "message"));
	}
}
