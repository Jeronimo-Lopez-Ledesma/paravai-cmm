package com.paravai.foundation.domain.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa una aplicación (tenant) que contextualiza a un sujeto. No modela la visibilidad del recurso (eso es propio del recurso),
 * sólo aporta el CONTEXTO del sujeto y helpers para componer filtros.
 * - GLOBAL: no hay tenant asociado, por ejemplo, usuarios sin app/tenant o en un contexto global.
 * - SCOPED: tenant concreto (IdValue).
 *
 * Además, expone helpers para construir el predicado de visibilidad de recursos
 * (GLOBAL_PUBLIC | TENANT_PRIVATE | TENANT_SHARED) en función del tenant
 *
 */
public final class TenantValue {

    public static final String GLOBAL_LITERAL = "GLOBAL";

    private final IdValue id;
    private final boolean global;

    private TenantValue(IdValue id) {
        this.id = id;
        this.global = (id == null);
    }

    /** Crea un tenant GLOBAL, sin IdValue */
    public static TenantValue global() {
        return new TenantValue(null);
    }
    public static TenantValue of(IdValue id) {
        return new TenantValue(Objects.requireNonNull(id, "tenant id must not be null"));
    }

    public static TenantValue ofNullable(IdValue id) {
        return new TenantValue(id);
    }

    /** Deserialización **/
    @JsonCreator
    public static TenantValue fromJson(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("tenant value is blank");
        }
        final String s = raw.trim();
        if (GLOBAL_LITERAL.equalsIgnoreCase(s)) {
            return global();
        }
        return of(IdValue.of(s));
    }

    /** Serialización **/
    @JsonValue
    public String asString() {
        return isGlobal() ? GLOBAL_LITERAL : id.toString();
    }


    public boolean isGlobal() { return global; }


    public boolean isScoped() { return !global; }


    public Optional<IdValue> id() { return Optional.ofNullable(id); }

    @Override public String toString() { return asString(); }
    @Override public boolean equals(Object o) {
        return (this == o) || (o instanceof TenantValue tv && Objects.equals(this.id, tv.id));
    }
    @Override public int hashCode() { return Objects.hash(id); }

    /**
     * Construye un ResourcePredicateValue que refleja la visibilidad de un recurso
     * para el TenantValue, asumiendo :
     * - visibility: "GLOBAL_PUBLIC" | "TENANT_PRIVATE" | "TENANT_SHARED"
     * - ownerTenantId: IdValue|null (propietario del recurso; null si global)
     * - sharedWith:   array de IdValue (tenants con los que se comparte)
     *
     * Campos en los documentos del recurso:
     *   visibilityField     = "visibility"
     *   ownerTenantIdField  = "ownerTenantId"
     *   sharedWithField     = "sharedWith"
     *
     * Para GLOBAL, sin tenant, sólo GLOBAL_PUBLIC.
     * Para tenant T:
     *   GLOBAL_PUBLIC  OR
     *   (TENANT_PRIVATE AND ownerTenantId == T) OR
     *   (TENANT_SHARED  AND T IN sharedWith)
     */
    public ResourcePredicateValue visibilityPredicate(String visibilityField,
                                                      String ownerTenantIdField,
                                                      String sharedWithField) {

        Objects.requireNonNull(visibilityField, "visibilityField");
        Objects.requireNonNull(ownerTenantIdField, "ownerTenantIdField");
        Objects.requireNonNull(sharedWithField, "sharedWithField");

        final var vGlobal = ResourcePredicateValue.eq(visibilityField, "GLOBAL_PUBLIC");
        if (isGlobal()) {
            // Sin tenant de sujeto, sólo ve recursos globales públicos
            return vGlobal;
        }

        final IdValue t = id; // no null aquí
        final var vPrivate = ResourcePredicateValue.and(
                ResourcePredicateValue.eq(visibilityField, "TENANT_PRIVATE"),
                ResourcePredicateValue.eq(ownerTenantIdField, t.toString())
        );
        final var vShared = ResourcePredicateValue.and(
                ResourcePredicateValue.eq(visibilityField, "TENANT_SHARED"),
                ResourcePredicateValue.in(sharedWithField, java.util.List.of(t.toString()))
        );

        return ResourcePredicateValue.or(vGlobal, vPrivate, vShared);
    }

    /**
     * Con nombres por defecto
     */
    public ResourcePredicateValue defaultVisibilityPredicate() {
        return visibilityPredicate("visibility", "ownerTenantId", "sharedWith");
    }

    /**
     * Permite evaluar si un recurso concreto es visible para el tenant
     * Útil para validaciones o tests, pero NO para filtrado masivo
     * Para filtrado masivo usar predicate.
     */
    public boolean canSee(String visibility,
                          IdValue ownerTenantId,
                          Collection<IdValue> sharedWith) {
        if (visibility == null) return false;
        final String v = visibility.trim().toUpperCase();

        if ("GLOBAL_PUBLIC".equals(v)) return true;
        if (isGlobal()) return false;

        if ("TENANT_PRIVATE".equals(v)) {
            return ownerTenantId != null && id.equals(ownerTenantId);
        }
        if ("TENANT_SHARED".equals(v)) {
            return sharedWith != null && sharedWith.contains(id);
        }
        return false;
    }
}
