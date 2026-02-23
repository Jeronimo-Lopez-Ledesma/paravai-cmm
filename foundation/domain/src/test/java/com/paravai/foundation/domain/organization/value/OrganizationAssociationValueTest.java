package com.paravai.foundation.domain.organization.value;

import com.paravai.foundation.domain.value.IdValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationAssociationValueTest {

    @Test
    void forContactWithoutLocation_shouldCreateWithTargetContact() {
        IdValue orgId = IdValue.generate();

        OrganizationAssociationValue value = OrganizationAssociationValue.forContact(orgId);

        assertEquals(orgId, value.getOrganizationId());
        assertTrue(value.getOrganizationLocationId().isEmpty());
        assertEquals(OrganizationAssociationValue.AssociationTarget.CONTACT, value.getTarget());
        assertFalse(value.belongsToLocation());
    }

    @Test
    void forContactWithLocation_shouldCreateWithTargetContact() {
        IdValue orgId = IdValue.generate();
        IdValue locId = IdValue.generate();

        OrganizationAssociationValue value = OrganizationAssociationValue.forContact(orgId, locId);

        assertEquals(orgId, value.getOrganizationId());
        assertTrue(value.getOrganizationLocationId().isPresent());
        value.getOrganizationLocationId().ifPresentOrElse(
                id -> assertEquals(locId, id),
                () -> fail("Expected locationId to be present")
        );
        assertTrue(value.belongsToLocation());
    }

    @Test
    void genericFactory_shouldCreateWithGivenTarget() {
        IdValue orgId = IdValue.generate();

        OrganizationAssociationValue value =
                OrganizationAssociationValue.of(orgId, OrganizationAssociationValue.AssociationTarget.CONTACT);

        assertEquals(orgId, value.getOrganizationId());
        assertEquals(OrganizationAssociationValue.AssociationTarget.CONTACT, value.getTarget());
        assertTrue(value.getOrganizationLocationId().isEmpty());
    }

    @Test
    void withLocation_shouldReturnNewInstanceWithLocation() {
        IdValue orgId = IdValue.generate();
        IdValue locId = IdValue.generate();

        OrganizationAssociationValue original = OrganizationAssociationValue.forContact(orgId);
        OrganizationAssociationValue updated = original.withLocation(locId);

        assertNotEquals(original, updated);
        assertTrue(updated.belongsToLocation());
        assertTrue(updated.getOrganizationLocationId().isPresent());
        updated.getOrganizationLocationId().ifPresentOrElse(
                id -> assertEquals(locId, id),
                () -> fail("Expected locationId to be present")
        );
    }

    @Test
    void withoutLocation_shouldReturnNewInstanceWithoutLocation() {
        IdValue orgId = IdValue.generate();
        IdValue locId = IdValue.generate();

        OrganizationAssociationValue original = OrganizationAssociationValue.forContact(orgId, locId);
        OrganizationAssociationValue updated = original.withoutLocation();

        assertNotEquals(original, updated);
        assertTrue(original.belongsToLocation());
        assertFalse(updated.belongsToLocation());
        assertTrue(updated.getOrganizationLocationId().isEmpty());
    }

    @Test
    void withTarget_shouldReturnNewInstanceWithDifferentTarget() {
        IdValue orgId = IdValue.generate();

        OrganizationAssociationValue original = OrganizationAssociationValue.forContact(orgId);
        OrganizationAssociationValue updated =
                original.withTarget(OrganizationAssociationValue.AssociationTarget.CONTACT);

        assertEquals(original.getOrganizationId(), updated.getOrganizationId());
        assertEquals(original.getOrganizationLocationId(), updated.getOrganizationLocationId());
        assertEquals(OrganizationAssociationValue.AssociationTarget.CONTACT, updated.getTarget());
    }

    @Test
    void equalsAndHashCode_shouldDependOnFields() {
        IdValue orgId = IdValue.generate();
        IdValue locId = IdValue.generate();

        OrganizationAssociationValue v1 = OrganizationAssociationValue.forContact(orgId, locId);
        OrganizationAssociationValue v2 = OrganizationAssociationValue.forContact(orgId, locId);

        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void constructorShouldThrowWhenOrganizationIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                OrganizationAssociationValue.of(null, OrganizationAssociationValue.AssociationTarget.CONTACT));
    }

    @Test
    void constructorShouldThrowWhenTargetIsNull() {
        IdValue orgId = IdValue.generate();
        assertThrows(IllegalArgumentException.class, () ->
                OrganizationAssociationValue.of(orgId, null));
    }

    // Tests para certificate

    @Test
    void forCertificateWithoutLocation_shouldCreateWithTargetCertificate() {
        IdValue orgId = IdValue.generate();

        OrganizationAssociationValue value = OrganizationAssociationValue.forCertificate(orgId);

        assertEquals(orgId, value.getOrganizationId());
        assertTrue(value.getOrganizationLocationId().isEmpty());
        assertEquals(OrganizationAssociationValue.AssociationTarget.CERTIFICATE, value.getTarget());
        assertFalse(value.belongsToLocation());
    }

    @Test
    void forCertificateWithLocation_shouldCreateWithTargetCertificate() {
        IdValue orgId = IdValue.generate();
        IdValue locId = IdValue.generate();

        OrganizationAssociationValue value = OrganizationAssociationValue.forCertificate(orgId, locId);

        assertEquals(orgId, value.getOrganizationId());
        assertTrue(value.getOrganizationLocationId().isPresent());
        value.getOrganizationLocationId().ifPresentOrElse(
                id -> assertEquals(locId, id),
                () -> fail("Expected locationId to be present")
        );
        assertTrue(value.belongsToLocation());
        assertEquals(OrganizationAssociationValue.AssociationTarget.CERTIFICATE, value.getTarget());
    }

    @Test
    void genericFactory_shouldCreateWithCertificateTarget() {
        IdValue orgId = IdValue.generate();

        OrganizationAssociationValue value =
                OrganizationAssociationValue.of(orgId, OrganizationAssociationValue.AssociationTarget.CERTIFICATE);

        assertEquals(orgId, value.getOrganizationId());
        assertEquals(OrganizationAssociationValue.AssociationTarget.CERTIFICATE, value.getTarget());
        assertTrue(value.getOrganizationLocationId().isEmpty());
        assertFalse(value.belongsToLocation());
    }

    @Test
    void withTarget_shouldAllowChangingToCertificate() {
        IdValue orgId = IdValue.generate();
        IdValue locId = IdValue.generate();

        OrganizationAssociationValue original = OrganizationAssociationValue.forContact(orgId, locId);
        OrganizationAssociationValue updated =
                original.withTarget(OrganizationAssociationValue.AssociationTarget.CERTIFICATE);

        assertEquals(orgId, updated.getOrganizationId());
        assertTrue(updated.getOrganizationLocationId().isPresent());
        assertEquals(OrganizationAssociationValue.AssociationTarget.CERTIFICATE, updated.getTarget());
    }
}

