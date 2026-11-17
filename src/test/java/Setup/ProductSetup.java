package Setup;

import Api.ProductApi;
import ApiDataBuilder.ProductApiDataBuilder;
import Context.AuthSession;
import Context.TestContext;
import Pojo.Product;
import Resources.GlobalEnum;
import io.restassured.response.Response;

import java.io.IOException;

import static Assertion.Assertions.assertHttpStatus;
import static Utilities.ResponseUtils.getJsonPath;
import static java.net.HttpURLConnection.HTTP_CREATED;

/**
 * Creates products that scenarios need as setup, and registers them for cleanup.
 * Shared by hooks and steps, so the create-register-assert sequence lives in one place.
 */
public class ProductSetup {

	private final TestContext testContext;

	/**
	 * Cucumber injects the same TestContext the scenario's steps and hooks use (cucumber-picocontainer),
	 * so products created here are registered for that scenario's cleanup.
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public ProductSetup(TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Create a product with the default test values, register it for cleanup, check it was created,
	 * and store its id in TestContext as the scenario's product.
	 *
	 * @param session the logged-in session
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public void createDefaultProduct(AuthSession session) throws IOException {
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
