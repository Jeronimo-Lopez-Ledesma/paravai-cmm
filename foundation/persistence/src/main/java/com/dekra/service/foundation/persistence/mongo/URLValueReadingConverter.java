package com.dekra.service.foundation.persistence.mongo;

import com.dekra.service.foundation.domaincore.value.URLValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class URLValueReadingConverter implements Converter<String, URLValue> {
    @Override
    public URLValue convert(String source) {
        return URLValue.of(source);
    }
}
