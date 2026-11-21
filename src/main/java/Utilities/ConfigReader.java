package Utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static Logger.Log.logError;
import static Logger.Log.logWarning;

/** Reads settings from Global.properties and credentials from the git-ignored secrets.properties. */
public final class ConfigReader {

	/** Settings file on the test classpath (src/test/resources). */
	private static final String PROPERTIES_FILE = "Global.properties";

	/** Credentials file on the test classpath (src/test/resources): local, git-ignored and never committed. */
	private static final String SECRETS_FILE = "secrets.properties";

	private ConfigReader() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Read a value from Global.properties.
	 * The file is read from the test classpath (the build copies it from src/test/resources), so an edit takes
	 * effect on the next run. Logs a warning if the key is missing, and an error if the file cannot be read.
	 *
	 * @param globalKey the key to look up, e.g. "api.base.uri"
	 * @return the value for the key, or null if the key is not in the file
	 * @throws IOException if Global.properties cannot be found or read
	 */
	public static String getPropertiesValue(String globalKey) throws IOException {
		String value = loadProperties(PROPERTIES_FILE).getProperty(globalKey);
		if (value == null) {
			logWarning("Key '" + globalKey + "' not found in " + PROPERTIES_FILE);
		}
		return value;
	}

	/**
	 * Load a properties file, logging an error if it cannot be read.
	 *
	 * @param resourceName name of the properties file on the classpath, e.g. "Global.properties"
	 * @return the properties loaded from the file
	 * @throws IOException if properties file cannot be read
	 */
	private static Properties loadProperties(String resourceName) throws IOException {
		Properties properties = new Properties();
		try (InputStream inputStream = ConfigReader.class.getClassLoader()
		                                                 .getResourceAsStream(resourceName)) {
			if (inputStream == null) {
				throw new FileNotFoundException(resourceName + " not found on the classpath");
			}
			properties.load(inputStream);
		} catch (IOException e) {
			logError("Could not read " + resourceName + " | " + e.getMessage());
			throw e;
		}
		return properties;
	}

	/**
	 * Read a required credential from secrets.properties. The file is git-ignored; create it by copying
	 * secrets.properties.example. Values are never logged; only the key and file name appear in errors.
	 *
	 * @param secretKey the key to look up, e.g. "ecom.user.email"
	 * @return the value for the key, without surrounding whitespace
	 * @throws IOException           if secrets.properties cannot be read
	 * @throws IllegalStateException if secrets.properties is missing, or the key is missing or blank
	 */
	public static String getSecretValue(String secretKey) throws IOException {
		if (ConfigReader.class.getClassLoader()
		                      .getResource(SECRETS_FILE) == null) {
			logError(SECRETS_FILE + " not found");
			throw new IllegalStateException(
					SECRETS_FILE + " not found. Copy src/test/resources/secrets.properties.example "
							+ "to secrets.properties in the same folder and fill in the values; see README → Setup.");
		}
		String value = loadProperties(SECRETS_FILE).getProperty(secretKey);
		if (value == null || value.trim()
		                          .isEmpty()) {
			logError("Secret '" + secretKey + "' is not set in " + SECRETS_FILE);
			throw new IllegalStateException("Secret '" + secretKey + "' is not set in " + SECRETS_FILE);
		}
		// Trailing spaces are easy to miss in a properties file and would make the login fail
		return value.trim();
	}
}