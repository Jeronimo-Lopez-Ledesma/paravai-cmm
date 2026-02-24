package com.paravai.eureka.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;


/**
 * These rules intentionally exclude com.paravai.foundation packages.
 * The rules will be applied only to services that import this module.
 */
public class ExcludeFoundationImportOption implements ImportOption {

    @Override
    public boolean includes(Location location) {
        String uri = location.asURI().toString().toLowerCase();
        // Skip any compiled class that belongs to service-foundation
        return !(uri.contains("service-foundation") ||
                uri.contains("com/paravai/eureka/service/foundation"));
    }
}