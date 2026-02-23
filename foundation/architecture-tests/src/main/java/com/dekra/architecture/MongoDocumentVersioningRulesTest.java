package com.dekra.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
        packages = "com.paravai",
        importOptions = {
                com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class,
                com.dekra.architecture.ExcludeFoundationImportOption.class
        }
)
public class MongoDocumentVersioningRulesTest {

    @ArchTest
    static final ArchRule mongo_documents_should_have_document_version_field =
            classes().that()
                    .areAnnotatedWith(Document.class)
                    .should(haveFieldNamed("documentVersion"))
                    .allowEmptyShould(true);

    private static ArchCondition<JavaClass> haveFieldNamed(String fieldName) {
        return new ArchCondition<>("have a field named '" + fieldName + "'") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean found = javaClass.getAllFields().stream()
                        .map(JavaField::getName)
                        .anyMatch(fieldName::equals);

                String message = javaClass.getName()
                        + (found ? " declares field " : " does NOT declare field ")
                        + "'" + fieldName + "'";

                events.add(new SimpleConditionEvent(javaClass, found, message));
            }
        };
    }
}
