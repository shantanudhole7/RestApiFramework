package cucumber.Options;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runs every feature file with JUnit; writes a JSON report for the HTML report plugin.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/java/Features",
		glue = {"StepDefinition"},
		plugin = {"pretty", "json:target/jsonReports/cucumber-report.json"}
)
public class TestRunner {
}