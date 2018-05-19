package beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/* Node representing an Edge node */
@XmlRootElement(name = "node")
public class Node
{
    int id;
    String ipAddress;
    int sensorsPort;
    int nodesPort;
    int x;
    int y;

    String serverAddress;

    Set<Stat> stats;

    public Node() {};

    public Node(int id, String ipAddress, int sensorsPort, int nodesPort, int x, int y)
    {
        this.id = id;
        this.ipAddress = ipAddress;
        this.sensorsPort = sensorsPort;
        this.nodesPort = nodesPort;
        this.x = x;
        this.y = y;
        this.stats = new TreeSet<Stat>();
    }

    public int getID()
    {
        return id;
    }

    public void setID(int id)
    {
        this.id = id;
    }

    public String getIPAddress()
    {
        return ipAddress;
    }

    public void setIPAddress(String ipAddress)
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

    public synchronized Set<Stat> getStats() {
    return new TreeSet<Stat>(stats);
}

    public synchronized void addStat(Stat stat)
    {
        stats.add(stat);
    }

    public synchronized void setStats(Set<Stat> stats) {
        this.stats = stats;
    }

//    public static void main(String[] args)
//    {
//        int id;
//        String ipAddress;
//        int sensorsPort;
//        int nodesPort;
//        int x;
//        int y;
//        String serverAddress;
//        ArrayList<Integer> stats;
//
//        URL url = null;
//        HttpURLConnection conn = null;
//
//
//        /* Inizializza l’input stream (da tastiera) */
//        BufferedReader inFromUser =
//                new BufferedReader(new InputStreamReader(System.in));
//
//        boolean validArgs[] = new boolean[4];
//
//        /* Legge una linea da tastiera, se è un intero accetta, altrimenti chiede di riprovare */
//        while(!validArgs[0] || !validArgs[1] || !validArgs[2] || !validArgs[3])
//        {
//            if (!validArgs[0]) {
//                try {
//                    System.out.print("Inserire un id: ");
//                    id = Integer.parseInt(inFromUser.readLine().trim());
//                    if (id >= 0)
//                        validArgs[0] = true;
//                    else
//                        System.out.println("id inserito non valido. L'id deve essere un intero positivo.");
//                } catch (IOException e) {
//                    System.out.println("id inserito non valido. L'id deve essere un intero positivo.");
//                    e.printStackTrace();
//                }
//            }
//
//            if (!validArgs[1]) {
//                try {
//                    System.out.print("Inserire il numero di porta per la comunicazione con i nodi edge: ");
//                    nodesPort = Integer.parseInt(inFromUser.readLine().trim());
//                    if (nodesPort >= 0)
//                        validArgs[1] = true;
//                    else
//                        System.out.println("id inserito non valido. L'id deve essere un intero positivo.");
//                } catch (IOException e) {
//                    System.out.println("Numero di porta inserito non valido. Il numero di porta deve essere un intero positivo.");
//                    e.printStackTrace();
//                }
//            }
//
//            if (!validArgs[2]) {
//                try {
//                    System.out.print("Inserire il numero di porta per la ricezione delle statistiche dai sensori: ");
//                    sensorsPort = Integer.parseInt(inFromUser.readLine().trim());
//                    if (sensorsPort >= 0)
//                        validArgs[2] = true;
//                    else
//                        System.out.println("Numero di porta inserito non valido. Il numero di porta deve essere un intero positivo.");
//                } catch (IOException e) {
//                    System.out.println("Numero di porta inserito non valido. Il numero di porta deve essere un intero positivo.");
//                    e.printStackTrace();
//                }
//            }
//
//            if (!validArgs[3]) {
//                try {
//                    System.out.print("Inserire l'indirizzo del server cloud: ");
//                    serverAddress = inFromUser.readLine();
//                    url = new URL(serverAddress);
//                    conn = (HttpURLConnection) url.openConnection();
//                    validArgs[3] = true;
//
//                } catch (IOException e) {
//                    System.out.println("Indirizzo inserito non valido.");
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        Random random = new Random();
//        boolean validPosition = false;
//
//        /* Parametri accettati, connessione con il server cloud stabilita */
//        /* Tentativi di generazione posizione per il nodo corrente */
//
//        while (!validPosition)
//        {
//            x = random.nextInt(100);
//            y = random.nextInt(100);
//
//            try {
//                ipAddress = InetAddress.getLocalHost().getHostAddress();
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Accept", "application/json");
//
//                if (conn.getResponseCode() != 200) {
//                    throw new RuntimeException("Failed : HTTP error code : "
//                            + conn.getResponseCode());
//                }
//
//                BufferedReader br = new BufferedReader(new InputStreamReader(
//                        (conn.getInputStream())));
//
//                String output;
//                System.out.println("Output from Server .... \n");
//                while ((output = br.readLine()) != null) {
//                    System.out.println(output);
//                }
//
//                conn.disconnect();
//            } catch (ProtocolException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            validPosition = true;
//        }
//
//
//
//    }
}
