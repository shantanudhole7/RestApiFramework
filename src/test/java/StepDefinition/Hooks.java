package StepDefinition;

import Api.PlaceApi;
import Context.TestContext;
import Resources.TestDataBuilder;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.List;

import static Assertion.Assertions.assertHttpOk;
import static Logger.Log.logInformation;
import static Logger.Log.logWarning;
import static Resources.Utils.getJsonPath;

/**
 * Cucumber hooks: setup and cleanup that run around scenarios. Setup hooks are selected by tag;
 * the cleanup hook runs after every scenario.
 */
public class Hooks {

	/**
	 * Delete Place status when the place was deleted.
	 */
	private static final int HTTP_OK = 200;

	/**
	 * Delete Place status when the place no longer exists.
	 */
	private static final int HTTP_NOT_FOUND = 404;

	private final TestContext testContext;

	/**
	 * Cucumber injects the same TestContext the scenario's steps use (cucumber-picocontainer),
	 * so data created here is visible to the steps.
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public Hooks(TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Create a place before each Delete Place scenario and keep its place_id, so every Delete scenario
	 * deletes a place of its own and can run alone, in any order, or as a retry.
	 *
	 * @throws IOException if Global.properties cannot be read
	 */
	@Before("@DeletePlace")
	public void createPlaceForDelete() throws IOException {
		logInformation("Hook: create a place for the Delete Place scenario");
		PlaceApi placeApi = new PlaceApi();
		TestDataBuilder testDataBuilder = new TestDataBuilder();

		Response response = placeApi.addPlace(testDataBuilder.defaultAddPlacePayload());
		String placeId = getJsonPath(response, "place_id");
		if (placeId != null) {
			// Track before the status check, so the place is cleaned up even if the check fails
			testContext.addCreatedPlaceId(placeId);
		}
		assertHttpOk(response);
		testContext.setPlaceId(placeId);
	}

	/**
	 * Delete every place created in the scenario, whether the scenario passed or failed.
	 * Cleanup never fails the scenario: a place that can't be deleted is logged as a warning instead.
	 */
	@After
	public void cleanUpCreatedPlaces() {
		List<String> placeIds = testContext.getCreatedPlaceIds();
		if (placeIds.isEmpty()) {
			return;
		}

		PlaceApi placeApi = new PlaceApi();
		for (String placeId : placeIds) {
			try {
				Response response = placeApi.deletePlace(placeId);
				int statusCode = response.statusCode();
				if (statusCode == HTTP_OK) {
					logInformation("Cleanup: deleted place " + placeId);
				} else if (statusCode == HTTP_NOT_FOUND) {
					logInformation("Cleanup: place " + placeId + " was already deleted");
				} else {
					logWarning("Cleanup: could not delete place " + placeId + " | Status: " + statusCode
							           + " | Body: " + response.asString());
				}
			} catch (Exception | AssertionError e) {
				// AssertionError too: the shared response spec throws it for a non-JSON response
				logWarning("Cleanup: could not delete place " + placeId + " | " + e.getMessage());
			}
		}
	}
}