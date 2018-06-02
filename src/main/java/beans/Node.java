package beans;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import simulators.Measurement;
import simulators.SensorServiceImpl;

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

                    System.out.println(nodeList.toString());

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
            if (nodeList.size() == 1)
            {
                //Il nodo è l'unico nella rete, perciò diventa il coordinatore.
                state = State.COORDINATOR;

            }
            else
            {
                //Il nodo non è l'unico nella rete, perciò deve presentarsi e chiedere chi è il coordinatore.

            }

            NodeServer nodeServer = new NodeServer(thisNode, state);
            nodeServer.start();

            NodeClient nodeClient = null;

            switch (state)
            {
                case COORDINATOR:
                {
                    System.out.println("I am the Coordinator!");

                    nodeClient = new NodeClient(thisNode, serverAddress);
                    nodeClient.start();

                    break;
                }
                case NOT_COORDINATOR:
                {
                    break;
                }
                case WAITING_COORDINATOR: break;
                case ELECTING_COORDINATOR: break;
            }

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
                    nodeServer.setStop();
                    nodeServer.getServer().shutdownNow();
                    if (state == State.COORDINATOR)
                    {
                        nodeClient.setStop();
                        nodeClient.getConn().disconnect();
                        nodeClient.getClient().destroy();
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
                if (argInt > 0)
                    validArg = true;
                else
                {
                    throw new Exception();
                }

            } catch (Exception e) {
                switch (type) {
                    case "ID": {
                        System.out.println("ID inserito non valido. L'ID deve essere un intero positivo.");
                        break;
                    }
                    case "NODES_PORT":
                    {
                        System.out.println("Numero di porta inserito non valido. Il numero di porta deve essere un intero positivo.");
                        break;
                    }
                    case "SENSORS_PORT":
                    {
                        System.out.println("Numero di porta inserito non valido. Il numero di porta deve essere un intero positivo.");
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
}
