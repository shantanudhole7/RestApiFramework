package ApiDataBuilder;

import Pojo.CreateOrder;
import Pojo.OrderItem;

import java.util.Collections;

/**
 * Builds request bodies for the Order API.
 */
public class OrderApiDataBuilder {

	/** Country of the default order, placed as setup for scenarios that only need an order to exist. */
	private static final String DEFAULT_COUNTRY = "India";

	/**
	 * Build the request body for the e-commerce Create Order API.
	 *
	 * @param country   the country to ship the order to, e.g. "India"
	 * @param productId the id of the product to order
	 * @return CreateOrder payload, serialized to JSON by Jackson when sent
	 */
	public CreateOrder createOrderPayload(String country, String productId) {
		OrderItem order = new OrderItem().setCountry(country)
		                                 .setProductOrderedId(productId);
		return new CreateOrder().setOrders(Collections.singletonList(order));
	}

	/**
	 * Build Create Order data with a fixed country, for scenarios that need an order to exist
	 * but don't test its details.
	 *
	 * @param productId id of the product to order
	 * @return CreateOrder payload, serialized to JSON by Jackson when sent
	 */
	public CreateOrder defaultCreateOrderPayload(String productId) {
		return createOrderPayload(DEFAULT_COUNTRY, productId);
	}
}
