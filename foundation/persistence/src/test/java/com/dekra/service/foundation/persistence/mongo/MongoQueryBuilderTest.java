package com.dekra.service.foundation.persistence.mongo;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MongoQueryBuilderTest {

    @DisplayName("Given empty parameters, the list of criterias should be empty")
    @Test
    void addFiltersToQuery_empty() {
        //Given
        Query query = Mockito.mock(Query.class);
        Map<String, String> params = new HashMap<>();

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Mockito.verifyNoInteractions(query);
    }

    @DisplayName("Given invalid parameters, the list of criterias should be empty")
    @Test
    void addFiltersToQuery_noneFilter() {
        //Given
        Query query = Mockito.mock(Query.class);
        Map<String, String> params = Map.of(
                HttpHeaders.AUTHORIZATION, "bearer xxxx",
                HttpHeaders.ACCEPT, "accept value",
                "filter_no_jsonapi[campo]", "valor",
                "filter[_no_jsonapi[campo]", "valor",
                "filer[campo]", "valor");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Mockito.verifyNoInteractions(query);
    }

    @DisplayName("Given a filter parameter without comparator, there should be an Equals Criteria")
    @Test
    void addFiltersToQuery_noComparatorDefined() {
        //Given
        Query query = new Query();
        Map<String, String> params = Map.of("filter[campo]", "valor");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Document criteriaFilter = query.getQueryObject();
        assertNotNull(criteriaFilter);
        assertEquals("valor", criteriaFilter.get("campo"));
    }

    @DisplayName("Given a filter parameter with Equals comparator, there should be an Equals Criteria")
    @Test
    void addFiltersToQuery_eq() {
        //Given
        Query query = new Query();
        Map<String, String> params = Map.of("filter[campo][eq]", "valor");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Document criteriaFilter = query.getQueryObject();
        assertNotNull(criteriaFilter);
        assertEquals("valor", criteriaFilter.get("campo"));
    }

    @DisplayName("Given a filter parameter with Non Equals comparator, there should be a Non Equals Criteria")
    @Test
    void addFiltersToQuery_ne() {
        //Given
        Query query = new Query();
        Map<String, String> params = Map.of("filter[campo][ne]", "valor");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Document criteriaFilter = query.getQueryObject();
        assertNotNull(criteriaFilter);
        var fieldDocument = (Document) criteriaFilter.get("campo");
        assertEquals("valor", fieldDocument.get("$ne"));
    }

    @DisplayName("Given a filter parameter with Greater Than comparator, there should be a Greater Than Criteria")
    @Test
    void addFiltersToQuery_gt() {
        //Given
        Query query = new Query();
        Map<String, String> params = Map.of("filter[campo][gt]", "valor");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Document criteriaFilter = query.getQueryObject();
        assertNotNull(criteriaFilter);
        var fieldDocument = (Document) criteriaFilter.get("campo");
        assertEquals("valor", fieldDocument.get("$gt"));
    }

    @DisplayName("Given a filter parameter with Lower Than comparator, there should be a Lower Than Criteria")
    @Test
    void addFiltersToQuery_lt() {
        //Given
        Query query = new Query();
        Map<String, String> params = Map.of("filter[campo][lt]", "valor");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Document criteriaFilter = query.getQueryObject();
        assertNotNull(criteriaFilter);
        var fieldDocument = (Document) criteriaFilter.get("campo");
        assertEquals("valor", fieldDocument.get("$lt"));
    }

    @DisplayName("Given a filter parameter with Like comparator, there should be a like Criteria")
    @Test
    void addFiltersToQuery_like() {
        //Given
        Query query = new Query();
        Map<String, String> params = Map.of("filter[campo][like]", "valor");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Document criteriaFilter = query.getQueryObject();
        assertNotNull(criteriaFilter);
        var fieldDocument = (Pattern) criteriaFilter.get("campo");
        assertTrue(fieldDocument.pattern().contains("valor"));
    }

    @DisplayName("Given a filter parameter with In comparator, there should be a In Criteria")
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void addFiltersToQuery_in() {
        //Given
        Query query = new Query();
        Map<String, String> params = Map.of("filter[campo][in]", "valor1,valor2");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Document criteriaFilter = query.getQueryObject();
        assertNotNull(criteriaFilter);
        var fieldDocument = (Document) criteriaFilter.get("campo");
        var inDocument = (List) fieldDocument.get(("$in"));
        assertTrue(inDocument.containsAll(List.of("valor1", "valor2")));
    }

    @DisplayName("Given a list of valid filter parameter, there should be a list of Criterias")
    @Test
    void addFiltersToQuery_allAllowedTypes() {

        Query query = Mockito.mock(Query.class);
        Map<String, String> params = Map.of(
                "filter[campo]", "valor",
                "filter[campo][eq]", "valor",
                "filter[campo][ne]", "valor",
                "filter[campo][gt]", "valor",
                "filter[campo][lt]", "valor",
                "filter[campo][like]", "valor",
                "filter[campo][in]", "valor1,valor2");

        //When
        MongoQueryBuilder.addFiltersToQuery(params, query);

        //Then
        Mockito.verify(query, Mockito.times(7)).addCriteria(Mockito.any());
    }


}