package Pojo;

import java.util.List;

public class AddPlace {
	
	private Location location;
	private int accuracy;
	private String name;
	private String phone_number;
	private String address;
	private List<String> types;
	private String website;
	private String language;
	
	public Location getLocation() {
		return location;
	}
	public AddPlace setLocation(Location location) {
		this.location = location;
		return this;
	}
	public int getAccuracy() {
		return accuracy;
	}
	public AddPlace setAccuracy(int accuracy) {
		this.accuracy = accuracy;
		return this;
	}
	public String getName() {
		return name;
	}
	public AddPlace setName(String name) {
		this.name = name;
		return this;
	}
	public String getPhone_number() {
		return phone_number;
	}
	public AddPlace setPhone_number(String phone_number) {
		this.phone_number = phone_number;
		return this;
	}
	public String getAddress() {
		return address;
	}
	public AddPlace setAddress(String address) {
		this.address = address;
		return this;
	}
	public List<String> getTypes() {
		return types;
	}
	public AddPlace setTypes(List<String> types) {
		this.types = types;
		return this;
	}
	public String getWebsite() {
		return website;
	}
	public AddPlace setWebsite(String website) {
		this.website = website;
		return this;
	}
	public String getLanguage() {
		return language;
	}
	public AddPlace setLanguage(String language) {
		this.language = language;
		return this;
	}

}
