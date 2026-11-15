package Pojo;

/** One item of a Create Order request: the product to order and the country to ship it to. */
public class OrderItem {

	private String country;
	private String productOrderedId;

	/** @return the country to ship the order to, e.g. India */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to ship the order to, e.g. India
	 * @return this OrderItem, for chaining
	 */
	public OrderItem setCountry(String country) {
		this.country = country;
		return this;
	}

	/** @return the id of the product to order */
	public String getProductOrderedId() {
		return productOrderedId;
	}

	/**
	 * @param productOrderedId the id of the product to order
	 * @return this OrderItem, for chaining
	 */
	public OrderItem setProductOrderedId(String productOrderedId) {
		this.productOrderedId = productOrderedId;
		return this;
	}
}
