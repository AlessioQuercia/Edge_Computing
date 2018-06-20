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
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NodeServerToSensors extends Thread
{
    private Node node;

    private SensorServiceImpl sensorsService;

    private Server serverToSensors;

    private boolean stop;

    private long lastTime;

    private Object electionLock = new Object();

    private boolean bindToCoord;
    private boolean electionRequestDone;

    private int currentElectionNumber;
    private int previousElectionNumber;
    private ManagedChannel coordinatorChannel;
    private NodeServiceGrpc.NodeServiceStub coordinatorStub;

    public NodeServerToSensors(Node node)
    {
        this.node = node;

        this.stop = false;
        this.lastTime = System.currentTimeMillis();
        this.coordinatorStub = null;
        this.coordinatorChannel = null;
        this.bindToCoord = false;
        this.electionRequestDone = false;
        this.currentElectionNumber = 0;
        this.previousElectionNumber = 0;
    }

    @Override
    public void run()
    {
        sensorsService = new SensorServiceImpl();

//        node.setSensorsService(sensorsService);

        serverToSensors = ServerBuilder.forPort(this.node.getSensorsPort()).
                addService(sensorsService).
                build();

        try {
            serverToSensors.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        manageSensors();
    }

    private void manageSensors()
    {
        // GESTIONE MISURAZIONI
        sensorsService.insertCounter = 0;

        synchronized (sensorsService.bufferLock)
        {
            while (sensorsService.insertCounter != 40 && !stop)
            {
                try {
                    sensorsService.bufferLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (sensorsService.insertCounter == 40 && !stop)
                {
                    double sum = 0;
                    ArrayList<Measurement> bufferCopy = sensorsService.getMeasurementsBuffer();
                    for (int i = 0; i < 40; i++)
                        sum += bufferCopy.get(i).getValue();
                    double mean = sum / 40;

                    // Crea la statistica locale
                    Stat localStat = new Stat(node.getId(), mean, node.deltaTime());

                    if (node.getState() == beans.State.COORDINATOR)
                    {

                        synchronized (node.getRemovedNodes())
                        {
                            if (node.getRemovedNodes().size() > 0) {
                                for (Node n : node.getRemovedNodes())
                                {
//                                    node.advicePreviousNodes(node, n, "RIMOSSO");
                                    node.adviceNodes(node, n, "RIMOSSO");
                                }

                                node.getRemovedNodes().clear();
                            }
                        }

                    }

                    if (node.getState() == beans.State.NOT_COORDINATOR)
                    {
                        if (electionRequestDone)
                        {
                            bindToCoord = false;
                            electionRequestDone = false;
                        }
                        // Se non è collegato al coordinatore, collegalo
                        if (!bindToCoord && node.getCoordinatorPort() != -1)
                        {
                            System.out.println(node.getCoordinatorPort());
                            String serverAddress = "localhost:" + node.getCoordinatorPort();

//                            connectToCoordinator(serverAddress);

                            synchronized (node.getNodeServerToNodes().getCoordService().getElectingCoordinator())
                            {
                                node.getNodeServerToNodes().getCoordService().getElectingCoordinator().notifyAll();
                            }

                            bindToCoord = true;
                        }

                        // Invia la statistica locale al nodo coordinatore e aggiunge alla propria lista delle statistiche
                        // globali la risposta (ultima statistica globale) inviata dal coordinatore
                        if (node.getCoordinatorPort() != -1)
                            sendToCoordinator(localStat);
                    }

                    node.addLocalStat(localStat);

                    printPanel();

                    sensorsService.resetInsertCounter();
                    sensorsService.getBuffer().freeTwenty();
                }
            }

        }

//            server.awaitTermination();
        System.out.println("Server per i Sensori chiuso!");
    }

    private void connectToCoordinator(String serverAddress)
    {
        //plaintext channel on the address (ip/port) which offers the MethodsService service
        coordinatorChannel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext(true).build();

        //creating an asynchronous stub on the channel
        coordinatorStub = NodeServiceGrpc.newStub(coordinatorChannel);
    }

    public void setStop()
    {
        stop = true;
    }

    public void printPanel()
    {
        switch (node.getState())
        {
            case NOT_COORDINATOR: {
                if (Math.abs(System.currentTimeMillis() - lastTime) >= 5000)
                {
                    System.out.println("\n");
                    System.out.println("Last global stats:");

                    Set<Stat> globalStats = node.getGlobalStatsCopy();
                    Stat[] globalStatsArray = globalStats.toArray(new Stat[globalStats.size()]);
                    int loopDim = Math.min(globalStatsArray.length, 3);
                    for (int i = 0; i<loopDim; i++)
                        System.out.println("Global stat:  " + globalStatsArray[globalStatsArray.length-1-i].getMean() + "  " + globalStatsArray[globalStatsArray.length-1-i].getTimestamp());

                    System.out.println("Last local stats:");

                    Set<Stat> localStats = node.getLocalStatsCopy();
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
                    // PANNELLO COORDINATORE (STAMPA LE STATISTICHE GLOBALI E LE STATISTICHE LOCALI DI OGNI NODO)
                    System.out.println("\n");
                    System.out.println("Last global stats:");

                    Set<Stat> globalStats = node.getGlobalStatsCopy();
                    Stat[] globalStatsArray = globalStats.toArray(new Stat[globalStats.size()]);
                    int loopDim = Math.min(globalStatsArray.length, 3);
                    for (int i = 0; i<loopDim; i++)
                        System.out.println("Global stat:  " + globalStatsArray[globalStatsArray.length-1-i].getMean() + "  " + globalStatsArray[globalStatsArray.length-1-i].getTimestamp());

                    System.out.println("Last local stats:");

                    ArrayList<Node> nodes = node.getNodesListCopy();

                    Set<Stat> localStats = node.getLocalStatsCopy();
                    Stat[] localStatsArray = localStats.toArray(new Stat[localStats.size()]);
                    loopDim = Math.min(localStatsArray.length, 2);

                    for (Node n : nodes)
                    {
                        for (int i = localStatsArray.length-1; i>=0; i--)
                        {
                            if (localStatsArray[i].getNodeId() == n.getId())
                            {
                                System.out.println("Node: " + n.getId() + "  " +
                                        localStatsArray[i].getMean() + "  " +
                                        localStatsArray[i].getTimestamp());
                                loopDim--;
                            }
                            if (loopDim == 0)
                            {
                                loopDim = 2;
                                break;
                            }
                        }
                    }

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
    }

    private void sendToCoordinator(Stat localStat)
    {
        //plaintext channel on the address (ip/port) which offers the GreetingService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + node.getCoordinatorPort()).usePlaintext(true).build();

        //creating an asynchronous stub on the channel
        NodeServiceGrpc.NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

        NodeServiceOuterClass.LocalStatRequest request = NodeServiceOuterClass.LocalStatRequest.newBuilder().
                setNodeId(localStat.getNodeId()).
                setValue(localStat.getMean()).
                setTimestamp(localStat.getTimestamp()).
                build();

        stub.sendToCoordinator(request, new StreamObserver<NodeServiceOuterClass.GlobalStatResponse>() {
            @Override
            //this hanlder takes care of each item received in the stream
            public void onNext(NodeServiceOuterClass.GlobalStatResponse globalStatResponse)
            {
                Message globalStatMessage = new StatMessage("globalStatFromCoordinator", globalStatResponse.getTimestamp(),
                        globalStatResponse.getNodeId(), globalStatResponse.getValue());

                node.getMessagesBuffer().put(globalStatMessage);
            }

            @Override
            //if there are some errors, this method will be called
            public void onError(Throwable throwable)
            {
//                System.out.println("Error! " + throwable.getMessage());
                startNewElection();
            }

            @Override
            //when the stream is completed (the server called "onCompleted") just close the channel
            public void onCompleted()
            {
                channel.shutdownNow();
            }
        });

    }

    private void streamToCoordinator(Stat localStat){

        StreamObserver<NodeServiceOuterClass.LocalStatRequest> requestObserver = coordinatorStub.streamToCoordinator(new StreamObserver<NodeServiceOuterClass.GlobalStatResponse>() {
            @Override
            //this hanlder takes care of each item received in the stream
            public void onNext(NodeServiceOuterClass.GlobalStatResponse response) {

                Message globalStatMessage = new StatMessage("globalStatFromCoordinator", response.getTimestamp(),
                        response.getNodeId(), response.getValue());

                node.getMessagesBuffer().put(globalStatMessage);

//                // Ricezione della risposta del server (global stat)
//                // Se sono presenti statistiche globali (quindi ne è stata inviata una) allora salvala
//                if (response != null)
//                {
//                    Stat globalStat = new Stat(response.getNodeId(), response.getValue(), response.getTimestamp());
//
////                    System.out.println("Global stat ricevuta: " + globalStat);
//
//                    node.addGlobalStat(globalStat);
//                }
            }

            @Override
            //if there are some errors, this method will be called
            public void onError(Throwable throwable)
            {
                startNewElection();
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
                    setNodeId(localStat.getNodeId()).
                    setValue(localStat.getMean()).
                    setTimestamp(localStat.getTimestamp()).
                    build());
//            requestObserver.onError(new Exception("Connessione con il coordinatore interrotta!"));
        } catch (Exception e)
        {
            System.out.println("Errore nell'invio della statistica local al coordinatore");
        }

    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public Object getElectionLock() {
        return electionLock;
    }

    public void setElectionLock(Object electionLock) {
        this.electionLock = electionLock;
    }

    public ManagedChannel getCoordinatorChannel() {
        return coordinatorChannel;
    }

    public void setCoordinatorChannel(ManagedChannel coordinatorChannel) {
        this.coordinatorChannel = coordinatorChannel;
    }

    public NodeServiceGrpc.NodeServiceStub getCoordinatorStub() {
        return coordinatorStub;
    }

    public void setCoordinatorStub(NodeServiceGrpc.NodeServiceStub coordinatorStub) {
        this.coordinatorStub = coordinatorStub;
    }

    public SensorServiceImpl getSensorsService() {
        return sensorsService;
    }

    public void setSensorsService(SensorServiceImpl sensorsService) {
        this.sensorsService = sensorsService;
    }

    public Server getServerToSensors() {
        return serverToSensors;
    }

    public void setServerToSensors(Server serverToSensors) {
        this.serverToSensors = serverToSensors;
    }

    public void startNewElection()
    {
        synchronized (node.getNodeServerToNodes().sendingLock) {
            if (node.getState() != beans.State.ELECTING_COORDINATOR && !electionRequestDone && previousElectionNumber == currentElectionNumber) {
                electionRequestDone = true;

//                        System.out.println(previousElectionNumber + " vs " + currentElectionNumber);

                System.out.println("Starting new election");

                // Start a new election
                node.setState(beans.State.ELECTING_COORDINATOR);

                node.sending = true;

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("Errore nello sleep");
                }

                // Rimuove il vecchio coordinatore dalla lista
                for (Node n : node.getNodesListCopy()) {
                    if (n.getState() == beans.State.COORDINATOR) {
                        node.removeNodeFromNodesList(n);
                        node.updateNextNodes(node);
                    }
                }

                Node nextNode = null;

                ArrayList<Node> nextNodesCopy = node.getNextNodesCopy();
                if (nextNodesCopy.size() > 0) {
                    nextNode = nextNodesCopy.get(0);
                    node.connectToNextNode(nextNode);
                } else {
                    nextNode = null;
                }

//                        if (nextNode != null && nextNode.getState() == beans.State.COORDINATOR && nextNodesCopy.size() > 1) {
//                            nextNode = nextNodesCopy.get(1);
//
//                            //                        node.getNextNodes().remove(nextNode);
//                            //                        node.getNodesList().remove(nextNode);
//                        }
//                        else if (nextNode != null && nextNode.getState() == beans.State.COORDINATOR && nextNodesCopy.size() <= 1)
//                            nextNode = null;

                if (nextNode != null) {
//                            System.out.println(nextNode + " " + nextNode.getState());
                    boolean sent = false;
                    while (!sent) {
                        node.sendTries = 3;

                        while (node.sendTries > 0) {
                            try {
                                int idToSend = node.getId();

//                                        // Se ha già inviato un messaggio maggiore o uguale (non dovrebbe succedere in teoria)
//                                        if (node.lastMessageSent != null && node.lastMessageSent.getValue() >= idToSend)
//                                        {
//                                            System.out.println("Già inviato");
//                                            sent = true;
//                                            break;
//                                        }

                                node.sendElectionMessageToNextNode(node, nextNode, "ELECTING", idToSend);
//                                            node.getNodeServerToNodes().sendingLock.wait();

                                if (node.resend) {
                                    node.resend = false;
                                    System.out.println("ECCEZIONE");
                                    throw new Exception();
                                }

                                sent = true;
                                System.out.println("Messaggio inviato al nodo " + nextNode);

                                node.sending = false;

                                break;
                            } catch (Exception e) {
                                System.out.println("Nodo " + nextNode + " non disponibile. Riprovo.");

                                node.sendTries--;
                            }
                        }

                        if (!sent) {
                            // Rimuove il nodo non disponibile dai nodi e ricalcola i nodi vicini
                            if (node.getNodeFromNodesList(nextNode.getId()) != null) {
                                node.removeNodeFromNodesList(nextNode);
                                node.updateNextNodes(node);
                            }

                            nextNodesCopy = node.getNextNodesCopy();

                            if (nextNodesCopy.size() > 0) {
                                nextNode = nextNodesCopy.get(0);
                                node.connectToNextNode(nextNode);
                                System.out.println("Provo con il prossimo nodo: " + nextNode);
                            } else {
                                System.out.println("NON CI SONO ALTRI NODI " + node.getNodesListCopy());
                                // Non ci sono altri nodi, quindi diventa lui il coordinatore
                                nextNode = null;
                                break;
                            }
                        }
                    }
                } else if (nextNode == null) {
                    // Elezione conclusa
                    System.out.println("ELEZIONE CONCLUSA");

                    // Si imposta coordinatore
                    node.setState(beans.State.COORDINATOR);

                    node.setCoordinatorPort(node.getNodesPort());

                    synchronized (node.getNodeServerToNodes().getCoordService().getElectingCoordinator()) {
                        node.getNodeServerToNodes().getCoordService().getElectingCoordinator().notifyAll();
                    }

                    // Apre la connessione con il Server Cloud
                    System.out.println(node.getServerAddress());
                    node.setNodeClient(NodeClient.getNodeClientInstance(node, node.getServerAddress()));
                    node.getNodeClient().start();

//                            // Chiude la connessione con gli altri da non coordinatore
//                            node.getNodeServerToNodes().getServerToNodes().shutdownNow();
//
//                            // Apre la connessione con gli altri da coordinatore
//                            NodeServiceImpl nodeService = new NodeServiceImpl(node);
//                            CoordServiceImpl coordService = new CoordServiceImpl(node);
//                            ElectionServiceImpl electionService = new ElectionServiceImpl(node);
//
//                            Server coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
//                                    addService(nodeService).
//                                    addService(coordService).
//                                    addService(electionService).
//                                    build();
//                            node.getNodeServerToNodes().setServerToNodes(coordinatorServer);
//                            try {
//                                node.getNodeServerToNodes().getServerToNodes().start();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }

//                            coordService = new CoordServiceImpl(node);
//                            ElectionServiceImpl electionService = new ElectionServiceImpl(node);
//
//                            Server coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
//                                    addService(nodeService).
//                                    addService(coordService).
//                                    addService(electionService).
//                                    build();
//                            node.getNodeServer().setCoordinatorServer(coordinatorServer);
//                            try {
//                                node.getNodeServer().getCoordinatorServer().start();
//                            } catch (IOException e) {
//                                System.out.println("Errore nel lanciare il server coordinatore");
//                            }

                    System.out.println("Coordinator server started, node " + node.getId() + "!");

                    Node exCoord = null;
                    ArrayList<Node> nodesListCopy = node.getNodesListCopy();
                    for (Node n : nodesListCopy)
                        if (n.getState() == beans.State.COORDINATOR)
                            exCoord = n;

                    node.removeNodeFromNodesList(exCoord);
                    node.removeNodeFromNextNodes(exCoord);

                    System.out.println(node.getNextNodesCopy());
                    System.out.println(node.getNodesListCopy());

                    currentElectionNumber++;
                }
            }
        }
    }
}
