package StepDefinition;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

/**
 * Step definitions for the Product API feature.
 */
public class ProductStepDefinition {

	@Given("the user is logged in")
	public void theUserIsLoggedIn() {
		throw new io.cucumber.java.PendingException();
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
