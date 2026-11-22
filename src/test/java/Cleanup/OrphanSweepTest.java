package Cleanup;

import ApiDataBuilder.ProductApiDataBuilder;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static Assertion.Assertions.assertEquals;

/** Unit tests for the orphan sweep's selection rules: what is ours, and what is old enough. No API calls. */
public class OrphanSweepTest {

	private static final Instant NOW = Instant.parse("2026-11-22T12:00:00Z");
	private static final Duration MIN_AGE = Duration.ofMinutes(30);
	private static final String OUR_USER_ID = "user-123";
	private static final String OTHER_USER_ID = "user-456";

	/**
	 * Build a fake MongoDB id created at the given time (the first 8 hex characters are the seconds).
	 *
	 * @param time when the fake id was created
	 * @return a 24-character fake id
	 */
	private static String idCreatedAt(Instant time) {
		return String.format("%08x", time.getEpochSecond()) + "0000000000000000";
	}

	/** Our test product (owner and prefix match) is selected. */
	@Test
	public void ourTestProductIsSwept() {
		boolean result = OrphanSweep.isOurTestProduct(ProductApiDataBuilder.TEST_PRODUCT_PREFIX + " 3", OUR_USER_ID,
		                                              OUR_USER_ID);
		assertEquals("Verify our test product is eligible", true, result);
	}

	/** Admin-owned test product is not selected. */
	@Test
	public void adminProductIsNeverSwept() {
		boolean result = OrphanSweep.isOurTestProduct(ProductApiDataBuilder.TEST_PRODUCT_PREFIX + " 3", "admin",
		                                              OUR_USER_ID);
		assertEquals("Verify admin product is not eligible", false, result);
	}

	/** Our manually named product (prefix does not match) is not selected. */
	@Test
	public void ourManualProductIsNeverSwept() {
		boolean result = OrphanSweep.isOurTestProduct("ABC", OUR_USER_ID, OUR_USER_ID);
		assertEquals("Verify manual product is not eligible", false, result);
	}

	/** Another user's test product is not selected. */
	@Test
	public void otherUsersTestProductIsNeverSwept() {
		boolean result = OrphanSweep.isOurTestProduct(ProductApiDataBuilder.TEST_PRODUCT_PREFIX + " 3", OTHER_USER_ID,
		                                              OUR_USER_ID);
		assertEquals("Verify another user's test product is not eligible", false, result);
	}

	/** Our test order (prefix match) is selected. */
	@Test
	public void ourTestOrderIsSwept() {
		boolean result = OrphanSweep.isOurTestOrder(ProductApiDataBuilder.TEST_PRODUCT_PREFIX + " 1");
		assertEquals("Verify test order is eligible", true, result);
	}

	/** Manual order (prefix does not match) is not selected. */
	@Test
	public void manualOrderIsNeverSwept() {
		boolean result = OrphanSweep.isOurTestOrder("ADIDAS ORIGINAL");
		assertEquals("Verify manual order is not eligible", false, result);
	}

	/** Data older than the minimum age is selected. */
	@Test
	public void oldDataIsOldEnough() {
		Instant created = NOW.minus(Duration.ofMinutes(31));
		String id = idCreatedAt(created);
		boolean result = OrphanSweep.isOldEnough(id, NOW, MIN_AGE);
		assertEquals("Verify data older than minimum age is eligible", true, result);
	}

	/** Recent data (younger than the minimum age) is not selected. */
	@Test
	public void recentDataIsNotOldEnough() {
		Instant created = NOW.minus(Duration.ofMinutes(5));
		String id = idCreatedAt(created);
		boolean result = OrphanSweep.isOldEnough(id, NOW, MIN_AGE);
		assertEquals("Verify recent data is not eligible", false, result);
	}

	/** A malformed, negative or null id is never selected. */
	@Test
	public void malformedIdIsNeverOldEnough() {
		String negativeId = "-" + idCreatedAt(NOW.minus(Duration.ofDays(1))).substring(1);
		boolean negativeResult = OrphanSweep.isOldEnough(negativeId, NOW, MIN_AGE);
		boolean invalidStringResult = OrphanSweep.isOldEnough("not-an-id", NOW, MIN_AGE);
		boolean nullResult = OrphanSweep.isOldEnough(null, NOW, MIN_AGE);
		assertEquals("Verify negative ID is not eligible", false, negativeResult);
		assertEquals("Verify malformed ID is not eligible", false, invalidStringResult);
		assertEquals("Verify null ID is not eligible", false, nullResult);
	}

	/** Data exactly at the minimum age is selected. */
	@Test
	public void dataExactlyAtMinimumAgeIsOldEnough() {
		Instant created = NOW.minus(MIN_AGE);
		String id = idCreatedAt(created);
		boolean result = OrphanSweep.isOldEnough(id, NOW, MIN_AGE);
		assertEquals("Verify data exactly at minimum age is eligible", true, result);
	}
}
