package com.dekra.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * These rules intentionally exclude com.paravai.foundation packages.
 * The rules will be applied only to services that import this module.
 */
@AnalyzeClasses(
    packages = "com.dekra.service",
    importOptions = {
        com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class,
        com.dekra.architecture.ExcludeFoundationImportOption.class
    }
)
public class DddLayeringAndNamingConventionsTest {

    @ArchTest
    static final ArchRule repositories_should_end_with_Repository =
        classes().that().resideInAPackage("..domain.repository..")
            .should().haveSimpleNameEndingWith("Repository")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule services_should_end_with_Service =
        classes().that().resideInAPackage("..application..")
            .should().haveSimpleNameEndingWith("Service")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dto_classes_should_reside_in_api_package =
        classes().that().haveSimpleNameEndingWith("Request")
            .or().haveSimpleNameEndingWith("Response")
            .should().resideInAPackage("..api..")
            .allowEmptyShould(true);
}