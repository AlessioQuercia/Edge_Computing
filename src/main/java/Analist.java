import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Analist
{
    public static void main(String[] args)
    {
        Client c = Client.create();

        String serverAddress = null;

        /* Inizializza l’input stream (da tastiera) */
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        URL url = null;
        HttpURLConnection conn = null;

        boolean validAddress = false;

        while (!validAddress)
        {
            try
            {
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

        int selectedService = 0;
        WebResource resource;
        boolean end = false;

        String analistsServices = serverAddress + "/analistsServices";
        String method = "";

        while (!end)
        {
            showAvailableActions("AVAILABLE_SERVICES");

            selectedService = selectNextAction(9);

            String id = "";
            String n = "";

                switch (selectedService)
                {
                    case 1: {
                        method = "/getList";

                        break;
                    }
                    case 2: {
                        method = "/getNodeStats";

                        id = "/" + insertID();
                        n = "/" + insertNumberOfStats();

                        break;
                    }
                    case 3: {
                        method = "/getGlobalStats";

                        n = "/" + insertNumberOfStats();

                        break;
                    }
                    case 4: {
                        method = "/getLocalStats";

                        n = "/" + insertNumberOfStats();

                        break;
                    }
                    case 5: {
                        method = "/getNodeStatsStdDev";

                        id = "/" + insertID();
                        n = "/" + insertNumberOfStats();

                        break;
                    }
                    case 6: {
                        method = "/getNodeStatsMean";

                        id = "/" + insertID();
                        n = "/" + insertNumberOfStats();

                        break;
                    }
                    case 7: {
                        method = "/getGlobalStatsStdDev";

                        n = "/" + insertNumberOfStats();

                        break;
                    }
                    case 8: {
                        method = "/getGlobalStatsMean";

                        n = "/" + insertNumberOfStats();

                        break;
                    }
                    case 9: {
                        conn.disconnect();
                        end = true;
                        break;
                    }
                }

            if (!end) {
                String params = id + n;

                resource = c.resource(analistsServices + method + params);
                ClientResponse response = null;
                response = resource.get(ClientResponse.class);
                if (response.getStatus() == ClientResponse.Status.OK.getStatusCode())
                    System.out.println(response.getEntity(String.class));
                else
                    System.out.println(response);

                showAvailableActions("NEXT_ACTION");

                boolean nextActionSelected = false;

                while (!nextActionSelected)
                {
                    try {
                        selectedService = Integer.parseInt(inFromUser.readLine().trim());
                        if (selectedService > 9 || selectedService <= 0)
                            throw new IOException();

                        switch (selectedService) {
                            case 1: {
                                nextActionSelected = true;
                                break;
                            }
                            case 2: {
                                nextActionSelected = true;
                                conn.disconnect();
                                end = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        //                e.printStackTrace();
                        System.out.println("Not a valid service. Please select a valid service.");
                    }
                }

                id = "";
                n = "";
            }
        }
    }


    public static int insertNumberOfStats() {

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        int n = -1;
        boolean validN = false;

        while (!validN)
        {
            System.out.print("Insert the number of stats to consider: ");
            try {
                n = Integer.parseInt(inFromUser.readLine().trim());
                if (n > 0)
                    validN = true;
                else
                    throw new Exception();
            } catch (Exception e) {
                System.out.println("Not valid number of stats. The number of stats must be a positive integer.");
            }
        }
        
        return n;
    }

    public static int insertID()
    {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        int id = -1;
        boolean validID = false;

        while (!validID)
        {
            System.out.print("Insert the node ID: ");
            try {
                id = Integer.parseInt(inFromUser.readLine().trim());
                if (id >= 0)
                    validID = true;
                else
                    throw new Exception();
            } catch (Exception e) {
//                                    e.printStackTrace();
                System.out.println("Not valid ID. The ID must be a positive integer.");
            }
        }

        return id;
    }

    public static int selectNextAction(int actionNumber) {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        int selectedAction = -1;
        boolean validAction = false;

        while (!validAction) {
            try {
                selectedAction = Integer.parseInt(inFromUser.readLine().trim());
                if (selectedAction > actionNumber || selectedAction <= 0)
                    throw new IOException();
                else
                    validAction = true;
            } catch (Exception e) {
                //                e.printStackTrace();
                System.out.println("Not a valid service. Please select a valid service.");
            }
        }

        return selectedAction;
    }

    public static void showAvailableActions(String type)
    {
        if (type == "AVAILABLE_SERVICES") {
            System.out.println("Available services:");
            System.out.println("    1 - Get city status");
            System.out.println("    2 - Get stats from a node");
            System.out.println("    3 - Get global stats");
            System.out.println("    4 - Get local stats");
            System.out.println("    5 - Get standard deviation from a node's stats");
            System.out.println("    6 - Get mean from a node's stats");
            System.out.println("    7 - Get standard deviation from global stats");
            System.out.println("    8 - Get mean from global stats");
            System.out.println("    9 - Exit");
            System.out.print("Type the service's number to select it: ");
        }
        else if (type == "NEXT_ACTION")
        {
            System.out.println("Select next action:");
            System.out.println("    1 - Show available services");
            System.out.println("    2 - Exit");
            System.out.print("Type the service's number to select it: ");
        }
        else
            System.out.println("Nothing to show!");
    }

}
