package Context;

import Resources.GlobalEnum;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
	private final Map<GlobalEnum, List<String>> createdIds = new EnumMap<>(GlobalEnum.class);
	private AuthSession authSession;

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
	 * Remember an id created by the given endpoint in this scenario, so cleanup deletes it.
	 *
	 * @param endpoint the API endpoint for which to track the created ID
	 * @param id       id of the created resource
	 */
	public void addCreatedId(GlobalEnum endpoint, String id) {
		logInformation("Track " + endpoint + " id " + id + " for cleanup");
		createdIds.computeIfAbsent(endpoint, k -> new ArrayList<>())
		          .add(id);
	}

	/**
	 * @param endpoint the create endpoint, e.g. ADD_PLACE
	 * @return ids created by that endpoint in this scenario, in creation order; empty if none (read-only)
	 */
	public List<String> getCreatedIds(GlobalEnum endpoint) {
		return Collections.unmodifiableList(createdIds.getOrDefault(endpoint, Collections.emptyList()));
	}

	/** @return the logged-in session, set by the "the user is logged in" step */
	public AuthSession getAuthSession() {
		return authSession;
	}

	/** @param authSession the logged-in session for this scenario's requests */
	public void setAuthSession(AuthSession authSession) {
		this.authSession = authSession;
	}
}