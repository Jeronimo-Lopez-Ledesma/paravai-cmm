package com.dekra.service.foundation.persistence.mongo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MongoQueryBuilder {

    private static final Pattern FILTER_PATTERN =
            Pattern.compile("^filter\\[([A-Za-z0-9]+)\\](?:\\[(gt|in|eq|ne|lt|like)\\])?$");

    /**
     * Constructor para ocultar el constructor por defecto, ya que es una clase estática.
     */
    private MongoQueryBuilder() {
        super();
    }

    public static Query buildQuery(Map<String, String> filters,
                                   Optional<String> search,
                                   Optional<String> sort,
                                   int page,
                                   int size,
                                   String searchField) {

        Query query = new Query();

        addFiltersToQuery(filters, query);

        // Búsqueda por texto (en campo 'name.value' u otro configurable)
        search.ifPresent(s -> query.addCriteria(Criteria.where(searchField).regex(s, "i")));

        // Ordenación tipo sort=name,asc
        sort.ifPresent(s -> {
            String[] parts = s.split(",");
            if (parts.length == 2) {
                String field = parts[0];
                String direction = parts[1];
                Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
                query.with(Sort.by(dir, field));
            }
        });

        Pageable pageable = PageRequest.of(page - 1, size);
        query.with(pageable);

        return query;
    }

    public static Query buildQuery(Map<String, String> filters, Optional<String> search) {
        Query query = new Query();

        addFiltersToQuery(filters, query);

        // Añade búsqueda por nombre
        search.ifPresent(s -> query.addCriteria(Criteria.where("name").regex(s, "i")));

        return query;
    }

    /**
     * Add the criterias for Mongo depending on the filters received by params.
     * @param filters Request query params following the jsonapi format convention.
     * @param query Query where the Criterias will be loaded.
     */
    static void addFiltersToQuery(@NotNull Map<String, String> filters, @NotNull Query query) {

        filters.keySet().stream()
                .filter(key -> key.startsWith("filter["))   //Este filtro es más rápido que el siguiente matcher.
                .map(key -> {
                    Matcher matcher = FILTER_PATTERN.matcher(key);
                    if (matcher.matches()) {

                        String field = matcher.group(1);
                        String operator = matcher.group(2);    //Opcional. Por eso el Optional.orElse siguiente.
                        QueryFilterComparatorEnum comparator = Optional.ofNullable(QueryFilterComparatorEnum.from(operator))
                                .orElse(QueryFilterComparatorEnum.eq);
                        String value = filters.get(key);

                        return buildCriteria(field, value, comparator);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(query::addCriteria);
    }

    protected static Criteria buildCriteria(@NotBlank String field, @NotBlank String value,
                                            @NotNull QueryFilterComparatorEnum comparator) {
        return buildCriteria(field, value, MongoQueryParamTypeEnum.STRING, comparator);
    }

    /**
     * Crea un Criteria que podrá ser añadido a un Query de Spring Data.
     * @param field Nombre del campo.
     * @param value Valor que debe tener el campo.
     * @param valueType Clase del campo.
     * @param comparator Comparador para el filtro.
     * @return Criteria para el campo según el comparador y el tipo de campo.
     */
    public static Criteria buildCriteria(
            @NotBlank String field,
            @NotBlank String value,
            @NotNull MongoQueryParamTypeEnum valueType,
            @NotNull QueryFilterComparatorEnum comparator) {

        if (StringUtils.isAnyEmpty(field, value) || comparator == null) {
            log.warn("Filter for key {} with empty value of empty comparator. Value: {}, comparator: {}",
                    field, value, comparator);
            return null;
        }

        // Normalize field "id" to "_id"
        if ("id".equals(field)) {
            field = "_id";
        }

        switch (comparator) {
            case eq -> {
                return Criteria.where(field).is(valueType.getConvertedValue(value));
            }
            case gt -> {
                return Criteria.where(field).gt(valueType.getConvertedValue(value));
            }
            case lt -> {
                return Criteria.where(field).lt(valueType.getConvertedValue(value));
            }
            case ne -> {
                return Criteria.where(field).ne(valueType.getConvertedValue(value));
            }
            case like -> {
                //Value cannot be empty because of first validation of the method.
                // This comparator only accepts string values.
                String likeValue = ".*".concat(value).concat(".*");
                return Criteria.where(field).regex(likeValue, "i"); //i = case-insensitive
            }
            case in -> {
                List<Object> values = Arrays.stream(StringUtils.defaultString(value).split(","))
                        .map(valueType::getConvertedValue)
                        .toList();
                return Criteria.where(field).in(values);
            }
            case null, default ->
                    throw new IllegalArgumentException("Filter type not found");
        }
    }

}
