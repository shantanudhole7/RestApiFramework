package Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static Logger.Log.logError;
import static Logger.Log.logWarning;

/** Reads settings from Global.properties. */
public final class ConfigReader {

	/** Location of Global.properties, relative to the project root (the folder tests are run from). */
	private static final Path PROPERTIES_FILE =
			Paths.get(System.getProperty("user.dir"), "src", "test", "java", "Resources", "Global.properties");

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
		Properties properties = new Properties();
		try (InputStream inputStream = Files.newInputStream(PROPERTIES_FILE)) {
			properties.load(inputStream);
		} catch (IOException e) {
			logError("Could not read " + PROPERTIES_FILE + " | " + e.getMessage());
			throw e;
		}
		String value = properties.getProperty(globalKey);
		if (value == null) {
			logWarning("Key '" + globalKey + "' not found in " + PROPERTIES_FILE.getFileName());
		}
		return value;
	}
}