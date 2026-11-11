package ApiDataBuilder;

import Pojo.Login;

/**
 * Builds request bodies for the Login API.
 */
public class LoginApiDataBuilder {

	/**
	 * Build the request body for the e-commerce Login API.
	 *
	 * @param userEmail    the login email, read from secrets.properties
	 * @param userPassword the login password, read from secrets.properties
	 * @return Login payload, serialized to JSON by Jackson when sent
	 */
	public Login loginPayload(String userEmail, String userPassword) {
		return new Login().setUserEmail(userEmail)
		                  .setUserPassword(userPassword);
	}
}
