package Api;

import Context.AuthSession;
import Pojo.CreateOrder;
import Resources.GlobalEnum;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;

import static Logger.Log.logInformation;
import static Specification.Specifications.AUTHORIZATION_HEADER;
import static Specification.Specifications.defaultResponseSpecification;
import static Specification.Specifications.ecomRequestSpecification;
import static io.restassured.RestAssured.given;

/** Builds and sends Order API requests with the session's token; callers assert. */
public class OrderApi {

	/** This API answers 400 with this message for a missing order, not 404. */
	public static final String ORDER_NOT_FOUND_MESSAGE = "Order not found";

	/**
	 * Delete an order through the Delete Order API.
	 *
	 * @param orderId id of the order to delete, as returned by the Create Order API
	 * @param session the logged-in session
	 * @return response of the Delete Order call
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public Response deleteOrder(String orderId, AuthSession session) throws IOException {
		logInformation("Delete order " + orderId + " with " + GlobalEnum.DELETE_ORDER);
		return deleteOrderRequest(orderId, session).when()
		                                           .delete(GlobalEnum.DELETE_ORDER.getResource())
		                                           .then()
		                                           .spec(defaultResponseSpecification())
		                                           .extract()
		                                           .response();
	}

	/**
	 * Build a Delete Order request for one order, with the session's token and the orderId path parameter.
	 * Used by steps (which send it through the generic http request step) and by deleteOrder().
	 *
	 * @param orderId id of the order to delete, as returned by the Create Order API
	 * @param session the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public RequestSpecification deleteOrderRequest(String orderId, AuthSession session) throws IOException {
		return orderRequest(session).pathParam("orderId", orderId);
	}

	/**
	 * Build a Create Order request: the order as a JSON body, plus the token.
	 * Steps store it, and the generic http request step sends it through ApiClient, which registers the new order
	 * for cleanup.
	 *
	 * @param createOrder the order, built by OrderApiDataBuilder
	 * @param session     the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public RequestSpecification createOrderRequest(CreateOrder createOrder, AuthSession session) throws IOException {
		return orderRequest(session).contentType(ContentType.JSON)
		                            .body(createOrder);
	}

	/**
	 * Build the part every Order request shares: the e-commerce spec and the session's token.
	 * Delete adds the orderId path parameter; Get adds the id query parameter.
	 *
	 * @param session the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	private RequestSpecification orderRequest(AuthSession session) throws IOException {
		return given().spec(ecomRequestSpecification())
		              .header(AUTHORIZATION_HEADER, session.getToken());
	}
}
