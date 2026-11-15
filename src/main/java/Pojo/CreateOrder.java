package Pojo;

import java.util.List;

/** Request body for the e-commerce Create Order API. */
public class CreateOrder {

	private List<OrderItem> orders;

	/** @return the items to order, one per product */
	public List<OrderItem> getOrders() {
		return orders;
	}

	/**
	 * @param orders the items to order, one per product
	 * @return this CreateOrder, for chaining
	 */
	public CreateOrder setOrders(List<OrderItem> orders) {
		this.orders = orders;
		return this;
	}
}