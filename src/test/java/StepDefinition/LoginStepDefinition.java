package StepDefinition;

import Api.LoginApi;
import Context.TestContext;
import io.cucumber.java.en.Given;

import java.io.IOException;

import static Logger.Log.logInformation;

/** Steps for logging in to the e-commerce site. */
public class LoginStepDefinition {

	private final TestContext testContext;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer)
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public LoginStepDefinition(TestContext testContext) {
		this.testContext = testContext;
	}

	/** Uses the run's session: LoginApi logs in on the first call, then reuses it. */
	@Given("the user is logged in")
	public void theUserIsLoggedIn() throws IOException {
		logInformation("Use the logged-in e-commerce session");
		testContext.setAuthSession(LoginApi.session());
	}
}
