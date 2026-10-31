package Resources;

import java.util.ArrayList;
import java.util.List;

import Pojo.AddPlace;
import Pojo.Location;

/**
 * Builds request bodies for the Place API.
 */
public class TestDataBuilder {

	/** Fixed test latitude sent with every new place. */
	private static final double LATITUDE = -38.383494;

	/** Fixed test longitude sent with every new place. */
	private static final double LONGITUDE = 33.427362;

	/** Fixed location accuracy, in metres, sent with every new place. */
	private static final int ACCURACY = 50;

	/**
	 * Build the request body for the Add Place API.
	 * The location (lat/lng) and accuracy are fixed test values.
	 *
	 * @param name        place name, e.g. "Test Place One"
	 * @param address     street address of the place
	 * @param phoneNumber phone number of the place
	 * @param language    language spoken at the place, e.g. "English"
	 * @param website     website URL of the place
	 * @param types       main type of the place
	 * @return AddPlace payload, serialized to JSON by Jackson when sent
	 */
	public AddPlace addPlacePayload(String name, String address, String phoneNumber, String language, String website,
	                                String types) {
		List<String> typeList = new ArrayList<>();
		typeList.add(types);

		Location location = new Location().setLat(LATITUDE)
		                                  .setLng(LONGITUDE);
		return new AddPlace().setAccuracy(ACCURACY)
		                     .setName(name)
		                     .setPhoneNumber(phoneNumber)
		                     .setWebsite(website)
		                     .setLanguage(language)
		                     .setAddress(address)
		                     .setTypes(typeList)
		                     .setLocation(location);
	}

	/**
	 * Build the request body for the Delete Place API.
	 *
	 * @param placeId id of the place to delete, as returned by the Add Place API
	 * @return JSON body as a string, e.g. {"place_id": "abc123"}
	 */
	public String deletePlacePayload(String placeId) {
		return "{\"place_id\": \"" + placeId + "\"}";
	}
}