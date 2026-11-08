package Context;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Logger.Log.logInformation;

/**
 * State shared by the steps and hooks of one scenario.
 * Cucumber (cucumber-picocontainer) creates a new instance for every scenario and injects the same instance
 * into every glue class that takes it in its constructor, so scenarios never share state.
 */
public class TestContext {

	private RequestSpecification requestSpecification;
	private Response response;
	private String placeId;
	private final List<String> createdPlaceIds = new ArrayList<>();
	private AuthSession authSession;
	private final List<String> createdProductIds = new ArrayList<>();

	/**
	 * @return the request being prepared for the next API call
	 */
	public RequestSpecification getRequestSpecification() {
		return requestSpecification;
	}

	/**
	 * @param requestSpecification the request to send with the next API call
	 */
	public void setRequestSpecification(RequestSpecification requestSpecification) {
		this.requestSpecification = requestSpecification;
	}

	/**
	 * @return the response of the last API call
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * @param response the response of the last API call
	 */
	public void setResponse(Response response) {
		this.response = response;
	}

	/**
	 * @return place_id of the place created in this scenario, or null if none was created
	 */
	public String getPlaceId() {
		return placeId;
	}

	/**
	 * @param placeId place_id returned by the Add Place API
	 */
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	/**
	 * Remember a place created in this scenario, so the cleanup hook deletes it after the scenario.
	 *
	 * @param placeId place_id returned by the Add Place API
	 */
	public void addCreatedPlaceId(String placeId) {
		logInformation("Track place " + placeId + " for cleanup");
		createdPlaceIds.add(placeId);
	}

	/**
	 * @return place_ids created in this scenario, in creation order (read-only)
	 */
	public List<String> getCreatedPlaceIds() {
		return Collections.unmodifiableList(createdPlaceIds);
	}

	/**
	 * @return the logged-in session, set by the "the user is logged in" step
	 */
	public AuthSession getAuthSession() {
		return authSession;
	}

	/**
	 * @param authSession the logged-in session for this scenario's requests
	 */
	public void setAuthSession(AuthSession authSession) {
		this.authSession = authSession;
	}

	/**
	 * Remember a product created in this scenario, so the cleanup hook deletes it after the scenario.
	 *
	 * @param productId productId returned by the Create Product API
	 */
	public void addCreatedProductId(String productId) {
		logInformation("Track product " + productId + " for cleanup");
		createdProductIds.add(productId);
	}

	/**
	 * @return productIds created in this scenario, in creation order (read-only)
	 */
	public List<String> getCreatedProductIds() {
		return Collections.unmodifiableList(createdProductIds);
	}
}