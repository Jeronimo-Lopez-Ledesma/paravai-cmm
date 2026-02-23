package com.dekra.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

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
public class LombokUsageRulesTest {

    @ArchTest
    static final ArchRule domain_entities_should_not_use_Data =
        noClasses().that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("lombok.Data")
            .allowEmptyShould(true);
}