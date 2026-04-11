package com.paravai.communities.offer.api.grpc;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@GrpcGlobalServerInterceptor
public class RequestContextGrpcServerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestContextGrpcServerInterceptor.class);

    private static final Metadata.Key<String> TRACE_ID_HEADER =
            Metadata.Key.of("traceid", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> USER_OID_HEADER =
            Metadata.Key.of("useroid", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> SOURCE_SYSTEM_HEADER =
            Metadata.Key.of("sourcesystem", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String traceId = headers.get(TRACE_ID_HEADER);
        String userOid = headers.get(USER_OID_HEADER);
        String sourceSystem = headers.get(SOURCE_SYSTEM_HEADER);

        log.info("[grpc-server][offer] Incoming metadata - traceId={}, userOid={}, sourceSystem={}, method={}",
                traceId,
                userOid,
                sourceSystem,
                call.getMethodDescriptor().getFullMethodName()
        );

        Context context = Context.current()
                .withValue(OfferInternalCommandGrpcAdapter.TRACE_ID_CTX_KEY, traceId)
                .withValue(OfferInternalCommandGrpcAdapter.USER_OID_CTX_KEY, userOid)
                .withValue(OfferInternalCommandGrpcAdapter.SOURCE_SYSTEM_CTX_KEY, sourceSystem);

        return Contexts.interceptCall(context, call, headers, next);
    }
}