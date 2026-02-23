package com.paravai.foundation.viewjsonapi.composition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.*;
import java.util.function.Function;

public abstract class AbstractJsonApiCompositionMapper<R>
        implements JsonApiCompositionMapper<R> {

    // --------------------------------------------------------------------
    // SAFETY HELPERS
    // --------------------------------------------------------------------

    protected JsonNode safe(JsonNode node) {
        return (node == null || node.isMissingNode() || node.isNull())
                ? JsonNodeFactory.instance.objectNode()
                : node;
    }

    protected String text(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode()) return null;
        JsonNode v = node.get(fieldName);
        return (v == null || v.isNull() || !v.isValueNode()) ? null : v.asText();
    }

    protected Integer intValue(JsonNode node, String fieldName) {
        JsonNode v = safe(node.get(fieldName));
        return v.isInt() ? v.asInt() : null;
    }

    protected Boolean bool(JsonNode node, String fieldName) {
        JsonNode v = safe(node.get(fieldName));
        return v.isBoolean() ? v.asBoolean() : null;
    }

    protected Double dbl(JsonNode node, String fieldName) {
        JsonNode v = safe(node.get(fieldName));
        return v.isDouble() ? v.asDouble() : null;
    }

    // --------------------------------------------------------------------
    // RELATIONSHIPS
    // --------------------------------------------------------------------

    protected String extractRelationshipId(JsonNode relationships, String relName) {
        JsonNode rel = safe(relationships.get(relName));
        JsonNode data = safe(rel.get("data"));
        return text(data, "id");
    }

    protected List<String> extractRelationshipIds(JsonNode relationships, String relName) {
        JsonNode rel = safe(relationships.get(relName));
        JsonNode dataArray = safe(rel.get("data"));

        if (!dataArray.isArray()) return List.of();

        List<String> list = new ArrayList<>();
        for (JsonNode element : dataArray) {
            String id = text(element, "id");
            if (id != null) list.add(id);
        }
        return list;
    }

    // --------------------------------------------------------------------
    // INCLUDED
    // --------------------------------------------------------------------

    protected JsonNode findIncluded(JsonNode included, String type, String id) {
        if (!safe(included).isArray()) return null;

        for (JsonNode node : included) {
            if (type.equals(text(node, "type")) &&
                    id.equals(text(node, "id"))) {
                return node;
            }
        }
        return null;
    }

    protected List<JsonNode> findIncludedMany(JsonNode included, String type, List<String> ids) {
        if (!safe(included).isArray()) return List.of();

        Set<String> idSet = new HashSet<>(ids);
        List<JsonNode> list = new ArrayList<>();

        for (JsonNode node : included) {
            String nodeType = text(node, "type");
            String nodeId   = text(node, "id");
            if (type.equals(nodeType) && idSet.contains(nodeId)) {
                list.add(node);
            }
        }
        return list;
    }

    // --------------------------------------------------------------------
    // GENERIC ARRAY MAPPER
    // --------------------------------------------------------------------

    protected <T> List<T> mapList(JsonNode array, Function<JsonNode, T> mapper) {
        if (array == null || !array.isArray()) return List.of();

        List<T> list = new ArrayList<>();
        for (JsonNode item : array) {
            T v = mapper.apply(item);
            if (v != null) list.add(v);
        }
        return list;
    }

    // --------------------------------------------------------------------
    // EXTRACT ATTRIBUTES
    // --------------------------------------------------------------------

    protected JsonNode attributes(JsonNode resource) {
        return safe(resource.get("attributes"));
    }

    // --------------------------------------------------------------------
    // MAIN ENTRYPOINT (implemented by subclasses)
    // --------------------------------------------------------------------

    @Override
    public abstract R map(JsonNode document);
}
