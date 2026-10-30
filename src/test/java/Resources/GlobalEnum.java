package Resources;

/**
 * Endpoint paths of the Place API.
 * Each path is appended to the base URI from Global.properties.
 * Feature files refer to these by constant name, e.g. "ADD_PLACE", which the steps
 * resolve with {@code GlobalEnum.valueOf(...)}.
 */
public enum GlobalEnum {

	ADD_PLACE("/maps/api/place/add/json"),
	GET_PLACE("/maps/api/place/get/json"),
	DELETE_PLACE("/maps/api/place/delete/json");

	private final String resource;

	/**
	 * @param resource endpoint path, relative to the base URI
	 */
	GlobalEnum(String resource) {
		this.resource = resource;
	}

	/**
	 * @return the endpoint path, e.g. "/maps/api/place/add/json"
	 */
	public String getResource() {
		return resource;
	}
}