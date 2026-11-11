package Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static Logger.Log.logError;
import static Logger.Log.logWarning;

/** Reads settings from Global.properties and credentials from the git-ignored secrets.properties. */
public final class ConfigReader {

	/** Location of Global.properties, relative to the project root (the folder tests are run from). */
	private static final Path PROPERTIES_FILE =
			Paths.get(System.getProperty("user.dir"), "src", "test", "java", "Resources", "Global.properties");

	/** Location of secrets.properties: local credentials, git-ignored and never committed. */
	private static final Path SECRETS_FILE =
			Paths.get(System.getProperty("user.dir"), "src", "test", "java", "Resources", "secrets.properties");

	private ConfigReader() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Read a value from Global.properties.
	 * The file is read on every call, so changes take effect without restarting.
	 * Logs a warning if the key is missing, and an error if the file cannot be read.
	 *
	 * @param globalKey the key to look up, e.g. "api.base.uri"
	 * @return the value for the key, or null if the key is not in the file
	 * @throws IOException if Global.properties cannot be found or read
	 */
	public static String getPropertiesValue(String globalKey) throws IOException {
		String value = loadProperties(PROPERTIES_FILE).getProperty(globalKey);
		if (value == null) {
			logWarning("Key '" + globalKey + "' not found in " + PROPERTIES_FILE.getFileName());
		}
		return value;
	}

	/**
	 * Load a properties file, logging an error if it cannot be read.
	 *
	 * @param file file to read the properties value
	 * @return the properties loaded from the file
	 * @throws IOException if properties file cannot be read
	 */
	private static Properties loadProperties(Path file) throws IOException {
		Properties properties = new Properties();
		try (InputStream inputStream = Files.newInputStream(file)) {
			properties.load(inputStream);
		} catch (IOException e) {
			logError("Could not read " + file + " | " + e.getMessage());
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
		if (Files.notExists(SECRETS_FILE)) {
			logError(SECRETS_FILE.getFileName() + " not found");
			throw new IllegalStateException(
					SECRETS_FILE.getFileName() + " not found. Copy secrets.properties.example " +
							"to secrets.properties and fill in the values; see README → Setup.");
		}
		String value = loadProperties(SECRETS_FILE).getProperty(secretKey);
		if (value == null || value.trim()
		                          .isEmpty()) {
			logError("Secret '" + secretKey + "' is not set in " + SECRETS_FILE.getFileName());
			throw new IllegalStateException("Secret '" + secretKey + "' is not set in " + SECRETS_FILE.getFileName());
		}
		// Trailing spaces are easy to miss in a properties file and would make the login fail
		return value.trim();
	}
}