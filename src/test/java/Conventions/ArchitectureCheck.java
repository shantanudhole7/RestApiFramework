package Conventions;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;
import java.util.Optional;

import static Assertion.Assertions.assertNoViolations;

/**
 * Imports the project's classes once and provides the custom ArchUnit conditions
 * and the assertion the architecture convention tests use.
 */
final class ArchitectureCheck {

	/** The project's own classes (src/main and src/test, no JARs), imported once for all rules. */
	private static final JavaClasses CLASSES = new ClassFileImporter().withImportOption(
			                                                                  ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
	                                                                  .importClasspath();

	/** Name of the class every check must go through, so it is logged as PASS/FAIL. */
	private static final String ASSERTIONS_CLASS = "Assertion.Assertions";

	/** A Then step verifies its outcome through Assertions, directly or in a method of its own class. */
	static final ArchCondition<JavaMethod> CALL_ASSERTIONS = new ArchCondition<>(
			"call Assertions, directly or through a method of their own class") {
		@Override
		public void check(JavaMethod method, ConditionEvents events) {
			boolean asserts = callsAssertions(method) || callsAssertionsThroughOwnClass(method);
			String message = method.getFullName() + " is a Then step but never calls " + ASSERTIONS_CLASS;
			events.add(new SimpleConditionEvent(method, asserts, message));
		}
	};

	/** A setter returns its own class, so calls can be chained: new Product().setName(…).setPrice(…). */
	static final ArchCondition<JavaMethod> RETURN_THEIR_OWN_CLASS = new ArchCondition<>(
			"return their own class, so setters can be chained") {
		@Override
		public void check(JavaMethod method, ConditionEvents events) {
			boolean returnsOwnClass = method.getRawReturnType()
			                                .equals(method.getOwner());
			String returned = method.getRawReturnType()
			                        .getSimpleName();
			String owner = method.getOwner()
			                     .getSimpleName();
			String message = method.getFullName() + " returns " + returned + ", not " + owner;
			events.add(new SimpleConditionEvent(method, returnsOwnClass, message));
		}
	};

	private ArchitectureCheck() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Check whether a method calls Assertions through one of its own class's methods (one level deep).
	 *
	 * @param method the method to inspect
	 * @return true if a method of its own class that it calls goes to Assertions
	 */
	private static boolean callsAssertionsThroughOwnClass(JavaMethod method) {
		return method.getMethodCallsFromSelf()
		             .stream()
		             .filter(call -> call.getTargetOwner()
		                                 .equals(method.getOwner()))
		             .map(call -> call.getTarget()
		                              .resolveMember())
		             .flatMap(Optional::stream)
		             .anyMatch(ArchitectureCheck::callsAssertions);
	}

	/**
	 * Check whether a method calls Assertions itself.
	 *
	 * @param method the method to inspect
	 * @return true if one of its calls goes to Assertions
	 */
	private static boolean callsAssertions(JavaMethod method) {
		return method.getMethodCallsFromSelf()
		             .stream()
		             .anyMatch(call -> call.getTargetOwner()
		                                   .getName()
		                                   .equals(ASSERTIONS_CLASS));
	}

	/**
	 * Evaluate a rule against the project's classes and report every violation through
	 * Assertions.assertNoViolations.
	 *
	 * @param description what the rule checks, starting with "Verify"
	 * @param rule        the ArchUnit rule
	 */
	static void assertRule(String description, ArchRule rule) {
		List<String> violations = rule.evaluate(CLASSES)
		                              .getFailureReport()
		                              .getDetails();
		assertNoViolations(description, violations);
	}
}
