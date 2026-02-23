package com.dekra.service.foundation.domain.organization.value;

import com.dekra.service.foundation.domaincore.value.IdValue;

import java.util.Objects;
import java.util.Optional;

/**
 * OrganizationAssociationValue
 *
 * Representa una asociación débil con una Organization y, opcionalmente, con una OrganizationLocation,
 * desde cualquier otro agregado (Contact, Service, BusinessLine, etc.).
 *
 * - organizationId: obligatorio
 * - organizationLocationId: opcional (localización concreta)
 * - target: indica el tipo de entidad que se asocia con la Organization (por defecto CONTACT)
 *
 * Uso recomendado:
 *   - Contact: OrganizationAssociationValue.forContact(orgId) o forContact(orgId, locId)
 *   - Otros agregados: OrganizationAssociationValue.of(orgId, locId, AssociationTarget.XYZ)
 */
public final class OrganizationAssociationValue  {

    public enum AssociationTarget {
        CONTACT,
        CERTIFICATE,
        OPPORTUNITY,
        PROJECT,
        SERVICE,
        STANDARD
        // Futuras opciones: BUSINESS_LINE, PROJECT, ROLE, etc.
    }

    private final IdValue organizationId;
    private final IdValue organizationLocationId; // opcional
    private final AssociationTarget target;

    private OrganizationAssociationValue(IdValue organizationId,
                                         IdValue organizationLocationId,
                                         AssociationTarget target) {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId is required");
        }
        if (target == null) {
            throw new IllegalArgumentException("association target is required");
        }
        this.organizationId = organizationId;
        this.organizationLocationId = organizationLocationId;
        this.target = target;
    }

    /* ---------- Factories ---------- */
    /** Asociación por defecto para Contact (sin location) */
    public static OrganizationAssociationValue forContact(IdValue organizationId) {
        return new OrganizationAssociationValue(organizationId, null, AssociationTarget.CONTACT);
    }
    /** Asociación por defecto para Contact (con location) */
    public static OrganizationAssociationValue forContact(IdValue organizationId, IdValue organizationLocationId) {
        return new OrganizationAssociationValue(organizationId, organizationLocationId, AssociationTarget.CONTACT);
    }
    /** Asociación por defecto para Opportunity (sin location) */
    public static OrganizationAssociationValue forOpportunity(IdValue organizationId) {
        return new OrganizationAssociationValue(organizationId, null, AssociationTarget.OPPORTUNITY);
    }
    /** Asociación por defecto para Opportunity (con location) */
    public static OrganizationAssociationValue forOpportunity(IdValue organizationId, IdValue organizationLocationId) {
        return new OrganizationAssociationValue(organizationId, organizationLocationId, AssociationTarget.OPPORTUNITY);
    }
    /** Asociación por defecto para Project (sin location) */
    public static OrganizationAssociationValue forProject(IdValue organizationId) {
        return new OrganizationAssociationValue(organizationId, null, AssociationTarget.PROJECT);
    }
    /** Asociación por defecto para Project (con location) */
    public static OrganizationAssociationValue forProject(IdValue organizationId, IdValue organizationLocationId) {
        return new OrganizationAssociationValue(organizationId, organizationLocationId, AssociationTarget.PROJECT);
    }
    /** Asociación por defecto para Certificate (sin location) */
    public static OrganizationAssociationValue forCertificate(IdValue organizationId) {
        return new OrganizationAssociationValue(organizationId, null, AssociationTarget.CERTIFICATE);
    }
    /** Asociación por defecto para Certificate (con location) */
    public static OrganizationAssociationValue forCertificate(IdValue organizationId, IdValue organizationLocationId) {
        return new OrganizationAssociationValue(organizationId, organizationLocationId, AssociationTarget.CERTIFICATE);
    }

    /** Asociación por defecto para Standard (sin location) */
    public static OrganizationAssociationValue forStandardIssuer(IdValue organizationId) {
        return new OrganizationAssociationValue(organizationId, null, AssociationTarget.STANDARD);
    }

    /** Factory genérico para cualquier tipo de asociación (sin location) */
    public static OrganizationAssociationValue of(IdValue organizationId, AssociationTarget target) {
        return new OrganizationAssociationValue(organizationId, null, target);
    }

    /** Factory genérico para cualquier tipo de asociación (con location) */
    public static OrganizationAssociationValue of(IdValue organizationId,
                                                  IdValue organizationLocationId,
                                                  AssociationTarget target) {
        return new OrganizationAssociationValue(organizationId, organizationLocationId, target);
    }


    public IdValue getOrganizationId() {
        return organizationId;
    }

    public Optional<IdValue> getOrganizationLocationId() {
        return Optional.ofNullable(organizationLocationId);
    }

    public AssociationTarget getTarget() {
        return target;
    }


    public boolean belongsToLocation() {
        return organizationLocationId != null;
    }

    /** Copia con location (no muta el actual) */
    public OrganizationAssociationValue withLocation(IdValue locationId) {
        return new OrganizationAssociationValue(this.organizationId, locationId, this.target);
    }

    /** Copia sin location */
    public OrganizationAssociationValue withoutLocation() {
        return new OrganizationAssociationValue(this.organizationId, null, this.target);
    }

    /** Copia con distinto target */
    public OrganizationAssociationValue withTarget(AssociationTarget newTarget) {
        return new OrganizationAssociationValue(this.organizationId, this.organizationLocationId, newTarget);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationAssociationValue that)) return false;
        return organizationId.equals(that.organizationId)
                && Objects.equals(organizationLocationId, that.organizationLocationId)
                && target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, organizationLocationId, target);
    }

    @Override
    public String toString() {
        return "OrganizationAssociationValue{" +
                "organizationId=" + organizationId +
                ", organizationLocationId=" + (organizationLocationId != null ? organizationLocationId : "none") +
                ", target=" + target +
                '}';
    }
}
