package Conventions;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Tag;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Finds the feature files on the classpath, parses them with Cucumber's Gherkin parser, and holds the
 * project's tag rules.
 */
final class FeatureFileReader {

	/** Reads feature files into their document tree; the file text and compiled pickles are not needed. */
	private static final GherkinParser PARSER = GherkinParser.builder()
	                                                         .includeSource(false)
	                                                         .includePickles(false)
	                                                         .build();

	/** Tag prefix naming the scenario's owner in the Reporting Labs report, e.g. @owner:Shantanu. */
	static final String OWNER_TAG = "@owner:";

	/** Tag prefix naming the feature area in the Reporting Labs report, e.g. @feature:Place. */
	static final String FEATURE_TAG = "@feature:";

	/** @owner: and @feature: tags; the value is a name of letters, digits, _ or -. */
	private static final Pattern METADATA_TAG = Pattern.compile("@(?:owner|feature):[A-Za-z0-9_-]+");

	/** Priority tags Reporting Labs ranks failures by; @P0 is the highest. */
	static final List<String> PRIORITY_TAGS = List.of("@P0", "@P1", "@P2", "@P3", "@P4");

	/**
	 * Severity tags this project uses, from most to least severe. Reporting Labs also knows @normal; this project
	 * does not use it.
	 */
	static final List<String> SEVERITY_TAGS = List.of("@blocker", "@critical", "@major", "@minor", "@trivial");

	/** Tags that select a kind of run, independent of the API. */
	private static final Set<String> RUN_TAGS = Set.of("@Smoke");

	/**
	 * Actions a scenario tag may name together with its feature, e.g. @CreateOrder, @DeletePlace. Add a verb when a
	 * new kind of scenario appears; a new API needs no change.
	 */
	private static final List<String> ACTION_VERBS = List.of("Add", "Create", "Delete");

	private FeatureFileReader() {
		// Prevent instantiation of this utility class
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
		URL featuresFolder = FeatureFileReader.class.getClassLoader()
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
	static List<ParsedFeature> features() throws IOException, URISyntaxException {
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
	static List<String> tagNames(List<Tag> tags) {
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
	static long countTagsWithValue(List<String> tags, String prefix) {
		return tags.stream()
		           .filter(tag -> tag.startsWith(prefix) && tag.length() > prefix.length())
		           .count();
	}

	/**
	 * Describe where a scenario is, for violation messages, e.g. OrderApi.feature:23 Scenario "Delete an order".
	 *
	 * @param parsedFeature the feature file the scenario is in
	 * @param scenario      the scenario
	 * @return file, line and scenario name
	 */
	static String scenarioLocation(ParsedFeature parsedFeature, Scenario scenario) {
		Long line = scenario.getLocation()
		                    .getLine();
		return parsedFeature.getFileName() + ":" + line + " Scenario \"" + scenario.getName() + "\"";
	}

	/**
	 * Get the scenarios of a Feature, Scenario Outlines included. Rules are not used in this project's feature files.
	 *
	 * @param feature a parsed Feature
	 * @return its scenarios, in file order
	 */
	static List<Scenario> scenarios(Feature feature) {
		return feature.getChildren()
		              .stream()
		              .map(FeatureChild::getScenario)
		              .flatMap(Optional::stream)
		              .collect(Collectors.toList());
	}

	/**
	 * Check whether a tag is allowed in a feature file.
	 *
	 * @param tag         the tag name, with its @
	 * @param featureTags the tags of this file's feature area (API and action tags)
	 * @return true if the tag is allowed
	 */
	static boolean isAllowedTag(String tag, Set<String> featureTags) {
		boolean featureOrRunTag = featureTags.contains(tag) || RUN_TAGS.contains(tag);
		boolean rankingTag = PRIORITY_TAGS.contains(tag) || SEVERITY_TAGS.contains(tag);
		return featureOrRunTag || rankingTag || METADATA_TAG.matcher(tag)
		                                                    .matches();
	}

	/**
	 * Read the feature area from the @feature: tag, e.g. "Order" from @feature:Order.
	 *
	 * @param tags the Feature's tag names
	 * @return the feature area, or empty if there is no @feature: tag with a value
	 */
	static Optional<String> featureName(List<String> tags) {
		return tags.stream()
		           .filter(tag -> tag.startsWith(FEATURE_TAG) && tag.length() > FEATURE_TAG.length())
		           .map(tag -> tag.substring(FEATURE_TAG.length()))
		           .findFirst();
	}

	/**
	 * Build the tags a feature area allows: its API tag and one action tag per verb, e.g. @OrderApi, @CreateOrder.
	 *
	 * @param featureName the feature area, e.g. "Order"
	 * @return the feature-specific tags
	 */
	static Set<String> featureTags(String featureName) {
		Set<String> tags = ACTION_VERBS.stream()
		                               .map(verb -> "@" + verb + featureName)
		                               .collect(Collectors.toSet());
		tags.add(apiTag(featureName));
		return tags;
	}

	/**
	 * Build the API tag of a feature area, e.g. @OrderApi for Order.
	 *
	 * @param featureName the feature area, e.g. "Order"
	 * @return the API tag
	 */
	static String apiTag(String featureName) {
		return "@" + featureName + "Api";
	}

	/**
	 * Describe the allowed tags for a violation message.
	 *
	 * @param featureName the feature area, e.g. "Order"
	 * @return e.g. "@OrderApi, @<Add|Create|Delete>Order, a run tag [@Smoke], a priority, …
	 */
	static String expectedTags(String featureName) {
		String actionTags = "@<" + String.join("|", ACTION_VERBS) + ">" + featureName;
		String otherTags = "a run tag " + RUN_TAGS + ", a priority, a severity or @owner/@feature";
		return apiTag(featureName) + ", " + actionTags + ", " + otherTags;
	}

	/**
	 * Describe where a Feature is, for violation messages, e.g. OrderApi.feature: Feature "Order API".
	 *
	 * @param parsedFeature the parsed feature file
	 * @return file and Feature name
	 */
	static String featureLocation(ParsedFeature parsedFeature) {
		return parsedFeature.getFileName() + ": Feature \"" + parsedFeature.getFeature()
		                                                                   .getName() + "\"";
	}

	/** A parsed feature file: its Feature and the file name used in violation messages. */
	static final class ParsedFeature {
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
}
