package com.dekra.service.foundation.securityutils.reactive.context;

import reactor.util.context.ContextView;

public class RequestContext {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String USER_OID_KEY = "userOid";
    public static final String SOURCE_SYSTEM_KEY = "sourceSystem";

    public static String getTraceId(ContextView ctx) {
        return ctx.getOrDefault(TRACE_ID_KEY, "missing-trace-id");
    }

    public static String getUserOid(ContextView ctx) {
        return ctx.getOrDefault(USER_OID_KEY, "anonymous");
    }

    public static String getSourceSystem(ContextView ctx) {
        return ctx.getOrDefault(SOURCE_SYSTEM_KEY, "unknown");
    }


}