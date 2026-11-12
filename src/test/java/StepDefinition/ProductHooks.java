package StepDefinition;

import Api.LoginApi;
import Api.ProductApi;
import Context.TestContext;
import Resources.GlobalEnum;
import io.cucumber.java.After;
import io.restassured.response.Response;

import java.util.List;

import static Logger.Log.logInformation;
import static Logger.Log.logWarning;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

/** Cucumber hooks for products: delete every product a scenario created. */
public class ProductHooks {

	/** This API answers 400 with this message for a missing product, not 404 (spelled "foundd" on delete). */
	private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found";

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
					logWarning("Cleanup: could not delete product " + productId + " | Status: " + statusCode
							           + " | Body: " + response.asString());
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
		return response.statusCode() == HTTP_BAD_REQUEST
				&& message != null && message.startsWith(PRODUCT_NOT_FOUND_MESSAGE);
	}
}
