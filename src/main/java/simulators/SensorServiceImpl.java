package simulators;

import beans.MeasurementsBuffer;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;

public class SensorServiceImpl extends SensorServiceGrpc.SensorServiceImplBase {

    private MeasurementsBuffer measurementsBuffer;

    public Object bufferLock = new Object();

    public int insertCounter = 0;

    public SensorServiceImpl()
    {
        this.measurementsBuffer = new MeasurementsBuffer();
    }

    @Override
    public StreamObserver<simulators.SensorServiceOuterClass.MeasurementRequest> streamToNode(final StreamObserver<SensorServiceOuterClass.MeasurementResponse> responseObserver)
    {
        return new StreamObserver<SensorServiceOuterClass.MeasurementRequest>()
        {
            @Override
            public void onNext(SensorServiceOuterClass.MeasurementRequest measurementRequest)
            {
                Measurement m = new Measurement(measurementRequest.getId(), measurementRequest.getType(),
                        measurementRequest.getValue(), measurementRequest.getTimestamp());
                measurementsBuffer.put(m);

                synchronized (bufferLock)
                {
                    if (insertCounter == 40)
                    {
//                        System.out.println("Notify");
                        bufferLock.notifyAll();
                    }
                    else
                    {
                        insertCounter++;
                    }
                }
//                System.out.println("Measurement received!");
            }

            @Override
            public void onError(Throwable throwable)
            {
                if (throwable.getMessage() == "CANCELLED")
                {
                    synchronized (bufferLock)
                    {
                        insertCounter = 40;
                        bufferLock.notifyAll();
                        System.out.println("Comunicazione con nodo interrotta!");
                    }
                }
                else
                    System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted()
            {
//                responseObserver.onNext(SensorServiceOuterClass.MeasurementResponse.newBuilder().setAck("Comunication closed").build());

                responseObserver.onCompleted();
            }
        };
    }

    public ArrayList<Measurement> getMeasurementsBuffer()
    {
        return new ArrayList<Measurement>(measurementsBuffer.buffer);
    }

    public MeasurementsBuffer getBuffer()
    {
        return measurementsBuffer;
    }

    public void resetInsertCounter()
    {
        insertCounter -= 20;
    }
}
