package StepDefinition;

import Api.ProductApi;
import ApiDataBuilder.ProductApiDataBuilder;
import Context.AuthSession;
import Context.TestContext;
import Pojo.Product;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.io.IOException;

import static Logger.Log.logInformation;

/** Step definitions for the Product API feature. */
public class ProductApiStepDefinition {

	private final TestContext testContext;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario.
	 */
	public ProductApiStepDefinition(TestContext testContext) {
		this.testContext = testContext;
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

	@Then("the product created can be fetched using {string}")
	public void theProductCreatedCanBeFetchedUsing(String apiEndPoint) {
		throw new io.cucumber.java.PendingException();
	}
}
