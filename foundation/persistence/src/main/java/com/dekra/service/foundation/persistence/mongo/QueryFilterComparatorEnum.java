package com.dekra.service.foundation.persistence.mongo;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum comparator following the jsonapi query filters specifications.
 * @see <a href="https://jsonapi.org/format/#query-parameters">https://jsonapi.org/format/#query-parameters</a>
 */
public enum QueryFilterComparatorEnum {
    gt, in, eq, ne, lt, like;

    public static QueryFilterComparatorEnum from(String v) {
        if (StringUtils.isNotEmpty(v)) {
            for (QueryFilterComparatorEnum c : QueryFilterComparatorEnum.values()) {
                if (v.equals(c.name())) return c;
            }
        }
        return null;
    }
}
