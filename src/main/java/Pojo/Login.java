package Pojo;

/**
 * Request body for the e-commerce Login API.
 */
public class Login {

	private String userEmail;
	private String userPassword;

	/**
	 * @return the login email
	 */
	public String getUserEmail() {
		return userEmail;
	}

	/**
	 * @param userEmail the login email
	 * @return this Login, for chaining
	 */
	public Login setUserEmail(String userEmail) {
		this.userEmail = userEmail;
		return this;
	}

	/**
	 * @return the login password
	 */
	public String getUserPassword() {
		return userPassword;
	}

	/**
	 * @param userPassword the login password
	 * @return this Login, for chaining
	 */
	public Login setUserPassword(String userPassword) {
		this.userPassword = userPassword;
		return this;
	}
}