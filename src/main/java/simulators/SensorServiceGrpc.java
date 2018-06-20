package simulators;

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
    comments = "Source: SensorService.proto")
public final class SensorServiceGrpc {

  private SensorServiceGrpc() {}

  public static final String SERVICE_NAME = "simulators.SensorService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<simulators.SensorServiceOuterClass.MeasurementRequest,
      simulators.SensorServiceOuterClass.MeasurementResponse> METHOD_STREAM_TO_NODE =
      io.grpc.MethodDescriptor.<simulators.SensorServiceOuterClass.MeasurementRequest, simulators.SensorServiceOuterClass.MeasurementResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "simulators.SensorService", "streamToNode"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              simulators.SensorServiceOuterClass.MeasurementRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              simulators.SensorServiceOuterClass.MeasurementResponse.getDefaultInstance()))
          .setSchemaDescriptor(new SensorServiceMethodDescriptorSupplier("streamToNode"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SensorServiceStub newStub(io.grpc.Channel channel) {
    return new SensorServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SensorServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new SensorServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SensorServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new SensorServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static abstract class SensorServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<simulators.SensorServiceOuterClass.MeasurementRequest> streamToNode(
        io.grpc.stub.StreamObserver<simulators.SensorServiceOuterClass.MeasurementResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_STREAM_TO_NODE, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_STREAM_TO_NODE,
            asyncClientStreamingCall(
              new MethodHandlers<
                simulators.SensorServiceOuterClass.MeasurementRequest,
                simulators.SensorServiceOuterClass.MeasurementResponse>(
                  this, METHODID_STREAM_TO_NODE)))
          .build();
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class SensorServiceStub extends io.grpc.stub.AbstractStub<SensorServiceStub> {
    private SensorServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SensorServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SensorServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SensorServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<simulators.SensorServiceOuterClass.MeasurementRequest> streamToNode(
        io.grpc.stub.StreamObserver<simulators.SensorServiceOuterClass.MeasurementResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_STREAM_TO_NODE, getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class SensorServiceBlockingStub extends io.grpc.stub.AbstractStub<SensorServiceBlockingStub> {
    private SensorServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SensorServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SensorServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SensorServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class SensorServiceFutureStub extends io.grpc.stub.AbstractStub<SensorServiceFutureStub> {
    private SensorServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SensorServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SensorServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SensorServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_STREAM_TO_NODE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SensorServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SensorServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_STREAM_TO_NODE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamToNode(
              (io.grpc.stub.StreamObserver<simulators.SensorServiceOuterClass.MeasurementResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class SensorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SensorServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return simulators.SensorServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SensorService");
    }
  }

  private static final class SensorServiceFileDescriptorSupplier
      extends SensorServiceBaseDescriptorSupplier {
    SensorServiceFileDescriptorSupplier() {}
  }

  private static final class SensorServiceMethodDescriptorSupplier
      extends SensorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    SensorServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (SensorServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SensorServiceFileDescriptorSupplier())
              .addMethod(METHOD_STREAM_TO_NODE)
              .build();
        }
      }
    }
    return result;
  }
}
