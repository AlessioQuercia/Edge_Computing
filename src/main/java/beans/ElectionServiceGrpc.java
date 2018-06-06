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
    comments = "Source: ElectionService.proto")
public final class ElectionServiceGrpc {

  private ElectionServiceGrpc() {}

  public static final String SERVICE_NAME = "beans.ElectionService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<beans.ElectionServiceOuterClass.ElectionRequest,
      beans.ElectionServiceOuterClass.ElectionResponse> METHOD_SEND_ELECTION_MESSAGE =
      io.grpc.MethodDescriptor.<beans.ElectionServiceOuterClass.ElectionRequest, beans.ElectionServiceOuterClass.ElectionResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "beans.ElectionService", "sendElectionMessage"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.ElectionServiceOuterClass.ElectionRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              beans.ElectionServiceOuterClass.ElectionResponse.getDefaultInstance()))
          .setSchemaDescriptor(new ElectionServiceMethodDescriptorSupplier("sendElectionMessage"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ElectionServiceStub newStub(io.grpc.Channel channel) {
    return new ElectionServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ElectionServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ElectionServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ElectionServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ElectionServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static abstract class ElectionServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void sendElectionMessage(beans.ElectionServiceOuterClass.ElectionRequest request,
        io.grpc.stub.StreamObserver<beans.ElectionServiceOuterClass.ElectionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SEND_ELECTION_MESSAGE, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_SEND_ELECTION_MESSAGE,
            asyncUnaryCall(
              new MethodHandlers<
                beans.ElectionServiceOuterClass.ElectionRequest,
                beans.ElectionServiceOuterClass.ElectionResponse>(
                  this, METHODID_SEND_ELECTION_MESSAGE)))
          .build();
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class ElectionServiceStub extends io.grpc.stub.AbstractStub<ElectionServiceStub> {
    private ElectionServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ElectionServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ElectionServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ElectionServiceStub(channel, callOptions);
    }

    /**
     */
    public void sendElectionMessage(beans.ElectionServiceOuterClass.ElectionRequest request,
        io.grpc.stub.StreamObserver<beans.ElectionServiceOuterClass.ElectionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SEND_ELECTION_MESSAGE, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class ElectionServiceBlockingStub extends io.grpc.stub.AbstractStub<ElectionServiceBlockingStub> {
    private ElectionServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ElectionServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ElectionServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ElectionServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public beans.ElectionServiceOuterClass.ElectionResponse sendElectionMessage(beans.ElectionServiceOuterClass.ElectionRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SEND_ELECTION_MESSAGE, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Defining a Service, a Service can have multiple RPC operations
   * </pre>
   */
  public static final class ElectionServiceFutureStub extends io.grpc.stub.AbstractStub<ElectionServiceFutureStub> {
    private ElectionServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ElectionServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ElectionServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ElectionServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<beans.ElectionServiceOuterClass.ElectionResponse> sendElectionMessage(
        beans.ElectionServiceOuterClass.ElectionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SEND_ELECTION_MESSAGE, getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND_ELECTION_MESSAGE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ElectionServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ElectionServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND_ELECTION_MESSAGE:
          serviceImpl.sendElectionMessage((beans.ElectionServiceOuterClass.ElectionRequest) request,
              (io.grpc.stub.StreamObserver<beans.ElectionServiceOuterClass.ElectionResponse>) responseObserver);
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

  private static abstract class ElectionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ElectionServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return beans.ElectionServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ElectionService");
    }
  }

  private static final class ElectionServiceFileDescriptorSupplier
      extends ElectionServiceBaseDescriptorSupplier {
    ElectionServiceFileDescriptorSupplier() {}
  }

  private static final class ElectionServiceMethodDescriptorSupplier
      extends ElectionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ElectionServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (ElectionServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ElectionServiceFileDescriptorSupplier())
              .addMethod(METHOD_SEND_ELECTION_MESSAGE)
              .build();
        }
      }
    }
    return result;
  }
}
