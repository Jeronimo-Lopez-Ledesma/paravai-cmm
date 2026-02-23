package com.dekra.service.regulations.standards.api.grpc.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class StandardQueryServiceGrpc {

  private StandardQueryServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "dekra.regulations.standards.v1.StandardQueryService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest,
      com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse> getGetStandardByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetStandardById",
      requestType = com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest.class,
      responseType = com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest,
      com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse> getGetStandardByIdMethod() {
    io.grpc.MethodDescriptor<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest, com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse> getGetStandardByIdMethod;
    if ((getGetStandardByIdMethod = StandardQueryServiceGrpc.getGetStandardByIdMethod) == null) {
      synchronized (StandardQueryServiceGrpc.class) {
        if ((getGetStandardByIdMethod = StandardQueryServiceGrpc.getGetStandardByIdMethod) == null) {
          StandardQueryServiceGrpc.getGetStandardByIdMethod = getGetStandardByIdMethod =
              io.grpc.MethodDescriptor.<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest, com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetStandardById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse.getDefaultInstance()))
              .setSchemaDescriptor(new StandardQueryServiceMethodDescriptorSupplier("GetStandardById"))
              .build();
        }
      }
    }
    return getGetStandardByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest,
      com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse> getSearchStandardsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchStandards",
      requestType = com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest.class,
      responseType = com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest,
      com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse> getSearchStandardsMethod() {
    io.grpc.MethodDescriptor<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest, com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse> getSearchStandardsMethod;
    if ((getSearchStandardsMethod = StandardQueryServiceGrpc.getSearchStandardsMethod) == null) {
      synchronized (StandardQueryServiceGrpc.class) {
        if ((getSearchStandardsMethod = StandardQueryServiceGrpc.getSearchStandardsMethod) == null) {
          StandardQueryServiceGrpc.getSearchStandardsMethod = getSearchStandardsMethod =
              io.grpc.MethodDescriptor.<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest, com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchStandards"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new StandardQueryServiceMethodDescriptorSupplier("SearchStandards"))
              .build();
        }
      }
    }
    return getSearchStandardsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StandardQueryServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceStub>() {
        @java.lang.Override
        public StandardQueryServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StandardQueryServiceStub(channel, callOptions);
        }
      };
    return StandardQueryServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static StandardQueryServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceBlockingV2Stub>() {
        @java.lang.Override
        public StandardQueryServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StandardQueryServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return StandardQueryServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StandardQueryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceBlockingStub>() {
        @java.lang.Override
        public StandardQueryServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StandardQueryServiceBlockingStub(channel, callOptions);
        }
      };
    return StandardQueryServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static StandardQueryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StandardQueryServiceFutureStub>() {
        @java.lang.Override
        public StandardQueryServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StandardQueryServiceFutureStub(channel, callOptions);
        }
      };
    return StandardQueryServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getStandardById(com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest request,
        io.grpc.stub.StreamObserver<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetStandardByIdMethod(), responseObserver);
    }

    /**
     */
    default void searchStandards(com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest request,
        io.grpc.stub.StreamObserver<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchStandardsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service StandardQueryService.
   */
  public static abstract class StandardQueryServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return StandardQueryServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service StandardQueryService.
   */
  public static final class StandardQueryServiceStub
      extends io.grpc.stub.AbstractAsyncStub<StandardQueryServiceStub> {
    private StandardQueryServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StandardQueryServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StandardQueryServiceStub(channel, callOptions);
    }

    /**
     */
    public void getStandardById(com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest request,
        io.grpc.stub.StreamObserver<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetStandardByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchStandards(com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest request,
        io.grpc.stub.StreamObserver<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchStandardsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service StandardQueryService.
   */
  public static final class StandardQueryServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<StandardQueryServiceBlockingV2Stub> {
    private StandardQueryServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StandardQueryServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StandardQueryServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    public com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse getStandardById(com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetStandardByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse searchStandards(com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSearchStandardsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service StandardQueryService.
   */
  public static final class StandardQueryServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<StandardQueryServiceBlockingStub> {
    private StandardQueryServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StandardQueryServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StandardQueryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse getStandardById(com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetStandardByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse searchStandards(com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchStandardsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service StandardQueryService.
   */
  public static final class StandardQueryServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<StandardQueryServiceFutureStub> {
    private StandardQueryServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StandardQueryServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StandardQueryServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse> getStandardById(
        com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetStandardByIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse> searchStandards(
        com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchStandardsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_STANDARD_BY_ID = 0;
  private static final int METHODID_SEARCH_STANDARDS = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_STANDARD_BY_ID:
          serviceImpl.getStandardById((com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest) request,
              (io.grpc.stub.StreamObserver<com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse>) responseObserver);
          break;
        case METHODID_SEARCH_STANDARDS:
          serviceImpl.searchStandards((com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest) request,
              (io.grpc.stub.StreamObserver<com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetStandardByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest,
              com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse>(
                service, METHODID_GET_STANDARD_BY_ID)))
        .addMethod(
          getSearchStandardsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest,
              com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse>(
                service, METHODID_SEARCH_STANDARDS)))
        .build();
  }

  private static abstract class StandardQueryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    StandardQueryServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.dekra.service.regulations.standards.api.grpc.v1.StandardQueryServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("StandardQueryService");
    }
  }

  private static final class StandardQueryServiceFileDescriptorSupplier
      extends StandardQueryServiceBaseDescriptorSupplier {
    StandardQueryServiceFileDescriptorSupplier() {}
  }

  private static final class StandardQueryServiceMethodDescriptorSupplier
      extends StandardQueryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    StandardQueryServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (StandardQueryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new StandardQueryServiceFileDescriptorSupplier())
              .addMethod(getGetStandardByIdMethod())
              .addMethod(getSearchStandardsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
