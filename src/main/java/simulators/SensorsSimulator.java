package simulators;

import com.sun.jersey.api.client.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SensorsSimulator
{
    public static void main(String[] args)
    {
        ArrayList<Sensor> activeSensors = new ArrayList<Sensor> ();

        Client c = Client.create();
        URL url = null;
        HttpURLConnection tryConn = null;

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        int sensorsNumber = insertNumberOfSensors();

        String serverAddress = null;

        boolean validAddress = false;

        /* Legge il server address da tastiera */
        while(!validAddress)
        {
            try {
                System.out.print("Inserire l'indirizzo del server cloud: ");
                serverAddress = inFromUser.readLine();
                url = new URL(serverAddress);
                tryConn = (HttpURLConnection) url.openConnection();
                tryConn.connect();
                validAddress = true;

            } catch (Exception e) {
                System.out.println("Indirizzo inserito non valido.");
//                e.printStackTrace();
            }
        }

        tryConn.disconnect();
        c.destroy();

        for (int i = 0; i < sensorsNumber; i++)
            activeSensors.add(new Sensor(i, serverAddress));

    }

    public static int insertNumberOfSensors() {

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        int n = -1;
        boolean validN = false;

        while (!validN)
        {
            System.out.print("Insert the number of sensors to create: ");
            try {
                n = Integer.parseInt(inFromUser.readLine().trim());
                if (n > 0)
                    validN = true;
                else
                    throw new Exception();
            } catch (Exception e) {
                System.out.println("Not valid number of sensors. The number of sensors must be a positive integer.");
            }
        }

        return n;
    }
}
