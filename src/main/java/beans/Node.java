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
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.IOException;
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

    private NodeServer nodeServer;

    private NodeClient nodeClient;

    // Porta del coordinatore
    private int coordinatorPort;

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
        this.nodeServer = null;
        this.nodeClient = null;
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

    public Set<Stat> getGlobalStats() {
        return new TreeSet<Stat>(globalStats);
    }

    public synchronized void addGlobalStat(Stat globalStat)
    {
        globalStats.add(globalStat);
    }

    public Set<Stat> getLocalStats() {
        return new TreeSet<Stat>(localStats);
    }

    public synchronized void addLocalStat(Stat localStat)
    {
        localStats.add(localStat);
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
        Set<Stat> globalStats = new TreeSet<Stat>();
        Set<Stat> localStats = new TreeSet<Stat>();

        ArrayList<Node> nodeList = null;
        Node thisNode = null;
        State state = State.NOT_COORDINATOR;

        Object timerLock = new Object();
        long lastTime = System.currentTimeMillis();

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
            e.printStackTrace();
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
                    nodeList = (ArrayList) response.getEntity(new GenericType<List<Node>>() {
                    });
//                    System.out.println(response.getEntity(String.class));

                    for (int i = 0; i<nodeList.size(); i++)
                    {
                        if (nodeList.get(i).getId() == id)
                            thisNode = nodeList.get(i);
                    }

                    validPosition = true;
                    break;
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            tries++;
        }

        conn.disconnect();
        c.destroy();

        if (validPosition)
        {
            thisNode.nodesList = nodeList;
            thisNode.setServerAddress(serverAddress);

            System.out.println(thisNode.getNextNodes());

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
                thisNode.updateNextNodes(thisNode, nodeList);
            }

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
                    Node nextNode = thisNode.getNextNodes().get(0);

                    // Apre un canale con il nodo successivo e gli chiede chi è il coordinatore
                    thisNode.setCoordinatorPort(thisNode.askForCoordinator(nextNode));

                    // Avverte il nodo/i due nodi precedenti che si è aggiunto nella struttura e che farà parte dei loro prossimi nodi
                    if (thisNode.getNodesList().size() < 2)
                    {
                        // Ci sono solo due nodi nella struttura, quindi il prossimo e il precedente sono lo stesso nodo
                        Node previousNode = thisNode.getNextNodes().get(0);
                        thisNode.sendUpdatePreviousNodeMessage(previousNode, thisNode);
                    }
                    else
                    {
                        System.out.println("CERCANDO I PRECEDENTI NELLA LISTA " + thisNode.getNodesList());
                        // Ci sono più di due nodi nella struttura, quindi si calcola i due precedenti e li avvisa che è entrato
                        boolean found[] = {false, false};
                        int triess = MAX_NODES;
                        int foundIndex = 0;
                        ArrayList<Node> nodes = thisNode.getNodesList();
                        nodes.remove(thisNode);
                        while (triess > 0)
                        {
                            for (Node n : nodes)
                            {
                                if (n.getId() == (triess + thisNode.getId()) % (MAX_NODES))
                                {
                                    System.out.println("TROVATO PRECEDENTE: " + n);
                                    found[foundIndex] = true;
                                    foundIndex++;
                                    thisNode.sendUpdatePreviousNodeMessage(n, thisNode);
                                    break;
                                }
                            }

                            if (found[0] && found[1])
                                break;

                            triess--;
                        }
                    }

                    for (int i = 0; i<nodeList.size(); i++)
                    {
                        if (nodeList.get(i).getNodesPort() == thisNode.getCoordinatorPort())
                            nodeList.get(i).setState(State.COORDINATOR);
                    }

                    System.out.println(thisNode.getNextNodes());

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
                case WAITING_COORDINATOR: break;
                case ELECTING_COORDINATOR: break;
            }

            // In tutti i casi (coordinator o not_coordinator)
            thisNode.setNodeServer(new NodeServer(thisNode));
            thisNode.nodeServer.start();

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
                    thisNode.nodeServer.setStop();
                    thisNode.nodeServer.getServer().shutdownNow();
                    if (state == State.COORDINATOR)
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

    public void updateNextNodes(Node thisNode, ArrayList<Node> nodeList)
    {
        ArrayList<Node> nodes = nodeList;
        nodes.remove(thisNode);

        nextNodes = new ArrayList<Node>();

        if (nodes.size() < 2)
        {
            for (int i = 0; i < nodes.size(); i++)
            {
                nextNodes.add(nodes.get(i));
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
                        nextNodes.add(nodes.get(i));
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

    public ArrayList<Node> getNextNodes()
    {
        return nextNodes;
    }

    public int askForCoordinator(Node nextNode)
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
                build();

        //calling the method. it returns an instance of HelloResponse
        CoordServiceOuterClass.CoordResponse response = stub.askForCoordinator(request);

        int coordPort = response.getCoordPort();

        //closing the channel
        channel.shutdown();

        return coordPort;
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

    public void sendElectionMessage(Node nextNode, String requestStatus, int requestValue)
    {
        //plaintext channel on the address (ip/port) which offers the MethodsService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + nextNode.getNodesPort()).usePlaintext(true).build();

        //creating a blocking stub on the channel
        ElectionServiceGrpc.ElectionServiceBlockingStub stub = ElectionServiceGrpc.newBlockingStub(channel);

        //creating the HelloResponse object which will be provided as input to the RPC method
        ElectionServiceOuterClass.ElectionRequest request = ElectionServiceOuterClass.ElectionRequest.newBuilder().
                setStatus(requestStatus).setValue(requestValue).build();

        //calling the method. it returns an instance of HelloResponse
        ElectionServiceOuterClass.ElectionResponse response = stub.sendElectionMessage(request);

        //closing the channel
        channel.shutdown();
    }

    public void sendUpdatePreviousNodeMessage(Node nodeToAdvise, Node node)
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
                build();

        //calling the method. it returns an instance of HelloResponse
        CoordServiceOuterClass.NodeResponse response = stub.adviceNode(request);

        //closing the channel
        channel.shutdown();
    }

    public NodeServer getNodeServer() {
        return nodeServer;
    }

    public void setNodeServer(NodeServer nodeServer) {
        this.nodeServer = nodeServer;
    }

    public NodeClient getNodeClient() {
        return nodeClient;
    }

    public void setNodeClient(NodeClient nodeClient) {
        this.nodeClient = nodeClient;
    }
}
