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

	/** This API answers 400 with this message for a missing order, not 404, on both fetch and delete. */
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
	 * Build a Get Order request for one order, with the session's token and the id query parameter.
	 * Sent by the generic http request step.
	 *
	 * @param orderId id of the order to fetch, as returned by the Create Order API
	 * @param session the logged-in session
	 * @return the prepared request, not yet sent
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public RequestSpecification getOrderRequest(String orderId, AuthSession session) throws IOException {
		return orderRequest(session).queryParam("id", orderId);
	}

	/**
	 * Place an order through the Create Order API.
	 *
	 * @param createOrder the order, built by OrderApiDataBuilder
	 * @param session     the logged-in session
	 * @return response of the Create Order call; the order id is at orders[0] in its body
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public Response createOrder(CreateOrder createOrder, AuthSession session) throws IOException {
		logInformation("Place an order with " + GlobalEnum.CREATE_ORDER);
		return createOrderRequest(createOrder, session).when()
		                                               .post(GlobalEnum.CREATE_ORDER.getResource())
		                                               .then()
		                                               .spec(defaultResponseSpecification())
		                                               .extract()
		                                               .response();
	}

	/**
	 * List the logged-in user's orders. An account with no orders gets 200 with an empty "data" list.
	 *
	 * @param session the logged-in session; its user id selects the orders
	 * @return response of the Get Orders for Customer call; the orders are in "data"
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public Response getCustomerOrders(AuthSession session) throws IOException {
		logInformation("Get the orders of user " + session.getUserId() + " with " + GlobalEnum.GET_CUSTOMER_ORDERS);
		return orderRequest(session).pathParam("userId", session.getUserId())
		                            .when()
		                            .get(GlobalEnum.GET_CUSTOMER_ORDERS.getResource())
		                            .then()
		                            .spec(defaultResponseSpecification())
		                            .extract()
		                            .response();
	}

	/**
	 * Build the part every Order request shares: the e-commerce spec and the session's token.
	 * Delete adds the orderId path parameter, Get Orders for Customer the userId path parameter, and Get the id
	 * query parameter.
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
