package simulators;

import io.grpc.stub.StreamObserver;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SensorServiceImpl extends SensorServiceGrpc.SensorServiceImplBase {

    private Set<Measurement> measurements;

    public SensorServiceImpl()
    {
        this.measurements = new TreeSet<Measurement>();
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
                addMeasurement(m);
                System.out.println("Measurement received!");
            }

            @Override
            public void onError(Throwable throwable)
            {
                System.out.println("Errore! " + throwable.getMessage());
            }

            @Override
            public void onCompleted()
            {
//                responseObserver.onNext(SensorServiceOuterClass.MeasurementResponse.newBuilder().setAck("Comunication closed").build());

                responseObserver.onCompleted();
            }
        };
    }

    public synchronized void addMeasurement(Measurement measurement)
    {
        measurements.add(measurement);
    }

    public synchronized Set<Measurement> getMeasurements()
    {
        return measurements;
    }



}
