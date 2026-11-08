package StepDefinition;

import Api.LoginApi;
import Context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.io.IOException;

import static Logger.Log.logInformation;

/**
 * Step definitions for the Product API feature.
 */
public class ProductStepDefinition {

	private final TestContext testContext;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public ProductStepDefinition(TestContext testContext) {
		this.testContext = testContext;
	}

	@Given("the user is logged in")
	public void theUserIsLoggedIn() throws IOException {
		logInformation("Use the logged-in e-commerce session");
		testContext.setAuthSession(new LoginApi().session());
	}

	@Given("Create Product payload with {string} {string} {string} {string} {string} {string}")
	public void createProductPayloadWith(String name, String category, String subCategory, String productDescription,
	                                     String price, String productFor) {
		throw new io.cucumber.java.PendingException();
	}

	@Then("the product created can be fetched using {string}")
	public void theProductCreatedCanBeFetchedUsing(String apiEndPoint) {
		throw new io.cucumber.java.PendingException();
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
