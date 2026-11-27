package Conventions;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Tag;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	/** Reads feature files into their document tree; the file text and compiled pickles are not needed. */
	private static final GherkinParser PARSER = GherkinParser.builder()
	                                                         .includeSource(false)
	                                                         .includePickles(false)
	                                                         .build();

	/** A parsed feature file: its Feature and the file name used in violation messages. */
	private static final class ParsedFeature {
		private final String fileName;
		private final Feature feature;

		private ParsedFeature(String fileName, Feature feature) {
			this.fileName = fileName;
			this.feature = feature;
		}

		public Feature getFeature() {
			return feature;
		}

		public String getFileName() {
			return fileName;
		}
	}

	/**
	 * Find every feature file under classpath:features, the folder the runner reads.
	 *
	 * @return the paths of all .feature files
	 * @throws IOException           if the folder cannot be read
	 * @throws URISyntaxException    if the folder's location is not a valid URI
	 * @throws IllegalStateException if the folder is missing or holds no .feature files
	 */
	private static List<Path> featureFiles() throws IOException, URISyntaxException {
		URL featuresFolder = FeatureFileConventionsTest.class.getClassLoader()
		                                                     .getResource("features");
		if (featuresFolder == null) {
			String sourceFolder = "src/test/resources/features";
			throw new IllegalStateException("Folder 'features' not found on the classpath (" + sourceFolder + ")");
		}
		Path root = Paths.get(featuresFolder.toURI());
		List<Path> files;
		try (Stream<Path> paths = Files.walk(root)) {
			files = paths.filter(Files::isRegularFile)
			             .filter(path -> path.getFileName()
			                                 .toString()
			                                 .endsWith(".feature"))
			             .collect(Collectors.toList());
		}
		if (files.isEmpty()) {
			throw new IllegalStateException("No .feature files found under classpath:features");
		}
		return files;
	}

	/**
	 * Parse every feature file into its Feature.
	 *
	 * @return one ParsedFeature per file
	 * @throws IOException           if a file cannot be read
	 * @throws URISyntaxException    if the features folder's location is not a valid URI
	 * @throws IllegalStateException if a file cannot be parsed or has no Feature
	 */
	private static List<ParsedFeature> features() throws IOException, URISyntaxException {
		List<ParsedFeature> result = new ArrayList<>();
		for (Path path : featureFiles()) {
			String fileName = path.getFileName()
			                      .toString();
			List<Envelope> envelopes;
			try (Stream<Envelope> stream = PARSER.parse(path)) {
				envelopes = stream.collect(Collectors.toList());
			}
			for (Envelope envelope : envelopes) {
				if (envelope.getParseError()
				            .isPresent()) {
					String parseError = envelope.getParseError()
					                            .get()
					                            .getMessage();
					throw new IllegalStateException(fileName + ": parse error: " + parseError);
				}
			}
			String noFeature = fileName + ": no Feature found";
			Feature feature = envelopes.stream()
			                           .map(Envelope::getGherkinDocument)
			                           .flatMap(Optional::stream)
			                           .map(GherkinDocument::getFeature)
			                           .flatMap(Optional::stream)
			                           .findFirst()
			                           .orElseThrow(() -> new IllegalStateException(noFeature));
			result.add(new ParsedFeature(fileName, feature));
		}
		return result;
	}

	/**
	 * Get the names of tags, each with its @, e.g. "@owner:Shantanu".
	 *
	 * @param tags the tags of a Feature or Scenario
	 * @return their names
	 */
	private static List<String> tagNames(List<Tag> tags) {
		return tags.stream()
		           .map(Tag::getName)
		           .collect(Collectors.toList());
	}

	/**
	 * Count the tags that start with a prefix and carry a value after it.
	 *
	 * @param tags   tag names, with their @
	 * @param prefix the prefix, e.g. "@owner:"
	 * @return how many tags have the prefix and a non-empty value
	 */
	private static long countTagsWithValue(List<String> tags, String prefix) {
		return tags.stream()
		           .filter(tag -> tag.startsWith(prefix) && tag.length() > prefix.length())
		           .count();
	}

	/** Every Feature names exactly one owner and one feature area, for the Reporting Labs report. */
	@Test
	public void everyFeatureHasOwnerAndFeatureTags() throws IOException, URISyntaxException {
		List<String> violations = new ArrayList<>();
		for (ParsedFeature parsedFeature : features()) {
			List<String> tags = tagNames(parsedFeature.getFeature().getTags());
			String where = parsedFeature.getFileName() + ": Feature \"" + parsedFeature.getFeature().getName() + "\"";
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
