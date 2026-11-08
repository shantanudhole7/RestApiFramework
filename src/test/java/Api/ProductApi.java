package Api;

import Context.AuthSession;
import Pojo.Product;
import Resources.GlobalEnum;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.IOException;

import static Logger.Log.logInformation;
import static Resources.Specifications.AUTHORIZATION_HEADER;
import static Resources.Specifications.defaultResponseSpecification;
import static Resources.Specifications.ecomRequestSpecification;
import static io.restassured.RestAssured.given;

/**
 * Calls the e-commerce Product API. Requests carry the session's token in the Authorization header,
 * which the e-commerce spec masks in logs and the report. No state and no assertions; callers check the response.
 */
public class ProductApi {

	/**
	 * Build a Create Product request: the product's fields and image as multipart form data, plus the token.
	 * Used by steps (which send it through the generic http request step) and by create().
	 *
	 * @param product the product data, built by TestDataBuilder
	 * @param session the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read
	 */
	public RequestSpecification createProductRequest(Product product, AuthSession session) throws IOException {
		return given().spec(ecomRequestSpecification())
		              .header(AUTHORIZATION_HEADER, session.getToken())
		              .multiPart("productName", product.getProductName())
		              .multiPart("productAddedBy", product.getProductAddedBy())
		              .multiPart("productCategory", product.getProductCategory())
		              .multiPart("productSubCategory", product.getProductSubCategory())
		              .multiPart("productPrice", product.getProductPrice())
		              .multiPart("productDescription", product.getProductDescription())
		              .multiPart("productFor", product.getProductFor())
		              .multiPart("productImage", new File(product.getProductImage()));
	}

	/**
	 * Create a product through the Create Product API.
	 *
	 * @param product the product data, built by TestDataBuilder
	 * @param session the logged-in session
	 * @return response of the Create Product call; productId is in its body
	 * @throws IOException if Global.properties cannot be read
	 */
	public Response createProduct(Product product, AuthSession session) throws IOException {
		logInformation("Create product '" + product.getProductName() + "' with " + GlobalEnum.CREATE_PRODUCT);
		return createProductRequest(product, session).when()
		                                             .post(GlobalEnum.CREATE_PRODUCT.getResource())
		                                             .then()
		                                             .spec(defaultResponseSpecification())
		                                             .extract()
		                                             .response();
	}

	/**
	 * Delete a product through the Delete Product API.
	 *
	 * @param productId id of the product to delete, as returned by the Create Product API
	 * @param session   the logged-in session
	 * @return response of the Delete Product call
	 * @throws IOException if Global.properties cannot be read
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