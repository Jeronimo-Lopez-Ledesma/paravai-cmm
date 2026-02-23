package com.dekra.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.slf4j.Logger;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * These rules intentionally exclude com.paravai.foundation packages.
 * The rules will be applied only to services that import this module.
 */
@AnalyzeClasses(
    packages = "com.paravai",
    importOptions = {
        com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class,
        com.dekra.architecture.ExcludeFoundationImportOption.class
    }
)
public class LoggingRulesTest {

    @ArchTest
    static final ArchRule domain_should_not_have_loggers =
        noFields().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .should().haveRawType(Logger.class)
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule loggers_should_be_private_static_final =
        fields().that().haveRawType(Logger.class)
            .should().bePrivate()
            .andShould().beStatic()
            .andShould().beFinal()
            .allowEmptyShould(true);
}