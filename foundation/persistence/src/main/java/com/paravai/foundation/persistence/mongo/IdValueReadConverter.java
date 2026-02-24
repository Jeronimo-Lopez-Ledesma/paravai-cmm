package com.paravai.foundation.persistence.mongo;


import com.paravai.foundation.domain.value.IdValue;
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
