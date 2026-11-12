package Api;

import Context.AuthSession;
import Pojo.Product;
import Resources.GlobalEnum;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.IOException;

import static Logger.Log.logInformation;
import static Specification.Specifications.AUTHORIZATION_HEADER;
import static Specification.Specifications.defaultResponseSpecification;
import static Specification.Specifications.ecomRequestSpecification;
import static io.restassured.RestAssured.given;

/**
 * Builds and sends Product API requests with the session's token; callers assert.
 */
public class ProductApi {

	/**
	 * Build a Create Product request: the product's fields and image as multipart form data, plus the token.
	 * Steps store it, and the generic http request step sends it through ApiClient, which registers the new product
	 * for cleanup
	 *
	 * @param product the product data, built by ProductApiDataBuilder
	 * @param session the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public RequestSpecification createProductRequest(Product product, AuthSession session) throws IOException {
		logInformation("Build Create Product request for '" + product.getProductName() + "'");
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
