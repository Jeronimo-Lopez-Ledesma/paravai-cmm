package com.dekra.service.foundation.domaincore.value;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Schema(hidden = true)
public class SourceSystemValue
{
    private final IdValue sourceSystemId;
    private final IdValue sourceId;

    private SourceSystemValue(IdValue sourceSystemId, IdValue sourceId)
    {
        this.sourceSystemId = sourceSystemId;
        this.sourceId = sourceId;
    }

    public static SourceSystemValue of(String sourceSystemId, String sourceId)
    {
        return new SourceSystemValue(IdValue.of(sourceSystemId), IdValue.of(sourceId));
    }

    @Override
    public String toString() {
        return String.format("SourceSystem[sourceSystemId=%s, sourceId=%s]",
                sourceSystemId.toString(), sourceId.toString());
    }
}
