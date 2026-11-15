package ApiDataBuilder;

import Pojo.CreateOrder;
import Pojo.OrderItem;

import java.util.Collections;

/**
 * Builds request bodies for the Order API.
 */
public class OrderApiDataBuilder {

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
}
