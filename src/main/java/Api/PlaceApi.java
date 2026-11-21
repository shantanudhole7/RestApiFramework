package Api;

import ApiDataBuilder.PlaceApiDataBuilder;
import Pojo.AddPlace;
import Resources.GlobalEnum;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;

import static Logger.Log.logInformation;
import static Specification.Specifications.defaultRequestSpecification;
import static Specification.Specifications.defaultResponseSpecification;
import static io.restassured.RestAssured.given;

/** Builds and sends Place API requests; callers assert. */
public class PlaceApi {

	/**
	 * Add a place through the Add Place API.
	 *
	 * @param payload place to add, built by PlaceApiDataBuilder
	 * @return response of the Add Place call; place_id is in its body
	 * @throws IOException if Global.properties cannot be read
	 */
	public Response addPlace(AddPlace payload) throws IOException {
		logInformation("Add place '" + payload.getName() + "' with " + GlobalEnum.ADD_PLACE);
		return addPlaceRequest(payload).when()
		                               .post(GlobalEnum.ADD_PLACE.getResource())
		                               .then()
		                               .spec(defaultResponseSpecification())
		                               .extract()
		                               .response();
	}

	/**
	 * Delete a place through the Delete Place API.
	 *
	 * @param placeId id of the place to delete, as returned by the Add Place API
	 * @return response of the Delete Place call; "status" is "OK" when the place was deleted
	 * @throws IOException if Global.properties cannot be read
	 */
	public Response deletePlace(String placeId) throws IOException {
		logInformation("Delete place " + placeId + " with " + GlobalEnum.DELETE_PLACE);
		return deletePlaceRequest(placeId).when()
		                                  .post(GlobalEnum.DELETE_PLACE.getResource())
		                                  .then()
		                                  .spec(defaultResponseSpecification())
		                                  .extract()
		                                  .response();
	}

	/**
	 * Build an Add Place request: the shared spec with the place as a JSON body.
	 * Used by steps (which send it through the generic http request step) and by addPlace().
	 *
	 * @param payload place to add, built by PlaceApiDataBuilder
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read
	 */
	public RequestSpecification addPlaceRequest(AddPlace payload) throws IOException {
		return given().spec(defaultRequestSpecification())
		              .body(payload);
	}

	/**
	 * Build a Delete Place request: the shared spec with place_id in a JSON body.
	 * Used by steps (which send it through the generic http request step) and by deletePlace().
	 *
	 * @param placeId id of the place to delete, as returned by the Add Place API
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read
	 */
	public RequestSpecification deletePlaceRequest(String placeId) throws IOException {
		PlaceApiDataBuilder placeApiDataBuilder = new PlaceApiDataBuilder();
		return given().spec(defaultRequestSpecification())
		              .body(placeApiDataBuilder.deletePlacePayload(placeId));
	}

	/**
	 * Build a Get Place request: the shared spec with place_id as a query parameter.
	 * Sent by the generic http request step.
	 *
	 * @param placeId id of the place to fetch, as returned by the Add Place API
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read
	 */
	public RequestSpecification getPlaceRequest(String placeId) throws IOException {
		return given().spec(defaultRequestSpecification())
		              .queryParam("place_id", placeId);
	}
}