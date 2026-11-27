package Conventions;

import Conventions.FeatureFileReader.ParsedFeature;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepKeywordType;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static Assertion.Assertions.assertNoViolations;
import static Conventions.FeatureFileReader.countTagsWithValue;
import static Conventions.FeatureFileReader.features;
import static Conventions.FeatureFileReader.scenarioLocation;
import static Conventions.FeatureFileReader.scenarios;
import static Conventions.FeatureFileReader.tagNames;

/**
 * Convention tests for the feature files: every Feature and Scenario must carry the tags and steps the project's rules
 * require. Parses the files with Cucumber's Gherkin parser; calls no API.
 */
public class FeatureFileConventionsTest {

	/** Tag prefix naming the scenario's owner in the Reporting Labs report, e.g. @owner:Shantanu. */
	private static final String OWNER_TAG = "@owner:";

	/** Tag prefix naming the feature area in the Reporting Labs report, e.g. @feature:Place. */
	private static final String FEATURE_TAG = "@feature:";

	/** Priority tags Reporting Labs ranks failures by; @P0 is the highest. */
	private static final List<String> PRIORITY_TAGS = List.of("@P0", "@P1", "@P2", "@P3", "@P4");

	/** Severity tags Reporting Labs understands. */
	private static final List<String> SEVERITY_TAGS = List.of("@blocker", "@critical", "@major", "@minor", "@trivial");

	/** Every Feature names exactly one owner and one feature area, for the Reporting Labs report. */
	@Test
	public void everyFeatureHasOwnerAndFeatureTags() throws IOException, URISyntaxException {
		List<String> violations = new ArrayList<>();
		for (ParsedFeature parsedFeature : features()) {
			List<String> tags = tagNames(parsedFeature.getFeature()
			                                          .getTags());
			String where = parsedFeature.getFileName() + ": Feature \"" + parsedFeature.getFeature()
			                                                                           .getName() + "\"";
			if (countTagsWithValue(tags, OWNER_TAG) != 1) {
				violations.add(where + " needs exactly one " + OWNER_TAG + "<Name> tag");
			}
			if (countTagsWithValue(tags, FEATURE_TAG) != 1) {
				violations.add(where + " needs exactly one " + FEATURE_TAG + "<Name> tag");
			}
		}
		assertNoViolations("Verify every feature has an owner and a feature tag", violations);
	}

	/** Every Scenario has exactly one priority and one severity, so the report can rank failures. */
	@Test
	public void everyScenarioHasPriorityAndSeverity() throws IOException, URISyntaxException {
		List<String> violations = new ArrayList<>();
		for (ParsedFeature parsedFeature : features()) {
			for (Scenario scenario : scenarios(parsedFeature.getFeature())) {
				List<String> tags = tagNames(scenario.getTags());
				String where = scenarioLocation(parsedFeature, scenario);
				long priorities = tags.stream()
				                      .filter(PRIORITY_TAGS::contains)
				                      .count();
				if (priorities != 1) {
					violations.add(where + " needs exactly one priority tag (@P0–@P4)");
				}
				long severities = tags.stream()
				                      .filter(SEVERITY_TAGS::contains)
				                      .count();
				if (severities != 1) {
					violations.add(where + " needs exactly one severity tag " + SEVERITY_TAGS);
				}
			}
		}
		assertNoViolations("Verify every scenario has a priority and a severity tag", violations);
	}

	/** Every Scenario checks an outcome: at least one Then step. */
	@Test
	public void everyScenarioHasAThenStep() throws IOException, URISyntaxException {
		List<String> violations = new ArrayList<>();
		for (ParsedFeature parsedFeature : features()) {
			for (Scenario scenario : scenarios(parsedFeature.getFeature())) {
				String where = scenarioLocation(parsedFeature, scenario);
				boolean hasThenStep = scenario.getSteps()
				                              .stream()
				                              .map(Step::getKeywordType)
				                              .flatMap(Optional::stream)
				                              .anyMatch(StepKeywordType.OUTCOME::equals);
				if (!hasThenStep) {
					violations.add(where + " has no Then step");
				}
			}
		}
		assertNoViolations("Verify every scenario has a Then step", violations);
	}
}
