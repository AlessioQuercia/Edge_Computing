package beans;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Set;

public class NodeClient extends Thread
{
    private Node node;

    private Client client;

    private HttpURLConnection conn;

    private String serverAddress;

    private boolean stop;

    private long midnight;

    private long lastTime;

    public NodeClient(Node node, String serverAddress)
    {
        this.node = node;
        this.serverAddress = serverAddress;
        this.stop = false;
        this.midnight = computeMidnightMilliseconds();
        this.lastTime = System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        // Il nodo coordinatore si connette al Server Cloud per comunicargli le statistiche globali e locali

        client = Client.create();
        try {
            URL url = new URL(serverAddress);
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
            Set<Stat> localStats = node.getLocalStats();
            Stat[] statsCopy = localStats.toArray(new Stat[localStats.size()]);

            if (statsCopy.length > 0)
            {
                // Compute global stat

                double sum = 0;

                for (int i = 0; i < statsCopy.length; i++)
                    sum += statsCopy[i].getMean();

                double mean = sum / statsCopy.length;

                Stat globalStat = new Stat(node.getId(), mean, deltaTime());

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

                System.out.println(response);

                // Send local stats
                method = "/sendLocalStats";
                input = gson.toJson(node.getLocalStats());
                //            params = "/" + node.getStats();

                resource = client.resource(nodesServices + method);
                response = resource.type(MediaType.APPLICATION_JSON_TYPE)
                        .post(ClientResponse.class, input);

                System.out.println(response);

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

            try {
                // Dormi per 5 secondi
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("SONO FUORI");
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
}
