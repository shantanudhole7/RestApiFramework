package Utilities;

import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static Assertion.Assertions.assertHttpStatus;
import static Assertion.Assertions.assertEquals;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static org.junit.Assert.assertThrows;

/**
 * Unit tests for Retry: which failures are retried, how often, and what is returned or thrown.
 * Uses fake calls built with ResponseBuilder, so no request reaches the API.
 */
public class RetryTest {

	private static final int ATTEMPTS = 2;
	private static final long DELAY_MS = 0;
	private static final String TEST_CALL = "test call";

	/**
	 * @param statusCode the status the fake response returns
	 * @return a fake JSON response, built without calling the API
	 */
	private static Response response(int statusCode) {
		return new ResponseBuilder().setStatusCode(statusCode)
		                            .setBody("{}")
		                            .build();
	}

	/** A server error is retried, and the call's second answer (200) is returned. */
	@Test
	public void retriesServerErrorUntilSuccess() throws IOException {
		AtomicInteger calls = new AtomicInteger();
		ApiCall call = () -> {
			if (calls.incrementAndGet() == 1) {
				return response(HTTP_UNAVAILABLE);
			}
			return response(HTTP_OK);
		};
		Response response = Retry.sendWithRetry(call, TEST_CALL, ATTEMPTS, DELAY_MS);
		assertHttpStatus(response, HTTP_OK);
		assertEquals("Verify API call was sent twice", 2, calls.get());
	}

	/** A client error (4xx) is returned at once: the call is sent only once. */
	@Test
	public void returnsClientErrorWithoutRetry() throws IOException {
		AtomicInteger calls = new AtomicInteger();
		ApiCall call = () -> {
			calls.incrementAndGet();
			return response(HTTP_BAD_REQUEST);
		};
		Response response = Retry.sendWithRetry(call, TEST_CALL, ATTEMPTS, DELAY_MS);
		assertHttpStatus(response, HTTP_BAD_REQUEST);
		assertEquals("Verify API call was sent once", 1, calls.get());
	}

	/** A server error on every attempt: Retry stops after the last attempt and returns that 5xx. */
	@Test
	public void returnsLastServerErrorWhenAttemptsRunOut() throws IOException {
		AtomicInteger calls = new AtomicInteger();
		ApiCall call = () -> {
			calls.incrementAndGet();
			return response(HTTP_UNAVAILABLE);
		};
		Response response = Retry.sendWithRetry(call, TEST_CALL, ATTEMPTS, DELAY_MS);
		assertHttpStatus(response, HTTP_UNAVAILABLE);
		assertEquals("Verify API call was sent twice", 2, calls.get());
	}

	/** A network error is retried, and the call's second answer (200) is returned. */
	@Test
	public void retriesNetworkErrorUntilSuccess() throws IOException {
		AtomicInteger calls = new AtomicInteger();
		ApiCall call = () -> {
			if (calls.incrementAndGet() == 1) {
				throw new IOException("Network error");
			}
			return response(HTTP_OK);
		};
		Response response = Retry.sendWithRetry(call, TEST_CALL, ATTEMPTS, DELAY_MS);
		assertHttpStatus(response, HTTP_OK);
		assertEquals("Verify API call was sent twice", 2, calls.get());
	}

	/** The network is down every time: Retry gives up after the last attempt and throws the network error. */
	@Test
	public void throwsLastNetworkErrorWhenAttemptsRunOut() {
		AtomicInteger calls = new AtomicInteger();
		ApiCall call = () -> {
			calls.incrementAndGet();
			throw new IOException("Network error");
		};
		IOException exception = assertThrows(IOException.class,
		                                     () -> Retry.sendWithRetry(call, TEST_CALL, ATTEMPTS, DELAY_MS));
		assertEquals("Verify last network error message", "Network error", exception.getMessage());
		assertEquals("Verify API call was sent twice", 2, calls.get());
	}

	/** Zero attempts is rejected at once, instead of retrying forever. */
	@Test
	public void rejectsZeroAttempts() {
		AtomicInteger calls = new AtomicInteger();
		ApiCall call = () -> {
			calls.incrementAndGet();
			return response(HTTP_UNAVAILABLE);
		};
		assertThrows(IllegalArgumentException.class, () -> Retry.sendWithRetry(call, TEST_CALL, 0, DELAY_MS));
		assertEquals("Verify the call was never sent", 0, calls.get());
	}
}
