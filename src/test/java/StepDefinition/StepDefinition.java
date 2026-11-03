package StepDefinition;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

public class StepDefinition {

	@Given("Add Place payload with {string} {string} {string} {string} {string} {string}")
	public void addPlacePayloadWith(String name, String address, String phoneNumber, String language, String website,
	                                   String types) throws IOException {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}

	@When("User calls {string} with {string} http request")
	public void userCallsWithHttpRequest(String apiEndpoint, String method) {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}

	@Then("the API call succeeds with status code {int}")
	public void theApiCallSucceedsWithStatusCode(Integer statusCode) {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();

	}
}