package Api;

import Context.AuthSession;
import Resources.GlobalEnum;
import io.restassured.response.Response;

import java.io.IOException;

import static Logger.Log.logInformation;
import static Specification.Specifications.AUTHORIZATION_HEADER;
import static Specification.Specifications.defaultResponseSpecification;
import static Specification.Specifications.ecomRequestSpecification;
import static io.restassured.RestAssured.given;

/**
 * Calls the Product API endpoints. Each method sends one request with the session's token and returns the response;
 * callers assert.
 */
public class ProductApi {

	/**
	 * Delete a product through the Delete Product API.
	 *
	 * @param productId id of the product to delete, as returned by the Create Product API
	 * @param session   the logged-in session
	 * @return response of the Delete Product call
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public Response deleteProduct(String productId, AuthSession session) throws IOException {
		logInformation("Delete product " + productId + " with " + GlobalEnum.DELETE_PRODUCT);
		return given().spec(ecomRequestSpecification())
		              .header(AUTHORIZATION_HEADER, session.getToken())
		              .pathParam("productId", productId)
		              .when()
		              .delete(GlobalEnum.DELETE_PRODUCT.getResource())
		              .then()
		              .spec(defaultResponseSpecification())
		              .extract()
		              .response();
	}
}
