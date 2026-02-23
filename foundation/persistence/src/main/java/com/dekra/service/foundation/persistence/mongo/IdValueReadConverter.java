package com.dekra.service.foundation.persistence.mongo;


import com.dekra.service.foundation.domaincore.value.IdValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

// Reading: String a IdValue
@ReadingConverter
public class IdValueReadConverter implements Converter<String, IdValue> {
    @Override
    public IdValue convert(String source) {
        return IdValue.of(source);
    }
}
