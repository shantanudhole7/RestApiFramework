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

	/** This API answers 400 with this message for a missing product, not 404 (spelled "foundd" on delete). */
	public static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found";

	/**
	 * Build a Create Product request: the product's fields and image as multipart form data, plus the token.
	 * Steps store it, and the generic http request step sends it through ApiClient, which registers the new product
	 * for cleanup.
	 *
	 * @param product the product data, built by ProductApiDataBuilder
	 * @param session the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
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
	 * Delete a product through the Delete Product API.
	 *
	 * @param productId id of the product to delete, as returned by the Create Product API
	 * @param session   the logged-in session
	 * @return response of the Delete Product call
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public Response deleteProduct(String productId, AuthSession session) throws IOException {
		logInformation("Delete product " + productId + " with " + GlobalEnum.DELETE_PRODUCT);
		return deleteProductRequest(productId, session).when()
		                                               .delete(GlobalEnum.DELETE_PRODUCT.getResource())
		                                               .then()
		                                               .spec(defaultResponseSpecification())
		                                               .extract()
		                                               .response();
	}

	/**
	 * Build a Get Product request for one product, with the session's token. Sent by the generic http request step.
	 *
	 * @param productId id of the product to fetch, as returned by the Create Product API
	 * @param session   the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public RequestSpecification getProductRequest(String productId, AuthSession session) throws IOException {
		return productRequest(productId, session);
	}

	/**
	 * Create a product through the Create Product API.
	 *
	 * @param product the product data, built by ProductApiDataBuilder
	 * @param session the logged-in session
	 * @return response of the Create Product call; productId is in its body
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
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
	 * Build a Delete Product request for one product, with the session's token.
	 * Used by steps (which send it through the generic http request step) and by deleteProduct().
	 *
	 * @param productId id of the product to delete, as returned by the Create Product API
	 * @param session   the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public RequestSpecification deleteProductRequest(String productId, AuthSession session) throws IOException {
		return productRequest(productId, session);
	}

	/**
	 * Build a request for one product: the e-commerce spec, the session's token and the productId path parameter.
	 * Shared by getProductRequest and deleteProductRequest, whose requests are built the same way.
	 *
	 * @param productId id of the product the request is about
	 * @param session   the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	private RequestSpecification productRequest(String productId, AuthSession session) throws IOException {
		return given().spec(ecomRequestSpecification())
		              .header(AUTHORIZATION_HEADER, session.getToken())
		              .pathParam("productId", productId);
	}
}
