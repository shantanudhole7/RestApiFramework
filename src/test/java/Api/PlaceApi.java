package Api;

import Pojo.AddPlace;
import Resources.GlobalEnum;
import Resources.TestDataBuilder;
import Resources.Utils;
import io.restassured.response.Response;

import java.io.IOException;

import static Logger.Log.logInformation;
import static io.restassured.RestAssured.given;

/**
 * Calls the Place API endpoints. Each method sends one request built from the shared request spec,
 * checks it against the shared response spec, and returns the response; callers store state and assert.
 */
public class PlaceApi extends Utils {

	/**
	 * Add a place through the Add Place API.
	 *
	 * @param payload place to add, built by TestDataBuilder
	 * @return response of the Add Place call; place_id is in its body
	 * @throws IOException if Global.properties cannot be read
	 */
	public Response addPlace(AddPlace payload) throws IOException {
		logInformation("Add place '" + payload.getName() + "' with " + GlobalEnum.ADD_PLACE);
		return given().spec(defaultRequestSpecification())
		              .body(payload)
		              .when()
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
		TestDataBuilder testDataBuilder = new TestDataBuilder();
		logInformation("Delete place " + placeId + " with " + GlobalEnum.DELETE_PLACE);
		return given().spec(defaultRequestSpecification())
		              .body(testDataBuilder.deletePlacePayload(placeId))
		              .when()
		              .post(GlobalEnum.DELETE_PLACE.getResource())
		              .then()
		              .spec(defaultResponseSpecification())
		              .extract()
		              .response();
	}
}