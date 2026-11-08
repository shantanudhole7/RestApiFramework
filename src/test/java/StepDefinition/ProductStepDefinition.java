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
import static Logger.Log.logInformation;
import static Resources.Utils.getJsonPath;

/**
 * Step definitions for the Product API feature.
 */
public class ProductStepDefinition {

	private final TestContext testContext;
	private final StepDefinition commonSteps;

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

	@Given("Delete Product request for the created product")
	public void deleteProductRequestForTheCreatedProduct() {
		throw new io.cucumber.java.PendingException();
	}

	@Then("the deleted product can no longer be fetched using {string}")
	public void theDeletedProductCanNoLongerBeFetchedUsing(String apiEndPoint) {
		throw new io.cucumber.java.PendingException();
	}
}
