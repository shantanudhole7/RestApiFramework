package Conventions;

import com.tngtech.archunit.lang.ArchRule;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import org.junit.Test;

import static Conventions.ArchitectureCheck.CALL_ASSERTIONS;
import static Conventions.ArchitectureCheck.RETURN_THEIR_OWN_CLASS;
import static Conventions.ArchitectureCheck.assertRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Architecture convention tests: rules about code structure that every class must follow. Calls no API. */
public class ArchitectureConventionsTest {

	/** Every public POJO setter returns its own class, so request bodies are built in one chained expression. */
	@Test
	public void pojoSettersReturnTheirOwnClass() {
		ArchRule rule = methods().that()
		                         .areDeclaredInClassesThat()
		                         .resideInAPackage("Pojo")
		                         .and()
		                         .arePublic()
		                         .and()
		                         .haveNameStartingWith("set")
		                         .should(RETURN_THEIR_OWN_CLASS);
		assertRule("Verify every POJO setter returns its own class", rule);
	}

	/** Every Then step verifies something through Assertions, so a passing scenario has really checked an outcome. */
	@Test
	public void everyThenStepAsserts() {
		ArchRule rule = methods().that()
		                         .areDeclaredInClassesThat()
		                         .resideInAPackage("StepDefinition")
		                         .and()
		                         .areAnnotatedWith(Then.class)
		                         .should(CALL_ASSERTIONS);
		assertRule("Verify every Then step calls Assertions", rule);
	}

	/** Step and hook classes build requests through the Api classes, never with given() themselves. */
	@Test
	public void stepAndHookClassesNeverCallGiven() {
		ArchRule rule = noClasses().that()
		                           .resideInAPackage("StepDefinition..")
		                           .should()
		                           .callMethod(RestAssured.class, "given");
		assertRule("Verify step and hook classes never call given()", rule);
	}

	/** Api classes build and send requests; callers decide what to check, so Api classes never assert. */
	@Test
	public void apiClassesNeverAssert() {
		ArchRule rule = noClasses().that()
		                           .resideInAPackage("Api")
		                           .should()
		                           .dependOnClassesThat()
		                           .resideInAPackage("Assertion");
		assertRule("Verify Api classes never assert", rule);
	}
}
