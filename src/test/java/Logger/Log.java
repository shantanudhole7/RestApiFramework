package Logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {

	private static final Logger LOGGER = LoggerFactory.getLogger("automation");

	private Log() {
		// Utility class: use the static methods.
	}

	/**
	 * Log a test step or general information.
	 *
	 * @param message the message to log
	 */
	public static void Log_Information(String message) {
		LOGGER.info(message);
	}

	/**
	 * Log something unexpected that does not fail the test.
	 *
	 * @param message the message to log
	 */
	public static void Log_Warning(String message) {
		LOGGER.warn(message);
	}

	/**
	 * Log a failure or error.
	 *
	 * @param message the message to log
	 */
	public static void Log_Error(String message) {
		LOGGER.error(message);
	}
}