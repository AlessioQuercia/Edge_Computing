package beans;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class NodeClient extends Thread
{
    private Node node;

    private Client client;

    private URL url;

    private HttpURLConnection conn;

    private String serverAddress;

    private boolean stop;

    private long midnight;

    private long lastTime;

    private static NodeClient nodeClient;

    public boolean[] sentOne;

    public static NodeClient getNodeClientInstance(Node node, String serverAddress)
    {
        if (nodeClient == null)
            nodeClient = new NodeClient(node, serverAddress);
        return nodeClient;
    }

    public static void resetNodeClientInstance()
    {
        nodeClient = null;
    }

    private NodeClient(Node node, String serverAddress)
    {
        this.node = node;
        this.serverAddress = serverAddress;
        this.stop = false;
        this.midnight = computeMidnightMilliseconds();
        this.lastTime = System.currentTimeMillis();
        this.sentOne = new boolean[101];
    }

    @Override
    public void run()
    {
        // Il nodo coordinatore si connette al Server Cloud per comunicargli le statistiche globali e locali

        client = Client.create();
        try {
            url = new URL(serverAddress);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            System.out.println("Coordinator (node " + node.getId() + ") connected to the Server Cloud!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        WebResource resource;
        ClientResponse response = null;
        String nodesServices = serverAddress + "/nodesServices";

        while (!stop)
        {
            Set<Stat> localStats = node.getLocalStatsCopy();
            Stat[] statsCopy = localStats.toArray(new Stat[localStats.size()]);

            if (statsCopy.length > 0)
            {
                // Controlla se qualche nodo è uscito dalla rete e in tal caso rimuovilo e avvisa gli altri
                if (node.getNodesListCopy().size() > 1 && node.getGlobalStatsCopy().size() > 0)
                    checkNodesList();

                // Compute global stat

                double sum = 0;

                for (int i = 0; i < statsCopy.length; i++)
                    sum += statsCopy[i].getMean();

                double mean = sum / statsCopy.length;

                Stat globalStat = new Stat(node.getId(), mean, node.deltaTime());

                // Store global stat
                node.addGlobalStat(globalStat);

                // Send global stat
                String method = "/sendGlobalStat";
                //            String params = "/" + globalStat;

                Gson gson = new Gson();
                String input = gson.toJson(globalStat);

                resource = client.resource(nodesServices + method);
                response = resource.type(MediaType.APPLICATION_JSON_TYPE)
                        .post(ClientResponse.class, input);

//                System.out.println(response);

                // Send local stats
                method = "/sendLocalStats";
                input = gson.toJson(node.getLocalStatsCopy());
                //            params = "/" + node.getStats();

                resource = client.resource(nodesServices + method);
                response = resource.type(MediaType.APPLICATION_JSON_TYPE)
                        .post(ClientResponse.class, input);

//                System.out.println(response);

            }



//            // Print node Panel
//            System.out.println("\n");
//            System.out.println("Last local stats:");
//
//            localStats = node.getLocalStats();
//            Stat[] statArray = localStats.toArray(new Stat[localStats.size()]);
//            int loopDim = Math.min(statArray.length, 3);
//            for (int i = 0; i < loopDim; i++)
//                System.out.println("Node: " + node.getId() + "  " + statArray[statArray.length - 1 - i].getMean() + "  " + statArray[statArray.length - 1 - i].getTimestamp());
//
//            System.out.println();
//            System.out.print("Type \'q\' to remove the node from the Server Cloud: ");

            // Ogni 5 secondi calcola la statistica globale a partire dalle statistiche locali ricevute e la invia
            // al Server Cloud
            try {
                // Dormi per 5 secondi
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("SONO FUORI");
    }

    private long computeMidnightMilliseconds()
    {
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

    public void setStop()
    {
        stop = true;
    }

    public Client getClient()
    {
        return client;
    }

    public HttpURLConnection getConn()
    {
        return conn;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setConn(HttpURLConnection conn) {
        this.conn = conn;
    }

    private void checkNodesList()
    {
        Set<Stat> localStatsCopy = node.getLocalStatsCopy();
        Set<Stat> globalStatsCopy = node.getGlobalStatsCopy();

        Stat[] localStatsCopyArray = localStatsCopy.toArray(new Stat[localStatsCopy.size()]);
        Stat[] globalStatsCopyArray = globalStatsCopy.toArray(new Stat[globalStatsCopy.size()]);

        ArrayList<Stat> lastStatForEachNode = new ArrayList<>();

        ArrayList<Integer> addedNode = new ArrayList<>();

        ArrayList<Node> nodesListCopy = node.getNodesListCopy();

        for (int i = localStatsCopyArray.length -1; i >= 0; i--)
        {
            Stat stat = localStatsCopyArray[i];

            if (!addedNode.contains(stat.getNodeId()))
            {
                addedNode.add(stat.getNodeId());
                lastStatForEachNode.add(stat);
            }
            if (addedNode.size() == nodesListCopy.size())
                break;

        }

        for (Stat stat : lastStatForEachNode)
        {
            if (stat.getTimestamp() < globalStatsCopyArray[globalStatsCopyArray.length-1].getTimestamp() && sentOne[stat.getNodeId()])
            {
                Node n = node.getNodeFromNodesList(stat.getNodeId());
                node.removeNodeFromNodesList(n);
                node.updateNextNodes(node);
                sentOne[n.getId()] = false;
                node.adviceNodes(node, n, "RIMOSSO");
                System.out.println("RIMOSSO NODO " + n);
            }
        }

//        for (Node n : nodesListCopy)
//        {
//            if (!addedNode.contains(n.getId()) && sentOne[n.getId()])
//            {
//                node.removeNodeFromNodesList(n);
//                node.updateNextNodes(node);
//                node.adviceNodes(node, n, "RIMOSSO");
//                System.out.println("RIMOSSO NODO " + n);
//            }
//        }

    }
}
