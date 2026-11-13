package ApiDataBuilder;

import Pojo.Product;

/**
 * Builds request bodies for the Product API.
 */
public class ProductApiDataBuilder {

	/** Image uploaded with every test product: a small, clearly fake picture. */
	private static final String PRODUCT_IMAGE = "src/test/resources/files/product.jpeg";

	/** Name of the default product, created as setup for scenarios that only need a product to exist. */
	private static final String DEFAULT_PRODUCT_NAME = "Test Product 3";

	/** Category of the default product. */
	private static final String DEFAULT_PRODUCT_CATEGORY = "fashion";

	/** Sub-category of the default product. */
	private static final String DEFAULT_PRODUCT_SUB_CATEGORY = "T-Shirt";

	/** Description of the default product. */
	private static final String DEFAULT_PRODUCT_DESCRIPTION = "Setup product for a delete test";

	/** Price of the default product. */
	private static final String DEFAULT_PRODUCT_PRICE = "499";

	/** Who the default product is for. */
	private static final String DEFAULT_PRODUCT_FOR = "Men";

	/**
	 * Build the data for the e-commerce Create Product API. The image is a fixed test file.
	 *
	 * @param name        product name, e.g. "Test Product 1"
	 * @param category    category, e.g. "fashion"
	 * @param subCategory sub-category, e.g. "Shoes"
	 * @param description description, e.g. "Sneaker"
	 * @param price       price, as text, e.g. "1999"
	 * @param productFor  who the product is for, e.g. "Men"
	 * @param userId      id of the logged-in user, sent as productAddedBy
	 * @return Product data, sent as multipart form fields by ProductApi
	 */
	public Product createProductPayload(String name, String category, String subCategory, String description,
	                                    String price, String productFor, String userId) {
		return new Product().setProductName(name)
		                    .setProductCategory(category)
		                    .setProductSubCategory(subCategory)
		                    .setProductDescription(description)
		                    .setProductPrice(price)
		                    .setProductFor(productFor)
		                    .setProductAddedBy(userId)
		                    .setProductImage(PRODUCT_IMAGE);
	}

	/**
	 * Build Create Product data with fixed fake values, for scenarios that need a product to exist
	 * but don't test its details.
	 *
	 * @param userId id of the logged-in user, sent as productAddedBy
	 * @return Product data with the default test values
	 */
	public Product defaultCreateProductPayload(String userId) {
		return createProductPayload(DEFAULT_PRODUCT_NAME, DEFAULT_PRODUCT_CATEGORY, DEFAULT_PRODUCT_SUB_CATEGORY,
		                            DEFAULT_PRODUCT_DESCRIPTION, DEFAULT_PRODUCT_PRICE, DEFAULT_PRODUCT_FOR, userId);
	}
}
