package StepDefinition;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

/** Step definitions for the Order API feature. */
public class OrderApiStepDefinition {

	@Given("a product exists in the store")
	public void aProductExistsInTheStore() {
		throw new io.cucumber.java.PendingException();
	}

	@Given("Place Order payload with country {string}")
	public void placeOrderPayloadWithCountry(String country) {
		throw new io.cucumber.java.PendingException();
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
