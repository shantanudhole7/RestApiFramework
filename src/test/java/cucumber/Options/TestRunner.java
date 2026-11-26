package cucumber.Options;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runs every feature file with JUnit. Writes a single-file Cucumber HTML report (target/cucumber-reports/cucumber.html)
 * and the Reporting Labs report (target/reporting-labs/index.html).
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		features = "classpath:features",
		glue = {"StepDefinition"},
		plugin = {
				"pretty",
				"html:target/cucumber-reports/cucumber.html",
				"dev.reportinglabs.cucumber.ReportingLabsPlugin"
		}
)
public class TestRunner {
}