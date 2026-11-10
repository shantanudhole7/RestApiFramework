package Pojo;

/** Latitude and longitude of a place, nested in the Add Place request body. */
public class Location {

	private double lat;
	private double lng;

	/** @return the latitude of the place, in decimal degrees */
	public double getLat() {
		return lat;
	}

	/**
	 * @param lat the latitude of the place, in decimal degrees
	 * @return this Location, for chaining
	 */
	public Location setLat(double lat) {
		this.lat = lat;
		return this;
	}

	/** @return the longitude of the place, in decimal degrees */
	public double getLng() {
		return lng;
	}

	/**
	 * @param lng the longitude of the place, in decimal degrees
	 * @return this Location, for chaining
	 */
	public Location setLng(double lng) {
		this.lng = lng;
		return this;
	}
}