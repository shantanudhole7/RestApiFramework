package Conventions;

import org.junit.Test;

import Conventions.FeatureFileReader.ParsedFeature;

import static Conventions.FeatureFileReader.countTagsWithValue;
import static Conventions.FeatureFileReader.features;
import static Conventions.FeatureFileReader.tagNames;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static Assertion.Assertions.assertNoViolations;

/**
 * Convention tests for the feature files: every Feature and Scenario must carry the tags and steps the project's rules
 * require. Parses the files with Cucumber's Gherkin parser; calls no API.
 */
public class FeatureFileConventionsTest {

	/** Tag prefix naming the scenario's owner in the Reporting Labs report, e.g. @owner:Shantanu. */
	private static final String OWNER_TAG = "@owner:";

	/** Tag prefix naming the feature area in the Reporting Labs report, e.g. @feature:Place. */
	private static final String FEATURE_TAG = "@feature:";

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
}
