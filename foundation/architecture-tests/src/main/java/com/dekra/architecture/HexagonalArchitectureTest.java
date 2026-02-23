package com.dekra.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
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
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_api_or_infrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..api..", "..infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure_except_mappers =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should()
                    .dependOnClassesThat(
                            JavaClass.Predicates.resideInAPackage("..infrastructure..")
                                    .and(
                                            DescribedPredicate.not(
                                                    JavaClass.Predicates.resideInAnyPackage(
                                                            "..infrastructure.mapper..",
                                                            "..infrastructure.adapter..",
                                                            "..infrastructure.converter.."
                                                    )
                                            )
                                    )
                    )
                    .because("Application layer should not depend on Infrastructure layer, except mapper/adapter packages which act as internal boundaries for serialization or integration.");
}