package Utilities;

import io.restassured.response.Response;

import java.io.IOException;

/**
 * One API call that can be sent again, written as a lambda ({@code () -> orderApi.deleteOrder(id, session)}).
 * Java's form of a C# {@code Func<Response>}, with {@code IOException} allowed. Used by the retry helper.
 */
@FunctionalInterface
public interface ApiCall {

	/**
	 * Send the call.
	 *
	 * @return the response of the call
	 * @throws IOException on a network error, or if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	Response call() throws IOException;
}
