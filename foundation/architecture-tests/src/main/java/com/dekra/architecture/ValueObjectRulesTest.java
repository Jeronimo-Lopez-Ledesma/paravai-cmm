package com.dekra.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * These rules intentionally exclude com.paravai.foundation packages.
 * Service Foundation is a shared technical library, not a bounded context.
 * The rules will be applied only to services that import this module.
 */
@AnalyzeClasses(
    packages = "com.dekra.service",
    importOptions = {
        com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class,
        com.dekra.architecture.ExcludeFoundationImportOption.class
    }
)
public class ValueObjectRulesTest {

    @ArchTest
    static final ArchRule value_objects_should_have_final_fields =
        fields().that().areDeclaredInClassesThat().resideInAPackage("..domain.value..")
            .should().beFinal()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule value_objects_should_not_have_setters =
        noMethods().that().areDeclaredInClassesThat().resideInAPackage("..domain.value..")
            .should().haveNameStartingWith("set")
            .allowEmptyShould(true);
}