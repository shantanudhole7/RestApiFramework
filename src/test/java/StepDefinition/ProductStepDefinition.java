package StepDefinition;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class ProductStepDefinition {

	@Given("the user is logged in")
	public void theUserIsLoggedIn() {
		throw new io.cucumber.java.PendingException();
	}

	@Given("Create Product payload with {string} {string} {string} {string} {string} {string}")
	public void createProductPayloadWith(String string, String string2, String string3, String string4, String string5,
	                                     String string6) {
		throw new io.cucumber.java.PendingException();
	}

	@Then("the product created can be fetched using {string}")
	public void theProductCreatedCanBeFetchedUsing(String string) {
		throw new io.cucumber.java.PendingException();
	}

	@Given("Delete Product request for the created product")
	public void deleteProductRequestForTheCreatedProduct() {
		throw new io.cucumber.java.PendingException();
	}

	@Then("the deleted product can no longer be fetched using {string}")
	public void theDeletedProductCanNoLongerBeFetchedUsing(String string) {
		throw new io.cucumber.java.PendingException();
	}
}
