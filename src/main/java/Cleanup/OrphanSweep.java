package Cleanup;

import Api.OrderApi;
import Api.ProductApi;
import ApiDataBuilder.ProductApiDataBuilder;
import Context.AuthSession;
import Utilities.ConfigReader;
import io.restassured.response.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static Api.OrderApi.ORDER_NOT_FOUND_MESSAGE;
import static Api.ProductApi.PRODUCT_NOT_FOUND_MESSAGE;
import static Logger.Log.logInformation;
import static Logger.Log.logWarning;
import static Utilities.ResponseUtils.getJsonPath;
import static Utilities.Retry.sendWithRetry;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Deletes test data that earlier runs left behind (orphans): test orders first, then test products, only when they
 * belong to the test account, carry the test-product name prefix, and are older than sweep.min.age.minutes.
 * Anything uncertain is kept.
 */
public final class OrphanSweep {

	private static final String SWEEP_MIN_AGE = "sweep.min.age.minutes";

	/** A MongoDB id: exactly 24 hexadecimal characters; the first 8 are its creation time in seconds. */
	private static final String MONGO_ID_PATTERN = "[0-9a-f]{24}";

	private OrphanSweep() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Delete our old test orders, then our old test products; never throws, logs a warning instead.
	 *
	 * @param session the run's e-commerce session
	 */
	public static void run(AuthSession session) {
		try {
			int minAgeMinutes = Integer.parseInt(ConfigReader.getPropertiesValue(SWEEP_MIN_AGE));
			Duration minAge = Duration.ofMinutes(minAgeMinutes);
			Instant now = Instant.now();

			int failedOrders = sweepOrders(session, now, minAge);
			if (failedOrders > 0) {
				logWarning("Orphan sweep: " + failedOrders + " order(s) could not be deleted, products kept");
				return;
			}
			sweepProducts(session, now, minAge);
		} catch (Exception e) {
			logWarning("Orphan sweep failed unexpectedly: " + e.toString());
		}
	}

	/**
	 * Check whether a product belongs to our test user and is a test product.
	 * If ownership or naming is uncertain, the product is not considered eligible.
	 *
	 * @param productName    the product name
	 * @param productAddedBy the user ID that created the product
	 * @param userId         our test user ID
	 * @return true if the owner matches and the name has the test product prefix
	 */
	static boolean isOurTestProduct(String productName, String productAddedBy, String userId) {
		return userId.equals(productAddedBy) && hasTestPrefix(productName);
	}

	/**
	 * Check whether an order is a test order based on its product name.
	 * If the name is null or does not match the test prefix, it is not eligible.
	 *
	 * @param productName the product name associated with the order
	 * @return true if the name has the test product prefix
	 */
	static boolean isOurTestOrder(String productName) {
		return hasTestPrefix(productName);
	}

	/**
	 * Check whether a name carries the test-product prefix; a null name never does.
	 *
	 * @param name the product name
	 * @return true if the name starts with the test-product prefix
	 */
	private static boolean hasTestPrefix(String name) {
		return name != null && name.startsWith(ProductApiDataBuilder.TEST_PRODUCT_PREFIX);
	}

	/**
	 * Check whether a MongoDB ID represents data old enough to be swept.
	 * Invalid or uncertain IDs are never considered old enough.
	 *
	 * @param id     the MongoDB ID
	 * @param now    the fixed current time used for evaluation
	 * @param minAge the minimum age required for cleanup
	 * @return true if the ID timestamp is at or before now minus minAge
	 */
	static boolean isOldEnough(String id, Instant now, Duration minAge) {
		if (id == null || !id.matches(MONGO_ID_PATTERN)) {
			return false;
		}
		Instant created = Instant.ofEpochSecond(Long.parseLong(id.substring(0, 8), 16));
		return !created.isAfter(now.minus(minAge));
	}

