package ApiDataBuilder;

import Pojo.Product;

/**
 * Builds request bodies for the Product API.
 */
public class ProductApiDataBuilder {

	/** Image uploaded with every test product: a small, clearly fake picture. */
	private static final String PRODUCT_IMAGE = "src/test/resources/files/product.jpeg";

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
}
