package Conventions;

import com.tngtech.archunit.lang.ArchRule;
import io.cucumber.java.en.Then;
import org.junit.Test;

import static Conventions.ArchitectureCheck.CALL_ASSERTIONS;
import static Conventions.ArchitectureCheck.RETURN_THEIR_OWN_CLASS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

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
		ArchitectureCheck.assertRule("Verify every POJO setter returns its own class", rule);
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
		ArchitectureCheck.assertRule("Verify every Then step calls Assertions", rule);
	}
}
