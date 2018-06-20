package beans;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * Defining a Service, a Service can have multiple RPC operations
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: CoordService.proto")
public final class CoordServiceGrpc {

  private CoordServiceGrpc() {}

  public static final String SERVICE_NAME = "beans.CoordService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<beans.CoordServiceOuterClass.NodeRequest,
      beans.CoordServiceOuterClass.CoordResponse> METHOD_ASK_FOR_COORDINATOR =
      io.grpc.MethodDescriptor.<beans.CoordServiceOuterClass.NodeRequest, beans.CoordServiceOuterClass.CoordResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "beans.CoordService", "askForCoordinator"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.CoordServiceOuterClass.NodeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.CoordServiceOuterClass.CoordResponse.getDefaultInstance()))
          .setSchemaDescriptor(new CoordServiceMethodDescriptorSupplier("askForCoordinator"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<beans.CoordServiceOuterClass.NodeRequest,
      beans.CoordServiceOuterClass.NodeResponse> METHOD_ADVICE_NODE =
      io.grpc.MethodDescriptor.<beans.CoordServiceOuterClass.NodeRequest, beans.CoordServiceOuterClass.NodeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "beans.CoordService", "adviceNode"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.CoordServiceOuterClass.NodeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.CoordServiceOuterClass.NodeResponse.getDefaultInstance()))
          .setSchemaDescriptor(new CoordServiceMethodDescriptorSupplier("adviceNode"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<beans.CoordServiceOuterClass.NodeRequest,
      beans.CoordServiceOuterClass.NodeResponse> METHOD_HI_COORDINATOR =
      io.grpc.MethodDescriptor.<beans.CoordServiceOuterClass.NodeRequest, beans.CoordServiceOuterClass.NodeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "beans.CoordService", "hiCoordinator"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.CoordServiceOuterClass.NodeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.CoordServiceOuterClass.NodeResponse.getDefaultInstance()))
          .setSchemaDescriptor(new CoordServiceMethodDescriptorSupplier("hiCoordinator"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CoordServiceStub newStub(io.grpc.Channel channel) {
    return new CoordServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CoordServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CoordServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CoordServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CoordServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static abstract class CoordServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void askForCoordinator(beans.CoordServiceOuterClass.NodeRequest request,
        io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.CoordResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ASK_FOR_COORDINATOR, responseObserver);
    }

    /**
     */
    public void adviceNode(beans.CoordServiceOuterClass.NodeRequest request,
        io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.NodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADVICE_NODE, responseObserver);
    }

    /**
     */
    public void hiCoordinator(beans.CoordServiceOuterClass.NodeRequest request,
        io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.NodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_HI_COORDINATOR, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_ASK_FOR_COORDINATOR,
            asyncUnaryCall(
              new MethodHandlers<
                beans.CoordServiceOuterClass.NodeRequest,
                beans.CoordServiceOuterClass.CoordResponse>(
                  this, METHODID_ASK_FOR_COORDINATOR)))
          .addMethod(
            METHOD_ADVICE_NODE,
            asyncUnaryCall(
              new MethodHandlers<
                beans.CoordServiceOuterClass.NodeRequest,
                beans.CoordServiceOuterClass.NodeResponse>(
                  this, METHODID_ADVICE_NODE)))
          .addMethod(
            METHOD_HI_COORDINATOR,
            asyncUnaryCall(
              new MethodHandlers<
                beans.CoordServiceOuterClass.NodeRequest,
                beans.CoordServiceOuterClass.NodeResponse>(
                  this, METHODID_HI_COORDINATOR)))
          .build();
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class CoordServiceStub extends io.grpc.stub.AbstractStub<CoordServiceStub> {
    private CoordServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CoordServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CoordServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CoordServiceStub(channel, callOptions);
    }

    /**
     */
    public void askForCoordinator(beans.CoordServiceOuterClass.NodeRequest request,
        io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.CoordResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ASK_FOR_COORDINATOR, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void adviceNode(beans.CoordServiceOuterClass.NodeRequest request,
        io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.NodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADVICE_NODE, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void hiCoordinator(beans.CoordServiceOuterClass.NodeRequest request,
        io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.NodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_HI_COORDINATOR, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class CoordServiceBlockingStub extends io.grpc.stub.AbstractStub<CoordServiceBlockingStub> {
    private CoordServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CoordServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CoordServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CoordServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public beans.CoordServiceOuterClass.CoordResponse askForCoordinator(beans.CoordServiceOuterClass.NodeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ASK_FOR_COORDINATOR, getCallOptions(), request);
    }

    /**
     */
    public beans.CoordServiceOuterClass.NodeResponse adviceNode(beans.CoordServiceOuterClass.NodeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADVICE_NODE, getCallOptions(), request);
    }

    /**
     */
    public beans.CoordServiceOuterClass.NodeResponse hiCoordinator(beans.CoordServiceOuterClass.NodeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_HI_COORDINATOR, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class CoordServiceFutureStub extends io.grpc.stub.AbstractStub<CoordServiceFutureStub> {
    private CoordServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CoordServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CoordServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CoordServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<beans.CoordServiceOuterClass.CoordResponse> askForCoordinator(
        beans.CoordServiceOuterClass.NodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ASK_FOR_COORDINATOR, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<beans.CoordServiceOuterClass.NodeResponse> adviceNode(
        beans.CoordServiceOuterClass.NodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADVICE_NODE, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<beans.CoordServiceOuterClass.NodeResponse> hiCoordinator(
        beans.CoordServiceOuterClass.NodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_HI_COORDINATOR, getCallOptions()), request);
    }
  }

  private static final int METHODID_ASK_FOR_COORDINATOR = 0;
  private static final int METHODID_ADVICE_NODE = 1;
  private static final int METHODID_HI_COORDINATOR = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CoordServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CoordServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ASK_FOR_COORDINATOR:
          serviceImpl.askForCoordinator((beans.CoordServiceOuterClass.NodeRequest) request,
              (io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.CoordResponse>) responseObserver);
          break;
        case METHODID_ADVICE_NODE:
          serviceImpl.adviceNode((beans.CoordServiceOuterClass.NodeRequest) request,
              (io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.NodeResponse>) responseObserver);
          break;
        case METHODID_HI_COORDINATOR:
          serviceImpl.hiCoordinator((beans.CoordServiceOuterClass.NodeRequest) request,
              (io.grpc.stub.StreamObserver<beans.CoordServiceOuterClass.NodeResponse>) responseObserver);
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

  private static abstract class CoordServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CoordServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return beans.CoordServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CoordService");
    }
  }

  private static final class CoordServiceFileDescriptorSupplier
      extends CoordServiceBaseDescriptorSupplier {
    CoordServiceFileDescriptorSupplier() {}
  }

  private static final class CoordServiceMethodDescriptorSupplier
      extends CoordServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CoordServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (CoordServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CoordServiceFileDescriptorSupplier())
              .addMethod(METHOD_ASK_FOR_COORDINATOR)
              .addMethod(METHOD_ADVICE_NODE)
              .addMethod(METHOD_HI_COORDINATOR)
              .build();
        }
      }
    }
    return result;
  }
}
