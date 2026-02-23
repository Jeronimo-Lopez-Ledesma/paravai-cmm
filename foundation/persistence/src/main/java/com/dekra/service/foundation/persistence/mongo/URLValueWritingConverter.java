package com.dekra.service.foundation.persistence.mongo;

import com.dekra.service.foundation.domaincore.value.URLValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class URLValueWritingConverter implements Converter<URLValue, String> {
    @Override
    public String convert(URLValue source) {
        return source.toString();
    }
}
