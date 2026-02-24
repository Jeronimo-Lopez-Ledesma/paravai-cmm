package com.paravai.foundation.persistence.mongo;

import com.paravai.foundation.domain.value.IdValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

// Writing: IdValue a String
@WritingConverter
public class IdValueWriteConverter implements Converter<IdValue, String> {
    @Override
    public String convert(IdValue source) {
        return source.value();
    }
}

