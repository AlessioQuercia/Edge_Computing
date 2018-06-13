package beans;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

/* Node representing an Edge node */
@XmlRootElement(name = "node")
public class Node
{
    private int id;
    private String ipAddress;
    private int sensorsPort;
    private int nodesPort;
    private int x;
    private int y;

    private String serverAddress;

    private Set<Stat> globalStats;

    private Set<Stat> localStats;

    private State state;

    private static final int MAX_NODES = 101;

    // Prossimi nodi nella struttura ad anello
    private ArrayList<Node> nextNodes;

    private ArrayList<Node> nodesList;

//    private NodeServer nodeServer;

    private NodeClient nodeClient;

    // Porta del coordinatore
    private int coordinatorPort;
//    private SensorServiceImpl sensorsService;
//    private Server sensorsServer;

    private MessagesBuffer messagesBuffer;

    private NodeServerToSensors nodeServerToSensors;
    private NodeServerToNodes nodeServerToNodes;

    private long midnight;

    public ArrayList<Node> getRemovedNodes() {
        return removedNodes;
    }

    private ArrayList<Node> removedNodes;

    Object waitForCoordinator = new Object();

    public Node () {};

    public Node(int id, String ipAddress, int sensorsPort, int nodesPort, int x, int y)
    {
        this.id = id;
        this.ipAddress = ipAddress;
        this.sensorsPort = sensorsPort;
        this.nodesPort = nodesPort;
        this.x = x;
        this.y = y;
        this.globalStats = new TreeSet<Stat>();
        this.localStats = new TreeSet<Stat>();
        this.state = State.NOT_COORDINATOR;
        this.nextNodes = new ArrayList<Node>();
        this.nodesList = new ArrayList<Node>();
//        this.nodeServer = null;
        this.nodeClient = null;
        this.messagesBuffer = new MessagesBuffer();
        this.midnight = computeMidnightMilliseconds();
        this.removedNodes = new ArrayList<Node>();
        this.coordinatorPort = -1;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public int getSensorsPort()
    {
        return sensorsPort;
    }

    public void setSensorsPort(int sensorsPort)
    {
        this.sensorsPort = sensorsPort;
    }

    public int getNodesPort()
    {
        return nodesPort;
    }

    public void setNodesPort(int nodesPort)
    {
        this.nodesPort = nodesPort;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public String getServerAddress() {return serverAddress; }

    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }

    public Set<Stat> getGlobalStats()
    {
        return globalStats;
    }

    public Set<Stat> getGlobalStatsCopy()
    {
        Set<Stat> copy;
        synchronized (globalStats)
        {
            copy = new TreeSet<Stat>(globalStats);
        }
        return copy;
    }

    public void addGlobalStat(Stat globalStat)
    {
        synchronized (globalStats) {
            globalStats.add(globalStat);
        }
    }

    public Set<Stat> getLocalStats()
    {
        return localStats;
    }

    public Set<Stat> getLocalStatsCopy()
    {
        Set<Stat> copy;
        synchronized (localStats)
        {
            copy = new TreeSet<Stat>(localStats);
        }
        return copy;
    }

    public void addLocalStat(Stat localStat)
    {
        synchronized (localStats) {
            localStats.add(localStat);
        }
    }

    public int getCoordinatorPort()
    {
        return coordinatorPort;
    }

    public void setCoordinatorPort(int coordinatorPort)
    {
        this.coordinatorPort = coordinatorPort;
    }

    public ArrayList<Node> getNodesList()
    {
        return nodesList;
    }

    public ArrayList<Node> getNextNodes()
    {
        return nextNodes;
    }

    public void addNodeToNodesList(Node node)
    {
        synchronized (nodesList)
        {
            if (!nodesList.contains(node))
                nodesList.add(node);
        }
    }

    public void addNodeToNextNodes(Node node)
    {
        synchronized (nextNodes)
        {
            nextNodes.add(node);
        }
    }

    public void clearNodesList()
    {
        synchronized (nodesList)
        {
            nodesList.clear();
        }
    }

    public void clearNextNodes()
    {
        synchronized (nextNodes)
        {
            nextNodes.clear();
        }
    }

    public void removeNodeFromNodesList(Node node)
    {
        synchronized (nodesList)
        {
            nodesList.remove(node);
        }
    }

    public void removeNodeFromNextNodes(Node node)
    {
        synchronized (nextNodes)
        {
            nextNodes.remove(node);
        }
    }

    public ArrayList<Node> getNodesListCopy()
    {
        ArrayList<Node> copy;
        synchronized (nodesList)
        {
            copy = new ArrayList<Node>(nodesList);
        }
        return copy;
    }

    public ArrayList<Node> getNextNodesCopy()
    {
        ArrayList<Node> copy;
        synchronized (nextNodes)
        {
            copy = new ArrayList<Node>(nextNodes);
        }
        return copy;
    }

    public NodeClient getNodeClient() {
        return nodeClient;
    }

    public void setNodeClient(NodeClient nodeClient) {
        this.nodeClient = nodeClient;
    }

    public Node getNodeFromNodesList(int nodeId)
    {
        Node node = null;
        ArrayList<Node> nodesListCopy = getNodesListCopy();
        for (Node n : nodesListCopy)
        {
            if (n.getId() == nodeId)
            {
                node = n;
                break;
            }
        }
        return node;
    }

    public void removeNodeFromNodesList(int nodeId)
    {
        Node node = getNodeFromNodesList(nodeId);

        synchronized (nodesList)
        {
            nodesList.remove(node);
        }
    }

    public void removeNodeFromNextNodes(int nodeId)
    {
        Node node = getNodeFromNodesList(nodeId);

        synchronized (nextNodes)
        {
            nextNodes.remove(node);
        }
    }

    public MessagesBuffer getMessagesBuffer() {
        return messagesBuffer;
    }

    public NodeServerToSensors getNodeServerToSensors() {
        return nodeServerToSensors;
    }

    public void setNodeServerToSensors(NodeServerToSensors nodeServerToSensors) {
        this.nodeServerToSensors = nodeServerToSensors;
    }

    public NodeServerToNodes getNodeServerToNodes() {
        return nodeServerToNodes;
    }

    public void setNodeServerToNodes(NodeServerToNodes nodeServerToNodes) {
        this.nodeServerToNodes = nodeServerToNodes;
    }

    public long computeMidnightMilliseconds(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public long deltaTime(){
        return System.currentTimeMillis()-midnight;
    }

    public static void main(String[] args)
    {
        int id;
        String ipAddress = null;
        int sensorsPort;
        int nodesPort;
        int x;
        int y;
        String serverAddress = null;
        ArrayList<Node> nodeList = null;
        Node thisNode = null;
        State state = State.NOT_COORDINATOR;

        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        Client c = Client.create(config);
        URL url = null;
        HttpURLConnection conn = null;

        /* Inizializza l’input stream (da tastiera) */
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        /* Legge gli argomenti interi da tastiera */
        id = readArgInt("ID");
        nodesPort = readArgInt("NODES_PORT");
        sensorsPort = readArgInt("SENSORS_PORT");

        boolean validAddress = false;

        /* Legge il server address da tastiera */
        while(!validAddress)
        {
            try {
                System.out.print("Inserire l'indirizzo del server cloud: ");
                serverAddress = inFromUser.readLine();
                url = new URL(serverAddress);
                conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                validAddress = true;

            } catch (Exception e) {
                System.out.println("Indirizzo inserito non valido.");
//                e.printStackTrace();
            }
        }

        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Host sconosciuto");
//            e.printStackTrace();
        }

        Random random = new Random();
        boolean validPosition = false;

        /* Parametri accettati, connessione con il server cloud stabilita */
        /* Tentativi di generazione posizione per il nodo corrente */

        int tries = 0;
        WebResource resource;
        ClientResponse response = null;
        while (!validPosition)
        {
            if (tries >= 10) break;
            x = random.nextInt(100);
            y = random.nextInt(100);

            try
            {
                String nodesServices = serverAddress + "/nodesServices";
                String method = "/add";
                String params = "/" + id + "/" + ipAddress + "/" + sensorsPort + "/" + nodesPort + "/" + x + "/" + y;

                resource = c.resource(nodesServices + method + params);
                response = resource.post(ClientResponse.class);
                if (response.getStatus() == ClientResponse.Status.OK.getStatusCode())
                {
                    nodeList = (ArrayList) response.getEntity(new GenericType<List<Node>>() {});
//                    System.out.println(response.getEntity(String.class));

                    for (int i = 0; i<nodeList.size(); i++)
                    {
                        if (nodeList.get(i).getId() == id)
                        {
                            thisNode = nodeList.get(i);
                            thisNode.setNodesList(thisNode, nodeList);
                        }
                    }

                    validPosition = true;
                    break;
                }

            }
            catch (Exception e)
            {
                System.out.println("Errore nella chiamata di inserimento del nodo al Server Cloud");
//                e.printStackTrace();
            }

            tries++;
        }

        conn.disconnect();
        c.destroy();

        if (validPosition)
        {
//            thisNode.nodesList = nodeList;
            thisNode.setServerAddress(serverAddress);

            if (nodeList.size() == 1)
            {
                //Il nodo è l'unico nella rete, perciò diventa il coordinatore.
                state = State.COORDINATOR;
                thisNode.setState(State.COORDINATOR);
                thisNode.setCoordinatorPort(thisNode.nodesPort);

            }
            else
            {
                //Aggiorna la lista dei prossimi nodi nella struttura
                thisNode.updateNextNodes(thisNode);
            }

            System.out.println("NODESLIST: " + thisNode.getNodesListCopy());
            System.out.println("NEXTNODES: " + thisNode.getNextNodes());

            // In tutti i casi (coordinator o not_coordinator) starta i server per i sensori e per i nodi

            thisNode.setNodeServerToSensors(new NodeServerToSensors(thisNode));
            thisNode.getNodeServerToSensors().start();

            thisNode.setNodeServerToNodes(new NodeServerToNodes(thisNode));
            thisNode.getNodeServerToNodes().start();

//            try {
//                thisNode.sensorsServer.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            System.out.println("Server started for node " + thisNode.getId() + "!");

            switch (state)
            {
                case COORDINATOR:
                {
                    System.out.println("I am the Coordinator!");

                    System.out.println(thisNode.getState());

                    thisNode.setNodeClient(new NodeClient(thisNode, serverAddress));
                    thisNode.nodeClient.start();

                    break;
                }
                case NOT_COORDINATOR:
                {
                    // Search for the coordinator
                    // Chiede al nodo successivo nell'anello ((i+1)modN) se lui è il coordinatore
                    Node nextNode = thisNode.getNextNodesCopy().get(0);

                    while (thisNode.coordinatorPort == -1)
                    {
                        System.out.println("ASKING FOR COORDINATOR AT NODE " + nextNode);
                        // Apre un canale con il nodo successivo e gli chiede chi è il coordinatore
                        try {
                            thisNode.askForCoordinator(nextNode);
                        }
                        catch (Exception e)
                        {
                            System.out.println("Nodo non disponibile!!");
                            thisNode.removeNodeFromNodesList(nextNode);
                            thisNode.updateNextNodes(thisNode);
                            if (thisNode.getNextNodesCopy().size() > 0) {
                                nextNode = thisNode.getNextNodesCopy().get(0);
                            }
                            else
                            {
                                thisNode.setState(State.COORDINATOR);
                                thisNode.setNodeClient(new NodeClient(thisNode, serverAddress));
                                thisNode.nodeClient.start();
                                break;
                            }

                        }

                        System.out.println("ASKEDFORCOORDINATOR");

                        if (thisNode.getState() == State.COORDINATOR)
                            break;
                    }

                    // Apre un canale con il coordinatore per avvertirlo
                    // che si è unito alla struttura
                    thisNode.hiCoordinator("");

//                    // Avverte il nodo/i due nodi precedenti che si è aggiunto nella struttura e che farà parte dei loro prossimi nodi
//                    thisNode.advicePreviousNodes(thisNode);

                    for (int i = 0; i<nodeList.size(); i++)
                    {
                        if (nodeList.get(i).getNodesPort() == thisNode.getCoordinatorPort())
                            nodeList.get(i).setState(State.COORDINATOR);
                    }

                    System.out.println(thisNode.getNextNodesCopy());

//                    // Dopo aver conosciuto il nodo coordinatore, apre il servizio per eventuali richeste su chi sia il coordinatore
//                    CoordServiceImpl coordService = new CoordServiceImpl(thisNode);
//
//                    ElectionServiceImpl electionService = new ElectionServiceImpl(thisNode);
//
//                    Server server = ServerBuilder.forPort(thisNode.getNodesPort()).
//                            addService(coordService).
//                            addService(electionService).
//                            build();

                    break;
                }
                case WAITING_COORDINATOR:
                {
                    break;
                }
                case ELECTING_COORDINATOR: break;
            }

            // In tutti i casi (coordinator o not_coordinator)
//            thisNode.setNodeServerToNodes(new NodeServerToNodes(thisNode));
//            thisNode.getNodeServerToNodes().start();
//            thisNode.setNodeServer(new NodeServer(thisNode));
//            thisNode.nodeServer.start();

            if (wantToExit())
            {
                String nodesServices = serverAddress + "/nodesServices";
                String method = "/remove";
                String params = "/" + id;
                resource = c.resource(nodesServices + method + params);
                response = resource.delete(ClientResponse.class);
                if (response.getStatus() == ClientResponse.Status.OK.getStatusCode())
                {
                    // Vengono chiuse tutte le connessioni e il nodo esce
                    // CHIUDERE LA CONNESSIONE CON GLI ALTRI NODI
                    synchronized (thisNode.getNodeServerToSensors().getSensorsService().bufferLock)
                    {
                        thisNode.getNodeServerToSensors().setStop();
                        thisNode.getNodeServerToSensors().getSensorsService().insertCounter = 40;
                        thisNode.getNodeServerToSensors().getSensorsService().bufferLock.notifyAll();
                    }

                    thisNode.getNodeServerToSensors().getServerToSensors().shutdownNow();

                    synchronized (thisNode.getMessagesBuffer().getBuffer())
                    {
                        thisNode.getNodeServerToNodes().setStop();
                        thisNode.getMessagesBuffer().put(null);
                        thisNode.getMessagesBuffer().getBuffer().notifyAll();
                        System.out.println("notifiedAll");
                    }
                    thisNode.getNodeServerToNodes().getServerToNodes().shutdownNow();

                    if (thisNode.getState() == State.COORDINATOR)
                    {
                        thisNode.nodeClient.setStop();
                        thisNode.nodeClient.getConn().disconnect();
                        thisNode.nodeClient.getClient().destroy();
                    }

//                    conn.disconnect();
//                    c.destroy();

                }
                else
                {
                    System.out.println(response);
                }

            }
        }
        else
        {
            System.out.println(response);
        }


        System.out.println("EXIT");
        return;
    }

    private void setNodesList(Node thisNode, ArrayList<Node> nodesList)
    {
        thisNode.nodesList = nodesList;
    }

    public static int readArgInt(String type)
    {
        /* Inizializza l’input stream (da tastiera) */
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        int argInt = -1;
        boolean validArg = false;
        while (!validArg)
        {
            try
            {
                switch (type) {
                    case "ID": {
                        System.out.print("Inserire un ID: ");
                        break;
                    }
                    case "NODES_PORT":
                    {
                        System.out.print("Inserire il numero di porta per la comunicazione con i nodi edge: ");
                        break;
                    }
                    case "SENSORS_PORT":
                    {
                        System.out.print("Inserire il numero di porta per la ricezione delle statistiche dai sensori: ");
                        break;
                    }
                }
                argInt = Integer.parseInt(inFromUser.readLine().trim());
                if ((argInt >= 0 && argInt < MAX_NODES && type == "ID")
                        || argInt >= 0 && (type == "NODES_PORT" || type == "SENSORS_PORT") && Integer.toString(argInt).length() == 4)
                    validArg = true;
                else
                {
                    throw new Exception();
                }

            } catch (Exception e) {
                switch (type) {
                    case "ID": {
                        System.out.println("ID inserito non valido. L'ID deve essere un intero compreso tra 0 e 99.");
                        break;
                    }
                    case "NODES_PORT":
                    {
                        System.out.println("Numero di porta inserito non valido. Il numero di porta deve essere un intero positivo di 4 cifre.");
                        break;
                    }
                    case "SENSORS_PORT":
                    {
                        System.out.println("Numero di porta inserito non valido. Il numero di porta deve essere un intero positivo di 4 cifre.");
                        break;
                    }
                }
//                e.printStackTrace();
            }
        }
        return argInt;
    }

    @Override
    public String toString()
    {
        return id+"";
    }

    public static void printPanel()
    {
        System.out.println("STATISTICHE LOCALI");
        System.out.println("STATISTICHE GLOBALI");
    }

    public static boolean wantToExit()
    {

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        String s;
        boolean validString = false;

        while (!validString)
        {
            System.out.print("Type \'q\' to remove the node from the Server Cloud: ");
            try {
                s = inFromUser.readLine().trim();
                if (s.equals("q"))
                    validString = true;
                else
                    throw new Exception();
            } catch (Exception e) {
                System.out.println("Not a valid input.");
            }
        }
        return true;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void updateNextNodes(Node thisNode)
    {
        ArrayList<Node> nodes = thisNode.getNodesListCopy();
        nodes.remove(thisNode);

        synchronized (nextNodes)
        {
            nextNodes = new ArrayList<Node>();
        }

        if (nodes.size() < 2)
        {
            for (int i = 0; i < nodes.size(); i++)
            {
                addNodeToNextNodes(nodes.get(i));
            }
        }
        else
        {
//            Set<Double> orderedDistances = new TreeSet<Double>();
//            for (Node n : nodes)
//            {
//                double dist = Math.abs(getX() - n.getX()) + Math.abs(getY() - n.getY());
//                orderedDistances.add(dist);
//            }
//
//            Double[] orderedList = orderedDistances.toArray(new Double[orderedDistances.size()]);
//            for (Node n : nodes)
//            {
//                double dist = Math.abs(getX() - n.getX()) + Math.abs(getY() - n.getY());
//                if (orderedList[0] == dist)
//                    nextNodes.add(0, n);
//            }
//            for (Node n : nodes)
//            {
//                double dist = Math.abs(getX() - n.getX()) + Math.abs(getY() - n.getY());
//                if (orderedList[1] == dist)
//                    nextNodes.add(1, n);
//            }

            boolean found[] = {false, false};
            int tries = 1;
            int foundIndex = 0;
            while (tries <= 100)
            {
                for (int i = 0; i < nodes.size(); i++)
                {
                    if (nodes.get(i).getId() == (thisNode.getId() + tries) % (MAX_NODES))
                    {
                        addNodeToNextNodes(nodes.get(i));
//                        System.out.println(nextNodes);
                        found[foundIndex] = true;
                        foundIndex++;
                        break;
                    }
                }

                if (found[0] && found[1])
                    break;

                tries++;
            }
        }
    }

    public void askForCoordinator(Node nextNode)
    {
        //plaintext channel on the address (ip/port) which offers the MethodsService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + nextNode.getNodesPort()).usePlaintext(true).build();

        //creating a blocking stub on the channel
        CoordServiceGrpc.CoordServiceBlockingStub stub = CoordServiceGrpc.newBlockingStub(channel);

        //creating the HelloResponse object which will be provided as input to the RPC method
        CoordServiceOuterClass.NodeRequest request = CoordServiceOuterClass.NodeRequest.newBuilder().
                setNodeId(id).
                setIpAddress(ipAddress).
                setNodesPort(nodesPort).
                setSensorsPort(sensorsPort).
                setX(x).
                setY(y).
                setTimestamp(deltaTime()).
                build();

        //calling the method. it returns an instance of HelloResponse
        CoordServiceOuterClass.CoordResponse response = stub.askForCoordinator(request);

        Message coordResponseMessage = new CoordResponseMessage("coordinatorSent", response.getTimestamp(),
                response.getNodeId(), response.getCoordPort());

        getMessagesBuffer().put(coordResponseMessage);

//        int coordPort = response.getCoordPort();

        //closing the channel
        channel.shutdown();

//        return coordPort;
    }

    public void sendElectionMessage(Node nextNode, String requestStatus, int requestValue)
    {
        //plaintext channel on the address (ip/port) which offers the MethodsService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + nextNode.getNodesPort()).usePlaintext(true).build();

        //creating a blocking stub on the channel
        ElectionServiceGrpc.ElectionServiceBlockingStub stub = ElectionServiceGrpc.newBlockingStub(channel);

        //creating the HelloResponse object which will be provided as input to the RPC method
        ElectionServiceOuterClass.ElectionRequest request = ElectionServiceOuterClass.ElectionRequest.newBuilder().
                setStatus(requestStatus).
                setValue(requestValue).
                setTimestamp(deltaTime()).
                build();

        //calling the method. it returns an instance of HelloResponse
        ElectionServiceOuterClass.ElectionResponse response = stub.sendElectionMessage(request);

        Message electionResponseMessage = new ElectionResponseMessage("electionMessageReceived", response.getTimestamp(),
                response.getNodeId(), response.getAck());

        getMessagesBuffer().put(electionResponseMessage);

        //closing the channel
        channel.shutdown();
    }

    public void sendUpdatePreviousNodeMessage(Node nodeToAdvise, Node node, String type)
    {
        //plaintext channel on the address (ip/port) which offers the MethodsService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + nodeToAdvise.getNodesPort()).usePlaintext(true).build();

        //creating a blocking stub on the channel
        CoordServiceGrpc.CoordServiceBlockingStub stub = CoordServiceGrpc.newBlockingStub(channel);

        //creating the HelloResponse object which will be provided as input to the RPC method
        CoordServiceOuterClass.NodeRequest request = CoordServiceOuterClass.NodeRequest.newBuilder().
                setNodeId(node.getId()).
                setIpAddress(node.getIpAddress()).
                setNodesPort(node.getNodesPort()).
                setSensorsPort(node.getSensorsPort()).
                setX(node.getX()).
                setY(node.getY()).
                setType(type).
                setTimestamp(deltaTime()).
                build();

        //calling the method. it returns an instance of HelloResponse
        CoordServiceOuterClass.NodeResponse response = stub.adviceNode(request);

        Message nodeResponseMessage = new NodeResponseMessage("nodeAdviced", response.getTimestamp(),
                response.getNodeId(), response.getAck());

        getMessagesBuffer().put(nodeResponseMessage);

        //closing the channel
        channel.shutdown();
    }

//    public NodeServer getNodeServer() {
//        return nodeServer;
//    }

//    public void setNodeServer(NodeServer nodeServer) {
//        this.nodeServer = nodeServer;
//    }

//    public Server getSensorsServer()
//    {
//        return sensorsServer;
//    }

//    public void setSensorsServer(Server sensorsServer)
//    {
//        this.sensorsServer = sensorsServer;
//    }

//    public SensorServiceImpl getSensorsService()
//    {
//        return sensorsService;
//    }

//    public void setSensorsService(SensorServiceImpl sensorsService)
//    {
//        this.sensorsService = sensorsService;
//    }

//    public void advicePreviousNodes(Node thisNode, Node changedNode, String type)
//    {
//        ArrayList<Node> nodes = thisNode.getNodesListCopy();
//        nodes.remove(changedNode);
////        nodes.remove(thisNode);
//
//        // Se bisogna avvisare che un nodo che un suo nodo successivo è stato rimosso, bisogna prima avvertirlo
//        // di quale sarà il terzo nodo successivo
//
//        System.out.println("NODES: " + nodes);
//
//        if (nodes.size() < 3)
//        {
//            // Ci sono solo due nodi nella struttura, quindi il prossimo e il precedente sono lo stesso nodo
//            Node previousNode = nodes.get(0);
//            if (previousNode.getId() == thisNode.getId())
//                previousNode = nodes.get(1);
//            System.out.println("PREVIOUS NODE: " + previousNode);
//
//            thisNode.sendUpdatePreviousNodeMessage(previousNode, changedNode, type);
//        }
//        else
//        {
//            System.out.println("CERCANDO I PRECEDENTI NELLA LISTA " + thisNode.getNodesListCopy());
//            // Ci sono più di due nodi nella struttura, quindi si calcola i due precedenti e li avvisa che è entrato
//            boolean found[] = {false, false};
//            int triess = MAX_NODES;
//            int foundIndex = 0;
//            while (triess > 0)
//            {
//                for (Node n : nodes)
//                {
//                    if (n.getId() == (triess + changedNode.getId()) % (MAX_NODES))
//                    {
//                        System.out.println("TROVATO PRECEDENTE: " + n);
//                        Node previousNode = n;
//                        found[foundIndex] = true;
//                        foundIndex++;
//
//                        if (n.getId() == thisNode.getId())
//                            break;
//
//                        if (type.equals("RIMOSSO"))
//                        {
//                            int triez = 0;
//                            boolean foundz = false;
//                            while (triez < MAX_NODES - 3)
//                            {
//                                for (Node m : nodes)
//                                {
//                                    if (m.getId() == previousNode.getId() + 3 + triez)
//                                    {
//                                        if (m.getId() == thisNode.getId())
//                                            break;
//
//                                        Node nextNode = m;
//
//                                        System.out.println("NODO " + nextNode + " ASSENTE NELLA LISTA DI " + previousNode + ". AGGIUNTO.");
//
//                                        thisNode.sendUpdatePreviousNodeMessage(previousNode, nextNode, "AGGIUNTO");
//
//                                        foundz = true;
//                                    }
//                                }
//
//                                if (foundz)
//                                    break;
//
//                                triez++;
//                            }
//                        }
//
//                        thisNode.sendUpdatePreviousNodeMessage(previousNode, changedNode, type);
//
//                        break;
//                    }
//                }
//
//                if (found[0] && found[1])
//                    break;
//
//                triess--;
//            }
//        }
//
//
//
////        if (changedNode.getNodesList().size() < 2)
////        {
////            // Ci sono solo due nodi nella struttura, quindi il prossimo e il precedente sono lo stesso nodo
////            Node previousNode = changedNode.getNextNodes().get(0);
////            changedNode.sendUpdatePreviousNodeMessage(previousNode, changedNode, type);
////        }
////        else
////        {
////            System.out.println("CERCANDO I PRECEDENTI NELLA LISTA " + changedNode.getNodesList());
////            // Ci sono più di due nodi nella struttura, quindi si calcola i due precedenti e li avvisa che è entrato
////            boolean found[] = {false, false};
////            int triess = MAX_NODES;
////            int foundIndex = 0;
////            ArrayList<Node> nodes = changedNode.getNodesList();
////            nodes.remove(changedNode);
////            while (triess > 0)
////            {
////                for (Node n : nodes)
////                {
////                    if (n.getId() == (triess + changedNode.getId()) % (MAX_NODES))
////                    {
////                        System.out.println("TROVATO PRECEDENTE: " + n);
////                        found[foundIndex] = true;
////                        foundIndex++;
////                        changedNode.sendUpdatePreviousNodeMessage(n, changedNode, type);
////                        break;
////                    }
////                }
////
////                if (found[0] && found[1])
////                    break;
////
////                triess--;
////            }
////        }
//    }

    public void adviceNodes(Node thisNode, Node changedNode, String type)
    {
        ArrayList<Node> nodes = thisNode.getNodesListCopy();
        nodes.remove(changedNode);
        nodes.remove(thisNode);

        System.out.println("NODES: " + nodes);

        // Avvisa tutti gli altri nodi che un nodo si è aggiunto/rimosso dalla rete

        for (Node n : nodes)
        {
            n.sendUpdatePreviousNodeMessage(n, changedNode, type);
        }

    }

    public void hiCoordinator(String type)
    {
        //plaintext channel on the address (ip/port) which offers the MethodsService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + getCoordinatorPort()).usePlaintext(true).build();

        //creating a blocking stub on the channel
        CoordServiceGrpc.CoordServiceBlockingStub stub = CoordServiceGrpc.newBlockingStub(channel);

        CoordServiceOuterClass.NodeRequest request;

        if (type.equals("NEW_COORDINATOR"))
        {
            request = CoordServiceOuterClass.NodeRequest.newBuilder().
                    setNodeId(id).
                    setIpAddress(ipAddress).
                    setNodesPort(nodesPort).
                    setSensorsPort(sensorsPort).
                    setX(x).
                    setY(y).
                    setTimestamp(deltaTime()).
                    setType(type).
                    build();
        }

        else
        {
            request = CoordServiceOuterClass.NodeRequest.newBuilder().
                    setNodeId(id).
                    setIpAddress(ipAddress).
                    setNodesPort(nodesPort).
                    setSensorsPort(sensorsPort).
                    setX(x).
                    setY(y).
                    setTimestamp(deltaTime()).
                    build();
        }

        //calling the method. it returns an instance of HelloResponse
        CoordServiceOuterClass.NodeResponse response = stub.hiCoordinator(request);

        Message nodeResponseMessage = new NodeResponseMessage("hiNode", response.getTimestamp(),
                response.getNodeId(), response.getAck());

        getMessagesBuffer().put(nodeResponseMessage);

        //closing the channel
        channel.shutdown();
    }

}
