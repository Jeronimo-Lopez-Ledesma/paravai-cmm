package com.dekra.architecture;


import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Architecture tests for the snapshot-support module.
 * Ensures isolation from service-specific domains and enforces clear dependency boundaries.
 */
@AnalyzeClasses(packages = "com.paravai.foundation.snapshot")
public class ArchitectureComplianceTest {

    private static final JavaClasses classes = new ClassFileImporter()
            .importPackages("com.paravai.foundation.snapshot");

    /**
     * The snapshot-support module must remain isolated from all service domains.
     */
    @ArchTest
    static final ArchRule should_not_depend_on_service_domains =
            noClasses()
                    .that().resideInAPackage("com.paravai.foundation.snapshot..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.dekra.service..domain..")
                    .because("snapshot-support must remain infrastructure-agnostic and independent of service domain models.");

    /**
     * Only allowed dependencies are from Jackson, Spring, or Java standard library.
     */
    @ArchTest
    static final ArchRule should_only_depend_on_allowed_libraries =
            classes()
                    .that().resideInAPackage("com.paravai.foundation.snapshot..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "com.paravai.foundation.snapshot..",
                            "java..",
                            "javax..",
                            "jakarta..",
                            "org.springframework..",
                            "com.fasterxml.jackson.."
                    )
                    .because("snapshot-support should depend only on Spring and Jackson abstractions, not on specific bounded contexts or frameworks.");
}
