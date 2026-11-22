package Cleanup;

import ApiDataBuilder.ProductApiDataBuilder;

import java.time.Duration;
import java.time.Instant;

/**
 * Deletes test data that earlier runs left behind (orphans): test orders first, then test products, only when they
 * belong to the test account, carry the test-product name prefix, and are older than sweep.min.age.minutes.
 * Anything uncertain is kept.
 */
public final class OrphanSweep {

	/** A MongoDB id: exactly 24 hexadecimal characters; the first 8 are its creation time in seconds. */
	private static final String MONGO_ID_PATTERN = "[0-9a-f]{24}";

	private OrphanSweep() {
		// Prevent instantiation of this utility class
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
}
