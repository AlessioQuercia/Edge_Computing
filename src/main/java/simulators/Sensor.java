package simulators;

import beans.Node;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import javax.ws.rs.core.MediaType;
import java.util.Random;

public class Sensor
{
    private Client c;
    private int id;
    private Stream stream;
    private String serverAddress;
    private int x;
    private int y;

    private Node communicationNode;

    private long lastRequestTime = 0;

    private Simulator simulator;


    public Sensor(int id, String serverAddress)
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        this.c = Client.create(config);
        this.id = id;
        this.serverAddress = serverAddress;

        this.stream = new Stream(this);
        this.simulator = new PM10Simulator(id+"", stream);
        this.simulator.start();

        this.initialize();
    }

    private void initialize()
    {
        /* Tentativi di generazione posizione per il nodo corrente */

        Random random = new Random();

        x = random.nextInt(100);
        y = random.nextInt(100);

        requestNearestNode();

    }

    public void requestNearestNode() {
        try {

            WebResource resource;

            String sensorsServices = serverAddress + "/sensorsServices";
            String method = "/getNearestNode";
            String params = "/" + x + "/" + y;

            resource = c.resource(sensorsServices + method + params);
            ClientResponse response = null;
            response = resource.get(ClientResponse.class);
            if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                //                System.out.println(response.getEntity(String.class));
                try {
                    communicationNode = response.getEntity(Node.class);
                }
                catch (Exception e){
                    System.out.println("Nessun nodo vicino trovato!");
                }

                setCommunicationNode(communicationNode);

                //                //faccio partire la simulazione del sensore
                //                simulator.start();


            } else
                System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCommunicationNode(Node communicationNode)
    {
        this.communicationNode = communicationNode;
    }

    public Node getCommunicationNode()
    {
        return communicationNode;
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(long lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
}
