package StepDefinition.Hooks;

import Api.LoginApi;
import Cleanup.OrphanSweep;
import Context.AuthSession;
import io.cucumber.java.BeforeAll;

import static Logger.Log.logWarning;

/**
 * Common hooks executed once for the entire Cucumber test run.
 * Handles cross-cutting setup that is not specific to an individual scenario.
 */
public class CommonHooks {

	/**
	 * Delete test data that earlier runs left behind (orphans).
	 * This cleanup is best-effort and must never fail the test run.
	 */
	@BeforeAll
	public static void cleanUpPreviousTestData() {
		AuthSession session;
		try {
			session = LoginApi.session();
		} catch (Exception | AssertionError e) {
			// AssertionError too: nothing may escape a @BeforeAll
			logWarning("Orphan sweep skipped: could not log in: " + e);
			return;
		}
		OrphanSweep.run(session);
	}
}
