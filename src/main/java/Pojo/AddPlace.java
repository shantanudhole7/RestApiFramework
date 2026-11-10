package Pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Request body for the Add Place API, serialized to JSON by Jackson. */
public class AddPlace {

	private Location location;
	private int accuracy;
	private String name;
	@JsonProperty("phone_number")
	private String phoneNumber;
	private String address;
	private List<String> types;
	private String website;
	private String language;

	/** @return the location of the place */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location of the place
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setLocation(Location location) {
		this.location = location;
		return this;
	}

	/** @return the accuracy of the place, in metres */
	public int getAccuracy() {
		return accuracy;
	}

	/**
	 * @param accuracy the accuracy of the place, in metres
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setAccuracy(int accuracy) {
		this.accuracy = accuracy;
		return this;
	}

	/** @return the name of the place */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name of the place
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setName(String name) {
		this.name = name;
		return this;
	}

	/** @return the phoneNumber of the place */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param phoneNumber the phone number of the place
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	/** @return the address of the place */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address of the place
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setAddress(String address) {
		this.address = address;
		return this;
	}

	/** @return the types of the place, e.g. "Engineer" */
	public List<String> getTypes() {
		return types;
	}

	/**
	 * @param types the types of the place, e.g. "Engineer"
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setTypes(List<String> types) {
		this.types = types;
		return this;
	}

	/** @return the website URL of the place */
	public String getWebsite() {
		return website;
	}

	/**
	 * @param website the website URL of the place
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setWebsite(String website) {
		this.website = website;
		return this;
	}

	/** @return the language spoken at the place */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language spoken at the place
	 * @return this AddPlace, for chaining
	 */
	public AddPlace setLanguage(String language) {
		this.language = language;
		return this;
	}
}