	/**
	 * Delete our old test orders and log a summary.
	 *
	 * @param session the run's e-commerce session
	 * @param now     the fixed current time used for evaluation
	 * @param minAge  the minimum age required for cleanup
	 * @return the number of orders that could not be deleted
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	private static int sweepOrders(AuthSession session, Instant now, Duration minAge) throws IOException {
		int deletedOrders = 0;
		int failedOrders = 0;
		OrderApi orderApi = new OrderApi();
		Response ordersResponse = orderApi.getCustomerOrders(session);
		int orderResponseStatus = ordersResponse.statusCode();
		if (orderResponseStatus != HTTP_OK) {
			logWarning("Orphan sweep: could not fetch orders, status " + orderResponseStatus + ", sweep skipped");
			return 0;
		}
		List<Map<String, Object>> orders = ordersResponse.jsonPath()
		                                                 .getList("data");
		if (orders == null) {
			logWarning("Orphan sweep: no order list in the response, sweep skipped");
			return 0;
		}
		for (Map<String, Object> order : orders) {
			String orderId = (String) order.get("_id");
			String productName = (String) order.get("productName");
			if (isOurTestOrder(productName) && isOldEnough(orderId, now, minAge)) {
				String item = "order " + orderId + " (" + productName + ")";
				Response deleteResponse = sendWithRetry(() -> orderApi.deleteOrder(orderId, session),
				                                        "delete order " + orderId);
				int statusCode = deleteResponse.statusCode();
				if (statusCode == HTTP_OK) {
					logInformation("Orphan sweep: deleted " + item);
					deletedOrders++;
				} else if (isOrderNotFound(deleteResponse)) {
					logInformation("Orphan sweep: " + item + " was already gone");
				} else {
					logWarning("Orphan sweep: could not delete " + item + ", status " + statusCode);
					failedOrders++;
				}
			}
		}
		logInformation("Orphan sweep: deleted " + deletedOrders + " order(s), " + failedOrders + " failed");
		return failedOrders;
	}

	/**
	 * Delete our old test products and log a summary.
	 *
	 * @param session the run's e-commerce session
	 * @param now     the fixed current time used for evaluation
	 * @param minAge  the minimum age required for cleanup
	 * @return the number of products that could not be deleted
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	private static int sweepProducts(AuthSession session, Instant now, Duration minAge) throws IOException {
		int deletedProducts = 0;
		int failedProducts = 0;
		ProductApi productApi = new ProductApi();
		Response productResponse = productApi.getAllProducts(session);
		int productResponseStatus = productResponse.statusCode();
		if (productResponseStatus != HTTP_OK) {
			logWarning("Orphan sweep: could not fetch products, status " + productResponseStatus + ", sweep skipped");
			return 0;
		}
		List<Map<String, Object>> products = productResponse.jsonPath()
		                                                    .getList("data");
		if (products == null) {
			logWarning("Orphan sweep: no product list in the response, sweep skipped");
			return 0;
		}
		for (Map<String, Object> product : products) {
			String productId = (String) product.get("_id");
			String productName = (String) product.get("productName");
			String productAddedBy = (String) product.get("productAddedBy");
			boolean isOurs = isOurTestProduct(productName, productAddedBy, session.getUserId());
			if (isOurs && isOldEnough(productId, now, minAge)) {
				String item = "product " + productId + " (" + productName + ")";
				Response deleteResponse = sendWithRetry(() -> productApi.deleteProduct(productId, session),
				                                        "delete product " + productId);
				int statusCode = deleteResponse.statusCode();
				if (statusCode == HTTP_OK) {
					logInformation("Orphan sweep: deleted " + item);
					deletedProducts++;
				} else if (isProductNotFound(deleteResponse)) {
					logInformation("Orphan sweep: " + item + " was already gone");
				} else {
					logWarning("Orphan sweep: could not delete " + item + ", status " + statusCode);
					failedProducts++;
				}
			}
		}
		logInformation("Orphan sweep: deleted " + deletedProducts + " product(s), " + failedProducts + " failed");
		return failedProducts;
	}

	/**
	 * Check whether a delete failed only because the order no longer exists.
	 *
	 * @param response the delete-order response
	 * @return true for 400 with the "Order not found" message
	 */
	private static boolean isOrderNotFound(Response response) {
		if (response.statusCode() != HTTP_BAD_REQUEST) {
			return false;
		}
		String message = getJsonPath(response, "message");
		return message != null && message.startsWith(ORDER_NOT_FOUND_MESSAGE);
	}

	/**
	 * Check whether a delete failed only because the product no longer exists.
	 *
	 * @param response response of a Delete Product call
	 * @return true if the API reports the product as not found (400 with "Product not found…")
	 */
	private static boolean isProductNotFound(Response response) {
		if (response.statusCode() != HTTP_BAD_REQUEST) {
			return false;
		}
		String message = getJsonPath(response, "message");
		return message != null && message.startsWith(PRODUCT_NOT_FOUND_MESSAGE);
	}
}
