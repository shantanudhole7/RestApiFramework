package Context;

/**
 * The logged-in e-commerce user for the whole run: the token sent as the Authorization header,
 * and the user id that created products belong to. Immutable, so it is safe to share across scenarios.
 */
public final class AuthSession {

	private final String token;
	private final String userId;

	/**
	 * @param token  token returned by the Login API
	 * @param userId user id returned by the Login API
	 */
	public AuthSession(String token, String userId) {
		this.token = token;
		this.userId = userId;
	}

	/**
	 * @return the token for the Authorization header
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @return the logged-in user's id
	 */
	public String getUserId() {
		return userId;
	}
}