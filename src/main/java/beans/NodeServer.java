package beans;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import simulators.Measurement;
import simulators.SensorServiceImpl;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class NodeServer extends Thread
{
    private Node node;

    private Server server;

    private boolean stop;

    private beans.State nodeState;

    private long midnight;

    private long lastTime;

    private SensorServiceImpl sensorService;

    private Server coordinatorServer;

    private NodeServiceImpl nodeService;

    private NodeServiceGrpc.NodeServiceStub coordinatorStub;

    private ManagedChannel coordinatorChannel;

    private boolean bindToCoord;

    public NodeServer(Node node, beans.State nodeState)
    {
        this.node = node;
        this.nodeState = nodeState;
        this.stop = false;
        this.midnight = computeMidnightMilliseconds();
        this.lastTime = System.currentTimeMillis();
        this.coordinatorStub = null;
        this.coordinatorChannel = null;
        this.bindToCoord = false;
    }

    @Override
    public void run ()
    {
        // Il nodo si mette a disposizione come server in ascolto sulla porta dedicata ai sensori
        try {

            sensorService = new SensorServiceImpl();

            server = ServerBuilder.forPort(node.getSensorsPort()).addService(sensorService).build();

            server.start();

            System.out.println("Server started for node " + node.getId() + "!");

            switch (nodeState)
            {
                case COORDINATOR: {
                    nodeService = new NodeServiceImpl(node);

                    coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).addService(nodeService).build();

                    coordinatorServer.start();

                    System.out.println("Coordinator server started, node " + node.getId() + "!");
                }
            }

            synchronized (sensorService.bufferLock)
            {
                while (sensorService.insertCounter != 40 && !stop)
                {
                    sensorService.bufferLock.wait();
                    if (sensorService.insertCounter == 40) {
                        double sum = 0;
                        ArrayList<Measurement> bufferCopy = sensorService.getMeasurementsBuffer();
                        for (int i = 0; i < 40; i++)
                            sum += bufferCopy.get(i).getValue();
                        double mean = sum / 40;

                        // Crea la statistica locale
                        Stat localStat = new Stat(mean, deltaTime());

                        if (nodeState == beans.State.NOT_COORDINATOR)
                        {
                            // Se non è collegato al coordinatore, collegalo
                            if (!bindToCoord)
                            {
                                String serverAddress = "localhost:" + node.getNodesPort();

                                connectToCoordinator(serverAddress);
                            }
                            // Invia la statistica locale al nodo coordinatore e aggiunge alla propria lista delle statistiche
                            // globali la risposta (ultima statistica globale) inviata dal coordinatore
                            sendToCoordinator(localStat);
                        }
                        else if (nodeState == beans.State.COORDINATOR)
                        {
                        }

                        node.addLocalStat(localStat);

//                        System.out.println();
//                        System.out.println("Node: " + node.getId() + "  " + stat.getMean() + "  " + stat.getTimestamp());
//                        System.out.print("Type 1 to remove the node from the Server Cloud: ");

                        sensorService.resetInsertCounter();
                        sensorService.getBuffer().freeTwenty();
                    }



                    // Print panel each 5 seconds (if not coordinator)
                    if (Math.abs(System.currentTimeMillis() - lastTime) >= 5000 && nodeState == beans.State.NOT_COORDINATOR)
                    {
                        System.out.println("\n");
                        System.out.println("Last global stats:");

                        Set<Stat> globalStats = node.getGlobalStats();
                        Stat[] globalStatsArray = globalStats.toArray(new Stat[globalStats.size()]);
                        int loopDim = Math.min(globalStatsArray.length, 3);
                        for (int i = 0; i<loopDim; i++)
                            System.out.println("Global stat:  " + globalStatsArray[globalStatsArray.length-1-i].getMean() + "  " + globalStatsArray[globalStatsArray.length-1-i].getTimestamp());

                        System.out.println("Last local stats:");

                        Set<Stat> localStats = node.getLocalStats();
                        Stat[] localStatsArray = localStats.toArray(new Stat[localStats.size()]);
                        loopDim = Math.min(localStatsArray.length, 3);
                        for (int i = 0; i<loopDim; i++)
                            System.out.println("Node: " + node.getId() + "  " + localStatsArray[localStatsArray.length-1-i].getMean() + "  " + localStatsArray[localStatsArray.length-1-i].getTimestamp());

                        System.out.println();
                        System.out.print("Type \'q\' to remove the node from the Server Cloud: ");

                        lastTime = System.currentTimeMillis();
                    }

                    else if (Math.abs(System.currentTimeMillis() - lastTime) >= 5000 && nodeState == beans.State.COORDINATOR)
                    {
                        // PANNELLO COORDINATORE (STAMPA LE STATISTICHE LOCALI DI OGNI NODO)
                        System.out.println("\n");
                        System.out.println("Last global stats:");

                        Set<Stat> globalStats = node.getGlobalStats();
                        Stat[] globalStatsArray = globalStats.toArray(new Stat[globalStats.size()]);
                        int loopDim = Math.min(globalStatsArray.length, 3);
                        for (int i = 0; i<loopDim; i++)
                            System.out.println("Global stat:  " + globalStatsArray[globalStatsArray.length-1-i].getMean() + "  " + globalStatsArray[globalStatsArray.length-1-i].getTimestamp());

                        System.out.println("Last local stats:");

                        Set<Stat> localStats = node.getLocalStats();
                        Stat[] localStatsArray = localStats.toArray(new Stat[localStats.size()]);
                        loopDim = Math.min(localStatsArray.length, 3);
                        for (int i = 0; i<loopDim; i++)
                            System.out.println("Node: " + node.getId() + "  " + localStatsArray[localStatsArray.length-1-i].getMean() + "  " + localStatsArray[localStatsArray.length-1-i].getTimestamp());

                        System.out.println();
                        System.out.print("Type \'q\' to remove the node from the Server Cloud: ");

                        lastTime = System.currentTimeMillis();
                    }

                }
            }

//            server.awaitTermination();
            System.out.println("SONO FUORI");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Server getServer()
    {
        return server;
    }

    public void setStop()
    {
        stop = true;
    }

    private long computeMidnightMilliseconds(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long deltaTime(){
        return System.currentTimeMillis()-midnight;
    }

    public SensorServiceImpl getSensorService() {
        return sensorService;
    }

    public NodeServiceImpl getNodeService() {
        return nodeService;
    }

    //calling a synchronous rpc operation
    private void sendToCoordinator(Stat localStat){

        StreamObserver<NodeServiceOuterClass.LocalStatRequest> requestObserver = coordinatorStub.streamToCoordinator(new StreamObserver<NodeServiceOuterClass.GlobalStatResponse>() {
            @Override
            //this hanlder takes care of each item received in the stream
            public void onNext(NodeServiceOuterClass.GlobalStatResponse response) {

                // Ricezione della risposta del server (global stat)
                // Se sono presenti statistiche globali (quindi ne è stata inviata una) allora salvala
                if (response != null)
                {
                    Stat globalStat = new Stat(response.getValue(), response.getTimestamp());

//                    System.out.println("Global stat ricevuta: " + globalStat);

                    node.addGlobalStat(globalStat);
                }
            }

            @Override
            //if there are some errors, this method will be called
            public void onError(Throwable throwable) {
                System.out.println("Error! " + throwable.getMessage());
            }

            @Override
            //when the stream is completed (the server called "onCompleted") just close the channel
            public void onCompleted() {
                System.out.println("StreamToStreamSum completed!");
                coordinatorChannel.shutdownNow();
            }
        });

        // Invia la local stat al coordinatore
        try {
            requestObserver.onNext(NodeServiceOuterClass.LocalStatRequest.newBuilder().
                    setValue(localStat.getMean()).setTimestamp(localStat.getTimestamp()).build());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void connectToCoordinator(String serverAddress)
    {
        //plaintext channel on the address (ip/port) which offers the MethodsService service
        coordinatorChannel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext(true).build();

        //creating an asynchronous stub on the channel
        coordinatorStub = NodeServiceGrpc.newStub(coordinatorChannel);

        bindToCoord = true;
    }
}
