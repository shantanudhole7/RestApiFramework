package Conventions;

import Conventions.FeatureFileReader.ParsedFeature;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepKeywordType;
import io.cucumber.messages.types.Tag;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static Assertion.Assertions.assertNoViolations;
import static Conventions.FeatureFileReader.FEATURE_TAG;
import static Conventions.FeatureFileReader.OWNER_TAG;
import static Conventions.FeatureFileReader.PRIORITY_TAGS;
import static Conventions.FeatureFileReader.SEVERITY_TAGS;
import static Conventions.FeatureFileReader.apiTag;
import static Conventions.FeatureFileReader.countTagsWithValue;
import static Conventions.FeatureFileReader.expectedTags;
import static Conventions.FeatureFileReader.featureLocation;
import static Conventions.FeatureFileReader.featureName;
import static Conventions.FeatureFileReader.featureTags;
import static Conventions.FeatureFileReader.features;
import static Conventions.FeatureFileReader.isAllowedTag;
import static Conventions.FeatureFileReader.scenarioLocation;
import static Conventions.FeatureFileReader.scenarios;
import static Conventions.FeatureFileReader.tagNames;

/**
 * Convention tests for the feature files: every Feature and Scenario must carry the tags and steps the project's rules
 * require. Parses the files with Cucumber's Gherkin parser; calls no API.
 */
public class FeatureFileConventionsTest {

	/** Every Feature names exactly one owner and one feature area, and carries the API tag that matches its area. */
	@Test
	public void everyFeatureHasOwnerFeatureAndApiTags() throws IOException, URISyntaxException {
		List<String> violations = new ArrayList<>();
		for (ParsedFeature parsedFeature : features()) {
			List<String> tags = tagNames(parsedFeature.getFeature()
			                                          .getTags());
			String where = featureLocation(parsedFeature);
			if (countTagsWithValue(tags, OWNER_TAG) != 1) {
				violations.add(where + " needs exactly one " + OWNER_TAG + "<Name> tag");
			}
			if (countTagsWithValue(tags, FEATURE_TAG) != 1) {
				violations.add(where + " needs exactly one " + FEATURE_TAG + "<Name> tag");
			}
			Optional<String> featureName = featureName(tags);
			if (featureName.isPresent() && !tags.contains(apiTag(featureName.get()))) {
				String needs = " needs the tag " + apiTag(featureName.get()) + " to match " + FEATURE_TAG;
				violations.add(where + needs + featureName.get());
			}
		}
		assertNoViolations("Verify every feature has an owner, a feature and a matching API tag", violations);
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

	/**
	 * Every tag is allowed for its feature: the feature's API tag, an action on the feature (@CreateOrder), a run tag,
	 * a priority, a severity, or @owner/@feature. Catches typos (@Smokea) and tags copied from another API.
	 * A file without a @feature: tag is skipped here; everyFeatureHasOwnerFeatureAndApiTags reports it.
	 */
	@Test
	public void everyTagIsAllowedForItsFeature() throws IOException, URISyntaxException {
		List<String> violations = new ArrayList<>();
		for (ParsedFeature parsedFeature : features()) {
			Feature feature = parsedFeature.getFeature();
			Optional<String> featureName = featureName(tagNames(feature.getTags()));
			if (featureName.isEmpty()) {
				continue;
			}
			Set<String> featureTags = featureTags(featureName.get());
			String notAllowed = " is not allowed here; expected " + expectedTags(featureName.get());
			String featureWhere = featureLocation(parsedFeature);
			for (Tag tag : feature.getTags()) {
				if (!isAllowedTag(tag.getName(), featureTags)) {
					violations.add(featureWhere + " tag " + tag.getName() + notAllowed);
				}
			}
			for (Scenario scenario : scenarios(feature)) {
				String where = scenarioLocation(parsedFeature, scenario);
				for (Tag tag : scenario.getTags()) {
					if (!isAllowedTag(tag.getName(), featureTags)) {
						violations.add(where + " tag " + tag.getName() + notAllowed);
					}
				}
			}
		}
		assertNoViolations("Verify every tag is allowed for its feature", violations);
	}
}
