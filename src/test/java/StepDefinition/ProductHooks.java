package StepDefinition;

import Api.LoginApi;
import Api.ProductApi;
import ApiDataBuilder.ProductApiDataBuilder;
import Context.AuthSession;
import Context.TestContext;
import Pojo.Product;
import Resources.GlobalEnum;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.List;

import static Api.ProductApi.PRODUCT_NOT_FOUND_MESSAGE;
import static Assertion.Assertions.assertHttpStatus;
import static Logger.Log.logInformation;
import static Logger.Log.logWarning;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

/** Cucumber hooks for products: create a product for each Delete scenario, and delete every product a scenario created. */
public class ProductHooks {

	private final TestContext testContext;

	/**
	 * Cucumber injects the same TestContext the scenario's steps use (cucumber-picocontainer),
	 * so it sees the products the scenario's steps created.
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public ProductHooks(TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Delete every product created in the scenario, whether the scenario passed or failed.
	 * Cleanup never fails the scenario: a product that can't be deleted is logged as a warning instead.
	 */
	@After
	public void cleanUpCreatedProducts() {
		List<String> productIds = testContext.getCreatedIds(GlobalEnum.CREATE_PRODUCT);
		if (productIds.isEmpty()) {
			return;
		}

		ProductApi productApi = new ProductApi();
		for (String productId : productIds) {
			try {
				Response response = productApi.deleteProduct(productId, LoginApi.session());
				int statusCode = response.statusCode();
				if (statusCode == HTTP_OK) {
					logInformation("Cleanup: deleted product " + productId);
				} else if (isProductNotFound(response)) {
					logInformation("Cleanup: product " + productId + " was already deleted");
				} else {
					String details = " | Status: " + statusCode + " | Body: " + response.asString();
					logWarning("Cleanup: could not delete product " + productId + details);
				}
			} catch (Exception | AssertionError e) {
				// AssertionError too: the shared response spec throws it for a non-JSON response
				logWarning("Cleanup: could not delete product " + productId + " | " + e.getMessage());
			}
		}
	}

	/**
	 * @param response response of a Delete Product call
	 * @return true if the API reports the product as not found (400 with "Product not found…")
	 */
	private boolean isProductNotFound(Response response) {
		String message = getJsonPath(response, "message");
		return response.statusCode() == HTTP_BAD_REQUEST && message != null && message.startsWith(
				PRODUCT_NOT_FOUND_MESSAGE);
	}

	/**
	 * Create a product before each Delete Product scenario and keep its productId, so every Delete scenario
	 * deletes a product of its own. Hooks run before the Background, so the hook gets the session itself.
	 *
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	@Before("@DeleteProduct")
	public void createProductForDelete() throws IOException {
		logInformation("Hook: create a product for the Delete Product scenario");
		AuthSession session = LoginApi.session();
		ProductApiDataBuilder productApiDataBuilder = new ProductApiDataBuilder();

		Product product = productApiDataBuilder.defaultCreateProductPayload(session.getUserId());
		Response response = new ProductApi().createProduct(product, session);
		String productId = getJsonPath(response, GlobalEnum.CREATE_PRODUCT.getCreatedIdPath());
		if (productId != null) {
			// Track before the status check, so the product is cleaned up even if the check fails
			testContext.addCreatedId(GlobalEnum.CREATE_PRODUCT, productId);
		}
		assertHttpStatus(response, HTTP_CREATED);
		testContext.setProductId(productId);
	}
}
