package simulators;

import beans.Node;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Stream implements SensorStream
{
    private Set<Measurement> measurements;

    private Sensor sensor;

    private StreamObserver<SensorServiceOuterClass.MeasurementRequest> requestObserver;

    private int currentCommunicationNodeId;

    private boolean communicationOpen;

    public Stream(Sensor sensor)
    {
        this.sensor = sensor;
        this.measurements = new HashSet<Measurement>();
        this.communicationOpen = false;
        this.currentCommunicationNodeId = -1;
        System.out.println("Hi, I am a new stream!");

//        startAsynchronousStream();
    }

    @Override
    public void sendMeasurement(Measurement m)
    {
        String measurementMessage = "";
        // Se sono passati 10 secondi, allora richiedi il nodo più vicino al sensore
        if (Math.abs(System.currentTimeMillis() - sensor.getLastRequestTime()) >= 10000) {
            sensor.requestNearestNode();
            sensor.setLastRequestTime(System.currentTimeMillis());
            System.out.println("Nearest Node Requested!");
        }

        if (sensor.getCommunicationNode() == null)
        {
            communicationOpen = false;
            sensor.requestNearestNode();
            sensor.setLastRequestTime(System.currentTimeMillis());
//            System.out.print("Sto scartando i dati: ");
            measurementMessage = "Sto scartando i dati: ";
        }
        else if (sensor.getCommunicationNode() != null && sensor.getCommunicationNode().getId() != currentCommunicationNodeId)
        {
            communicationOpen = false;
            currentCommunicationNodeId = sensor.getCommunicationNode().getId();
//            System.out.print("Sto scardando i dati, ma ho trovato un nodo (" + sensor.getCommunicationNode().getId() + ") : ");
            measurementMessage = "Sto scardando i dati, ma ho trovato un nodo (" + sensor.getCommunicationNode().getId() + ") : ";
        }
        else if (sensor.getCommunicationNode() != null)
        {
            if (!communicationOpen)
                startAsynchronousStream();
            // Invio la misurazione al nodo a cui sono connesso
            try {
//                System.out.print("Sto inviando dati al nodo " + sensor.getCommunicationNode().getId() + ": ");
                measurementMessage = "Sto inviando dati al nodo " + sensor.getCommunicationNode().getId() + ": ";
                requestObserver.onNext(SensorServiceOuterClass.MeasurementRequest.newBuilder().
                        setId(m.getId()).setType(m.getType()).setValue(m.getValue()).setTimestamp(m.getTimestamp()).build());
            } catch (Exception e)
            {
                requestObserver.onError(e);
            }

        }
        this.measurements.add(m);
        System.out.println(measurementMessage + m);
    }

    //calling an asynchronous method based on stream
    public void startAsynchronousStream()
    {
        // Apro un canale di comunicazione con il nodo più vicino
        communicationOpen = true;

        //plaintext channel on the address (ip/port) which offers the GreetingService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(sensor.getCommunicationNode().getIpAddress() + ":" + sensor.getCommunicationNode().getSensorsPort()).usePlaintext(true).build();

        //creating an asynchronous stub on the channel
        SensorServiceGrpc.SensorServiceStub stub = SensorServiceGrpc.newStub(channel);

        requestObserver = stub.streamToNode(new StreamObserver<SensorServiceOuterClass.MeasurementResponse>() {
            @Override
            //this hanlder takes care of each item received in the stream
            public void onNext(SensorServiceOuterClass.MeasurementResponse measurementResponse) {
                //each item is just printed
                System.out.println(measurementResponse.getAck());
            }

            @Override
            //if there are some errors, this method will be called
            public void onError(Throwable throwable) {
                System.out.println("Error! " + throwable.getMessage());
            }

            @Override
            //when the stream is completed (the server called "onCompleted") just close the channel
            public void onCompleted() {
                System.out.println("StreamToNode completed!");
                channel.shutdownNow();
            }
        });


        //you need this. otherwise the method will terminate before that answers from the server are received
        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
