package StepDefinition;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

/** Step definitions for the Product API feature. */
public class ProductApiStepDefinition {

	@Given("Create Product payload with {string} {string} {string} {string} {string} {string}")
	public void createProductPayloadWith(String name, String category, String subCategory, String description,
	                                     String price, String productFor) {
		throw new io.cucumber.java.PendingException();
	}

	@Then("the product created can be fetched using {string}")
	public void theProductCreatedCanBeFetchedUsing(String apiEndPoint) {
		throw new io.cucumber.java.PendingException();
	}
}
