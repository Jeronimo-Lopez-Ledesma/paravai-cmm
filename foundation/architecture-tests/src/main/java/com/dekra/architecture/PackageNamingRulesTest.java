package com.dekra.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class PackageNamingRulesTest {
    @ArchTest
    static final ArchRule aggregates_should_not_depend_on_other_aggregates =
            ArchRuleDefinition.noClasses()
                    .that().resideInAPackage("..domain.model..")
                    .should().dependOnClassesThat(
                            DescribedPredicate
                                    .and(
                                            JavaClass.Predicates.resideInAnyPackage("..domain.model.."),
                                            DescribedPredicate.not(JavaClass.Predicates.resideInAnyPackage(
                                                    // Allow shared Value Objects from the Foundation
                                                    "com.paravai.foundation..value.."
                                            ))
                                    )
                                    //
                                    .and(new DescribedPredicate<JavaClass>("same aggregate") {
                                        @Override
                                        public boolean test(JavaClass input) {
                                            return false;
                                        }
                                    })
                    )
                    .because("Aggregates should remain independent, except when referencing shared Value Objects from Service Foundation or internal dependencies within the same aggregate (e.g. Factory → AggregateRoot).");



    @ArchTest
    static final ArchRule package_structure_should_follow_convention =
        noClasses().should().resideOutsideOfPackage("com.dekra.service..*..")
            .allowEmptyShould(true);
}