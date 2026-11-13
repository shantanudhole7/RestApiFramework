package StepDefinition;

import Api.ApiClient;
import Api.ProductApi;
import ApiDataBuilder.ProductApiDataBuilder;
import Context.AuthSession;
import Context.TestContext;
import Pojo.Product;
import Resources.GlobalEnum;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.Method;

import java.io.IOException;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpOk;
import static Logger.Log.logInformation;
import static Utilities.ResponseUtils.getJsonPath;

/** Step definitions for the Product API feature. */
public class ProductApiStepDefinition {

	private final TestContext testContext;
	private final ApiClient apiClient;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario.
	 * @param apiClient   client that sends prepared requests and checks the responses
	 */
	public ProductApiStepDefinition(TestContext testContext, ApiClient apiClient) {
		this.testContext = testContext;
		this.apiClient = apiClient;
	}

	/** Build the Create Product request with the session set by the "the user is logged in" Background step. */
	@Given("Create Product payload with {string} {string} {string} {string} {string} {string}")
	public void createProductPayloadWith(String name, String category, String subCategory, String description,
	                                     String price, String productFor) throws IOException {
		ProductApiDataBuilder productApiDataBuilder = new ProductApiDataBuilder();
		AuthSession session = testContext.getAuthSession();
		Product product = productApiDataBuilder.createProductPayload(name, category, subCategory, description, price,
		                                                             productFor, session.getUserId());
		testContext.setRequestSpecification(new ProductApi().createProductRequest(product, session));
	}

	/**
	 * Fetch the product created by the previous Create Product call and verify it is the same product.
	 * Reads productId from the last response, then calls the given endpoint with GET.
	 *
	 * @param apiEndPoint endpoint name from GlobalEnum used to fetch the product, e.g. "GET_PRODUCT"
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Then("the product created can be fetched using {string}")
	public void theProductCreatedCanBeFetchedUsing(String apiEndPoint) throws IOException {
		String productId = getJsonPath(testContext.getResponse(), GlobalEnum.CREATE_PRODUCT.getCreatedIdPath());
		logInformation("Get the created product " + productId + " with " + apiEndPoint);
		testContext.setRequestSpecification(
				new ProductApi().getProductRequest(productId, testContext.getAuthSession()));
		apiClient.send(GlobalEnum.valueOf(apiEndPoint), Method.GET);

		logInformation("Verify the created product can be fetched");
		assertHttpOk(testContext.getResponse());

		logInformation("Verify the fetched product is the created one");
		assertEquals("Verify that the fetched product id matches", productId,
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
