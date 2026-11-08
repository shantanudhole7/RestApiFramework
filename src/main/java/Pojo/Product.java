package Pojo;

public class Product {
	private String productName;
	private String productAddedBy;
	private String productCategory;
	private String productSubCategory;
	private String productDescription;
	private String productPrice;
	private String productFor;
	private String productImage;

	/** @return the product name */
	public String getProductName() {
		return productName;
	}

	/** @param productName the product name @return this Product, for chaining */
	public Product setProductName(String productName) {
		this.productName = productName;
		return this;
	}

	/** @return the id of the user who adds the product */
	public String getProductAddedBy() {
		return productAddedBy;
	}

	/** @param productAddedBy the product name @return this Product, for chaining */
	public Product setProductAddedBy(String productAddedBy) {
		this.productAddedBy = productAddedBy;
		return this;
	}

	/** @return the category, e.g. "fashion" */
	public String getProductCategory() {
		return productCategory;
	}

	/** @param productCategory the category @return this Product, for chaining */
	public Product setProductCategory(String productCategory) {
		this.productCategory = productCategory;
		return this;
	}

	/** @return the sub-category, e.g. "shirts" */
	public String getProductSubCategory() {
		return productSubCategory;
	}

	/** @param productSubCategory the sub-category @return this Product, for chaining */
	public Product setProductSubCategory(String productSubCategory) {
		this.productSubCategory = productSubCategory;
		return this;
	}

	/** @return the product description */
	public String getProductDescription() {
		return productDescription;
	}

	/** @param productDescription the product description @return this Product, for chaining */
	public Product setProductDescription(String productDescription) {
		this.productDescription = productDescription;
		return this;
	}

	/** @return the price, as the API expects it (a string) */
	public String getProductPrice() {
		return productPrice;
	}

	/** @param productPrice the price @return this Product, for chaining */
	public Product setProductPrice(String productPrice) {
		this.productPrice = productPrice;
		return this;
	}

	/** @return who the product is for, e.g. "Men" */
	public String getProductFor() {
		return productFor;
	}

	/** @param productFor who the product is for @return this Product, for chaining */
	public Product setProductFor(String productFor) {
		this.productFor = productFor;
		return this;
	}

	/** @return path of the image file to upload, relative to the project root */
	public String getProductImage() {
		return productImage;
	}

	/** @param productImage path of the image file to upload @return this Product, for chaining */
	public Product setProductImage(String productImage) {
		this.productImage = productImage;
		return this;
	}
}
