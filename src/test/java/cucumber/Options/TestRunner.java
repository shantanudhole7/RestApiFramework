package cucumber.Options;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runs every feature file with JUnit. Writes the Allure results (target/allure-results) and a single-file
 * Cucumber HTML report (target/cucumber-reports/cucumber.html).
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/java/Features",
		glue = {"StepDefinition"},
		plugin = {
				"pretty",
				"html:target/cucumber-reports/cucumber.html",
				"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
		}
)
public class TestRunner {
}