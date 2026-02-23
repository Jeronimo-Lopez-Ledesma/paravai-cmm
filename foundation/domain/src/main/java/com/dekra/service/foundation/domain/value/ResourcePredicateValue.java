package com.dekra.service.foundation.domaincore.value;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DSL mínimo para describir filtros sobre recursos (eq, in, and, or, not)
 * Se serializa a Map<String,Object> para que pueda manejarse tanto por adapters Mongo como SQL.
 */
public abstract class ResourcePredicateValue {

    @JsonValue
    public abstract Map<String, Object> toMap();

    // =========================================================
    // Factories (con normalización)
    // =========================================================

    public static ResourcePredicateValue eq(String field, Object value){
        Objects.requireNonNull(field, "field");
        return new Eq(field, value);
    }

    public static ResourcePredicateValue in(String field, Collection<?> values){
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(values, "values");
        return new In(field, List.copyOf(values));
    }

    /**
     * AND con normalización:
     * - Aplana hijos AND (and(a, and(b,c)) => and(a,b,c))
     * - Elimina nulls
     * - Unario => devuelve el hijo (and(x) => x)
     * - Vacío => IllegalArgumentException (para evitar un "TRUE" implícito)
     */
    public static ResourcePredicateValue and(ResourcePredicateValue... predicates){
        return normalizedBool("and", Arrays.asList(predicates));
    }
    /**
     * OR con normalización:
     * - Aplana hijos OR (or(a, or(b,c)) => or(a,b,c))
     * - Elimina nulls
     * - Unario => devuelve el hijo (or(x) => x)
     * - Vacío => IllegalArgumentException
     */
    public static ResourcePredicateValue or(ResourcePredicateValue... predicates){
        return normalizedBool("or", Arrays.asList(predicates));
    }
    /**
     * NOT con optimización:
     * - not(not(p)) => p
     */
    public static ResourcePredicateValue not(ResourcePredicateValue predicate){
        Objects.requireNonNull(predicate, "predicate");
        if (predicate instanceof Not n) {
            return n.inner; // doble negación
        }
        return new Not(predicate);
    }


    // Normalización
    private static ResourcePredicateValue normalizedBool(String op, List<ResourcePredicateValue> raw) {
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(raw, "predicates");

        // 1) elimina nulls
        List<ResourcePredicateValue> cleaned = raw.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        // 2) aplana hijos del mismo op
        List<ResourcePredicateValue> flat = new ArrayList<>(cleaned.size());
        for (ResourcePredicateValue p : cleaned) {
            if (p instanceof Bool b && b.op.equals(op)) {
                flat.addAll(b.list); // absorbe hijos
            } else {
                flat.add(p);
            }
        }

        // 3) elimina nulls residuales por si acaso
        flat.removeIf(Objects::isNull);

        // 4) cardinalidad
        if (flat.isEmpty()) {
            throw new IllegalArgumentException(op + "() requires at least one child");
        }
        if (flat.size() == 1) {
            return flat.get(0); // unario => identidad
        }

        // 5) crea Bool normalizado
        return new Bool(op, flat);
    }

    // =========================================================
    // Implementaciones
    // =========================================================

    private static final class Eq extends ResourcePredicateValue {
        private final String field; private final Object value;
        private Eq(String field, Object value){ this.field=field; this.value=value; }
        @Override public Map<String, Object> toMap(){
            return Map.of("eq", Map.of("field", field, "value", value));
        }
        @Override public boolean equals(Object o){ return (this==o) || (o instanceof Eq e && Objects.equals(field,e.field) && Objects.equals(value,e.value)); }
        @Override public int hashCode(){ return Objects.hash(field, value); }
        @Override public String toString(){ return "Eq(" + field + "=" + value + ")"; }
    }

    private static final class In extends ResourcePredicateValue {
        private final String field; private final List<?> values;
        private In(String field, List<?> values){ this.field=field; this.values=values; }
        @Override public Map<String, Object> toMap(){
            return Map.of("in", Map.of("field", field, "values", values));
        }
        @Override public boolean equals(Object o){ return (this==o) || (o instanceof In e && Objects.equals(field,e.field) && Objects.equals(values,e.values)); }
        @Override public int hashCode(){ return Objects.hash(field, values); }
        @Override public String toString(){ return "In(" + field + " IN " + values + ")"; }
    }

    private static final class Bool extends ResourcePredicateValue {
        private final String op; private final List<ResourcePredicateValue> list;
        private Bool(String op, List<ResourcePredicateValue> list){
            this.op = op;
            this.list = List.copyOf(list); // inmutable
        }
        @Override public Map<String, Object> toMap(){
            List<Map<String,Object>> inner = new ArrayList<>(list.size());
            for (ResourcePredicateValue p : list) inner.add(p.toMap());
            return Map.of(op, inner);
        }
        @Override public boolean equals(Object o){ return (this==o) || (o instanceof Bool b && Objects.equals(op,b.op) && Objects.equals(list,b.list)); }
        @Override public int hashCode(){ return Objects.hash(op, list); }
        @Override public String toString(){ return "Bool(" + op + ":" + list + ")"; }
    }

    private static final class Not extends ResourcePredicateValue {
        private final ResourcePredicateValue inner;
        private Not(ResourcePredicateValue inner){ this.inner = Objects.requireNonNull(inner); }
        @Override public Map<String, Object> toMap(){ return Map.of("not", inner.toMap()); }
        @Override public boolean equals(Object o){ return (this==o) || (o instanceof Not n && Objects.equals(inner,n.inner)); }
        @Override public int hashCode(){ return Objects.hash(inner); }
        @Override public String toString(){ return "Not(" + inner + ")"; }
    }

    public static ResourcePredicateValue inRef(String field, SetRefValue ref) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(ref, "ref");
        return new InRef(field, ref);
    }

    private static final class InRef extends ResourcePredicateValue {
        private final String field;
        private final SetRefValue ref;

        private InRef(String field, SetRefValue ref) {
            this.field = field;
            this.ref = ref;
        }

        @Override
        public Map<String, Object> toMap() {
            return Map.of("inRef", Map.of("field", field, "ref", ref.toMap()));
        }

        @Override public boolean equals(Object o) { return (this == o) || (o instanceof InRef v && Objects.equals(field, v.field) && Objects.equals(ref, v.ref)); }
        @Override public int hashCode() { return Objects.hash(field, ref); }
        @Override public String toString() { return "InRef(" + field + " IN " + ref + ")"; }
    }


}