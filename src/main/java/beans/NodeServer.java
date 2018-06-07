package beans;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import simulators.Measurement;
import simulators.SensorServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class NodeServer extends Thread
{
    private Node node;

    private Server server;

    private boolean stop;

    private long midnight;

    private long lastTime;

    private SensorServiceImpl sensorService;

    private Server coordinatorServer;

    private Server notCoordinatorServer;

    private NodeServiceImpl nodeService;

    private NodeServiceGrpc.NodeServiceStub coordinatorStub;

    private ManagedChannel coordinatorChannel;

    private Object electionLock = new Object();

    private boolean bindToCoord;
    private boolean electionRequestDone;
    private boolean error;

    private int currentElectionNumber;
    private int previousElectionNumber;

    public NodeServer(Node node)
    {
        this.node = node;
        this.stop = false;
        this.midnight = computeMidnightMilliseconds();
        this.lastTime = System.currentTimeMillis();
        this.coordinatorStub = null;
        this.coordinatorChannel = null;
        this.bindToCoord = false;
        this.electionRequestDone = false;
        this.currentElectionNumber = 0;
        this.previousElectionNumber = 0;
    }

    @Override
    public void run ()
    {
        // Il nodo si mette a disposizione come server in ascolto sulla porta dedicata ai sensori
        try {

            sensorService = new SensorServiceImpl();

            server = ServerBuilder.forPort(node.getSensorsPort()).
                    addService(sensorService).
                    build();

            server.start();

            System.out.println("Server started for node " + node.getId() + "!");

            switch (node.getState())
            {
                case COORDINATOR: {
                    nodeService = new NodeServiceImpl(node);

                    CoordServiceImpl coordService = new CoordServiceImpl(node);
                    ElectionServiceImpl electionService = new ElectionServiceImpl(node);

                    coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
                            addService(nodeService).
                            addService(coordService).
                            addService(electionService).
                            build();

                    coordinatorServer.start();

                    System.out.println("Coordinator server started, node " + node.getId() + "!");

                    break;
                }

                case NOT_COORDINATOR: {
                    // Dopo aver conosciuto il nodo coordinatore, apre il servizio per eventuali richeste su chi sia il coordinatore
                    CoordServiceImpl coordService = new CoordServiceImpl(node);

                    ElectionServiceImpl electionService = new ElectionServiceImpl(node);

                    notCoordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
                            addService(coordService).
                            addService(electionService).
                            build();

                    notCoordinatorServer.start();

                    break;
                }
            }

            synchronized (sensorService.bufferLock)
            {
                while (sensorService.insertCounter != 40 && !stop)
                {
                    sensorService.bufferLock.wait();
                    if (sensorService.insertCounter == 40)
                    {
                        double sum = 0;
                        ArrayList<Measurement> bufferCopy = sensorService.getMeasurementsBuffer();
                        for (int i = 0; i < 40; i++)
                            sum += bufferCopy.get(i).getValue();
                        double mean = sum / 40;

                        // Crea la statistica locale
                        Stat localStat = new Stat(node.getId(), mean, deltaTime());

                        switch (node.getState())
                        {
                            case NOT_COORDINATOR: {
                                if (electionRequestDone)
                                {
                                    bindToCoord = false;
                                    electionRequestDone = false;
                                }
                                // Se non è collegato al coordinatore, collegalo
                                if (!bindToCoord)
                                {
                                    System.out.println(node.getCoordinatorPort());
                                    String serverAddress = "localhost:" + node.getCoordinatorPort();

                                    connectToCoordinator(serverAddress);

                                    bindToCoord = true;
                                }

                                // Invia la statistica locale al nodo coordinatore e aggiunge alla propria lista delle statistiche
                                // globali la risposta (ultima statistica globale) inviata dal coordinatore
                                sendToCoordinator(localStat);


                                if (Math.abs(System.currentTimeMillis() - lastTime) >= 5000)
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

                                break;
                            }
                            case COORDINATOR: {
                                if (Math.abs(System.currentTimeMillis() - lastTime) >= 5000)
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

                                break;
                            }
                            case ELECTING_COORDINATOR:
                                {
                                    System.out.println("Coordinator election in progress!");
                                    break;
                                }
                            }
                            node.addLocalStat(localStat);
                        }

                        sensorService.resetInsertCounter();
                        sensorService.getBuffer().freeTwenty();
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
                    Stat globalStat = new Stat(response.getNodeId(), response.getValue(), response.getTimestamp());

//                    System.out.println("Global stat ricevuta: " + globalStat);

                    node.addGlobalStat(globalStat);
                }
            }

            @Override
            //if there are some errors, this method will be called
            public void onError(Throwable throwable)
            {

                synchronized (electionLock)
                {
                    if (node.getState() != beans.State.ELECTING_COORDINATOR && !electionRequestDone && previousElectionNumber == currentElectionNumber)
                    {
                        electionRequestDone = true;

                        // Start a new election
                        node.setState(beans.State.ELECTING_COORDINATOR);

                        Node nextNode = null;

                        if (node.getNextNodes().size() > 0) {
                            nextNode = node.getNextNodes().get(0);
                        }

                        if (nextNode != null && nextNode.getState() == beans.State.COORDINATOR && node.getNextNodes().size() > 1) {
                            nextNode = node.getNextNodes().get(1);
                            //                        node.getNextNodes().remove(nextNode);
                            //                        node.getNodesList().remove(nextNode);
                        } else if (nextNode != null && nextNode.getState() == beans.State.COORDINATOR && node.getNextNodes().size() <= 1)
                            nextNode = null;

                        if (nextNode != null)
                        {
                            System.out.println(nextNode + " " + nextNode.getState());
                            node.sendElectionMessage(nextNode, "ELECTING", node.getId());
                        }

                        else {
                            // Elezione conclusa
                            System.out.println("ELEZIONE CONCLUSA");

                            // Si imposta coordinatore
                            node.setState(beans.State.COORDINATOR);

                            node.setCoordinatorPort(node.getNodesPort());

                            // Apre la connessione con il Server Cloud
                            System.out.println(node.getServerAddress());
                            node.setNodeClient(new NodeClient(node, node.getServerAddress()));
                            node.getNodeClient().start();

                            // Chiude la connessione con gli altri da non coordinatore
                            node.getNodeServer().getNotCoordinatorServer().shutdownNow();

                            // Apre la connessione con gli altri da coordinatore
                            NodeServiceImpl nodeService = new NodeServiceImpl(node);
                            CoordServiceImpl coordService = new CoordServiceImpl(node);
                            ElectionServiceImpl electionService = new ElectionServiceImpl(node);

                            Server coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
                                    addService(nodeService).
                                    addService(coordService).
                                    addService(electionService).
                                    build();
                            node.getNodeServer().setCoordinatorServer(coordinatorServer);
                            try {
                                node.getNodeServer().getCoordinatorServer().start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            System.out.println("Coordinator server started, node " + node.getId() + "!");

                            for (Node n : node.getNextNodes())
                                if (n.getState() == beans.State.COORDINATOR)
                                    node.getNextNodes().remove(n);

                            for (Node n : node.getNodesList())
                                if (n.getState() == beans.State.COORDINATOR)
                                    node.getNodesList().remove(n);

                            System.out.println(node.getNextNodes());
                            System.out.println(node.getNodesList());

                            currentElectionNumber++;
                        }
                    }
                }

//                System.out.println("Error! " + throwable.getMessage());
            }

            @Override
            //when the stream is completed (the server called "onCompleted") just close the channel
            public void onCompleted()
            {
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
    }

    public void setCoordinatorServer(Server coordinatorServer)
    {
        this.coordinatorServer = coordinatorServer;
    }

    public Server getCoordinatorServer()
    {
        return this.coordinatorServer;
    }

    public void setNotCoordinatorServer(Server notCoordinatorServer)
    {
        this.notCoordinatorServer = notCoordinatorServer;
    }

    public Server getNotCoordinatorServer()
    {
        return notCoordinatorServer;
    }
}
