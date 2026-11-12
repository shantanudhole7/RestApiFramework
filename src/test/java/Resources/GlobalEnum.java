package Resources;

/**
 * Endpoint paths of the Place API and the e-commerce API.
 * Each path is appended to the base URI from Global.properties.
 * Feature files refer to these by constant name, e.g. "ADD_PLACE", which the steps
 * resolve with {@code GlobalEnum.valueOf(...)}.
 * Endpoints that create something also declare the JSON path of the new id,
 * which ApiClient uses to register it for cleanup.
 */
public enum GlobalEnum {

	ADD_PLACE("/maps/api/place/add/json", "place_id"),
	GET_PLACE("/maps/api/place/get/json"),
	DELETE_PLACE("/maps/api/place/delete/json"),
	// E-commerce API
	LOGIN("/api/ecom/auth/login"),
	CREATE_PRODUCT("/api/ecom/product/add-product", "productId"),
	DELETE_PRODUCT("/api/ecom/product/delete-product/{productId}");

	private final String resource;
	private final String createdIdPath;

	/**
	 * For endpoints that create nothing.
	 *
	 * @param resource endpoint path, relative to the base URI
	 */
	GlobalEnum(String resource) {
		this(resource, null);
	}

	/**
	 * @param resource      endpoint path, relative to the base URI
	 * @param createdIdPath JSON path of the id the endpoint creates, e.g. place_id
	 */
	GlobalEnum(String resource, String createdIdPath) {
		this.resource = resource;
		this.createdIdPath = createdIdPath;
	}

	/**
	 * @return the endpoint path, e.g. "/maps/api/place/add/json"
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * JSON path of the id the endpoint creates, e.g. place_id
	 *
	 * @return the path to the created ID, e.g. "place_id", or null if the endpoint creates nothing
	 */
	public String getCreatedIdPath() {
		return createdIdPath;
	}
}