package beans;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class NodeServerToNodes extends Thread
{
    private Node node;

    private long midnight;

    private boolean stop;

    private NodeServiceImpl nodeService;
    private CoordServiceImpl coordService;
    private ElectionServiceImpl electionService;
    private Server serverToNodes;

    Object sendingLock = new Object();

    Object waitForCoordinatorLock = new Object();

    Object waitForAckLock = new Object();

    public NodeServerToNodes(Node node)
    {
        this.node = node;

        this.midnight = computeMidnightMilliseconds();

        this.stop = false;
    }

    @Override
    public void run()
    {
//        switch (node.getState())
//        {
//            case COORDINATOR:
//            {
//                nodeService = new NodeServiceImpl(node);
//                coordService = new CoordServiceImpl(node);
//                electionService = new ElectionServiceImpl(node);
//                serverToNodes = ServerBuilder.forPort(this.node.getNodesPort()).
//                        addService(nodeService).
//                        addService(coordService).
//                        addService(electionService).
//                        build();
//                break;
//            }
//            case NOT_COORDINATOR:
//            {
//                coordService = new CoordServiceImpl(node);
//                electionService = new ElectionServiceImpl(node);
//                serverToNodes = ServerBuilder.forPort(this.node.getNodesPort()).
//                        addService(coordService).
//                        addService(electionService).
//                        build();
//                break;
//            }
//        }

        nodeService = new NodeServiceImpl(node);
        coordService = new CoordServiceImpl(node);
        electionService = new ElectionServiceImpl(node);
        serverToNodes = ServerBuilder.forPort(this.node.getNodesPort()).
                addService(nodeService).
                addService(coordService).
                addService(electionService).
                build();

        try {
            serverToNodes.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        manageNodesMessages();
    }

    private void manageNodesMessages()
    {
        // GESTIONE MESSAGGI
        while(!stop)
        {
            Message message = node.getMessagesBuffer().take();

            if (message == null)
                break;

            switch (message.getHeader())
            {
                case "hiCoordinator":
                {
                    hiCoordinator((NodeRequestMessage)message);
                    break;
                }
                case "hiNode":
                {
                    hiNode((NodeResponseMessage)message);
                    break;
                }
                case "localStatFromNode":
                {
                    manageLocalStatFromNode((StatMessage)message);
                    break;
                }
                case "globalStatFromCoordinator":
                {
                    manageGlobalStatFromCoordinator((StatMessage)message);
                    break;
                }
                case "adviceNode":
                {
                    adviceNode((NodeRequestMessage)message);
                    break;
                }
                case "nodeAdviced":
                {
                    nodeAdviced((NodeResponseMessage)message);
                    break;
                }
                case "askForCoordinator":
                {
                    askForCoordinator((NodeRequestMessage)message);
                    break;
                }
                case "coordinatorSent":
                {
                    coordinatorSent((CoordResponseMessage)message);
                    break;
                }
                case "electionRequestMessage":
                {
                    ElectionRequestMessage electionRequestMessage = ((ElectionRequestMessage)message);

                    synchronized (sendingLock)
                    {
                        if (!node.sending)
                            manageElectionMessage(electionRequestMessage);
                    }

                    break;
                }
                case "electionResponseMessage":
                {
                    manageElectionMessageReceived((ElectionResponseMessage)message);
                    break;
                }
            }
        }

        System.out.println("Server per i Nodi Edge chiuso!");
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

    public NodeServiceImpl getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeServiceImpl nodeService) {
        this.nodeService = nodeService;
    }

    public CoordServiceImpl getCoordService() {
        return coordService;
    }

    public void setCoordService(CoordServiceImpl coordService) {
        this.coordService = coordService;
    }

    public ElectionServiceImpl getElectionService() {
        return electionService;
    }

    public void setElectionService(ElectionServiceImpl electionService) {
        this.electionService = electionService;
    }

    public Server getServerToNodes() {
        return serverToNodes;
    }

    public synchronized void setServerToNodes(Server serverToNodes) {
        this.serverToNodes = serverToNodes;
    }

    private void hiCoordinator(NodeRequestMessage nodeRequestMessage)
    {

        synchronized (waitForCoordinatorLock)
        {
            if (node.getState() == beans.State.ELECTING_COORDINATOR)
            {
                try {
                    waitForCoordinatorLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isStop())
                {
                    return;
                }
            }
        }

        // Salva il nodo che ha fatto la richiesta nella lista dei nodi
        Node requestNode = nodeRequestMessage.getNode();

        node.addNodeToNodesList(requestNode);

        // Aggiorna la lista dei vicini del nodo ricevente
        node.updateNextNodes(node);

//        if (node.getNextNodesCopy().size() > 0)
//            node.connectToNextNode(node.getNextNodesCopy().get(0));

        System.out.println("UPDATED NEXTNODES: " + node.getNextNodesCopy());

        System.out.println("UPDATED NODESLIST: " + node.getNodesListCopy());

        node.getNodeClient().sentOne[requestNode.getId()] = false;

        if (node.getNodesListCopy().size() > 2 && !nodeRequestMessage.getType().equals("NEW_COORDINATOR"))
        {
            // Avverte i precedenti (1 o 2) del nodo richiedente che si è aggiunto un nuovo nodo e che sarà un loro nuovo vicino
//            node.advicePreviousNodes(node, requestNode, "AGGIUNTO");
            node.adviceNodes(node, requestNode, "AGGIUNTO");
        }
    }

    private void hiNode(NodeResponseMessage nodeResponseMessage)
    {
        System.out.println(nodeResponseMessage.getAck());
    }

    private void manageLocalStatFromNode(StatMessage message)
    {
        // Aggiunge la statistica locale alle statistiche locali
        Stat localStat = new Stat(message.getNodeId(), message.getValue(), message.getTimestamp());

        node.addLocalStat(localStat);

        if (!node.getNodeClient().sentOne[localStat.getNodeId()])
            node.getNodeClient().sentOne[localStat.getNodeId()] = true;
    }

    private void manageGlobalStatFromCoordinator(StatMessage message)
    {
        // Ricezione della risposta del server (global stat)
        Stat globalStat = new Stat(message.getNodeId(), message.getValue(), message.getTimestamp());

        node.addGlobalStat(globalStat);
    }

    private void adviceNode(NodeRequestMessage message)
    {
        System.out.println("ADVICENODE");
        if (message.getType().equals("AGGIUNTO")) {
            // Salva il nuovo vicino nella lista dei nodi (così potrà capire che è un suo vicino)
            Node nextNode = message.getNode();

            node.addNodeToNodesList(nextNode);
        }
        else if (message.getType().equals("RIMOSSO"))
        {
            System.out.println("RIMOSSO: " + message.getNode().getId());
            node.removeNodeFromNodesList(message.getNode().getId());
        }

        // Aggiorna la lista dei vicini del nodo ricevente
        node.updateNextNodes(node);

//        if (node.getNextNodesCopy().size() > 0)
//            node.connectToNextNode(node.getNextNodesCopy().get(0));

        System.out.println("UPDATED NEXTNODES: " + node.getNextNodesCopy());
    }

    private void nodeAdviced(NodeResponseMessage message)
    {
        System.out.println(message.getAck());
    }

    private void askForCoordinator(NodeRequestMessage message)
    {
        System.out.println("RequestReceived. Coordinator sent to node " + message.getNode());
    }

    private void coordinatorSent(CoordResponseMessage message)
    {
        node.setCoordinatorPort(message.getCoordPort());
    }

    private void manageElectionMessage(ElectionRequestMessage message)
    {
        System.out.println("RECEIVED_MESSAGE " + message.getValue() + " FROM NODE " + message.getNodeId() + ", STATUS: " + message.getStatus());

//        // Test per nodo che esce durante elezione (dopo aver ricevuto un messaggio, ma prima di inviarne un altro)
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        // Rimuove il vecchio coordinatore dalla lista dei nodi
//        for (Node n : node.getNodesListCopy())
//        {
//            if (n.getState() == beans.State.COORDINATOR)
//            {
//                node.removeNodeFromNodesList(n);
//                node.updateNextNodes(node);
//            }
//        }

        Node nextNode = null;

        if (node.getNextNodesCopy().size() > 0)
        {
            nextNode = node.getNextNodesCopy().get(0);
        }
        else {
            nextNode = null;
        }

        if (nextNode != null && message.getStatus().equals("ELECTING"))
        {
            if (message.getValue() > node.getId())
            {
                if (node.lastMessageSent == null || (node.lastMessageSent != null && message.getValue() > node.lastMessageSent.getValue()))
                {
                    node.setState(beans.State.ELECTING_COORDINATOR);
                    sendElectionMessage(nextNode, "ELECTING", message.getValue());
                }
//                node.streamElectionMessageToNextNode(nextNode, "ELECTING", message.getValue());
            }
            else if (message.getValue() < node.getId() && node.getState() != beans.State.ELECTING_COORDINATOR)
            {
                node.setState(beans.State.ELECTING_COORDINATOR);
                sendElectionMessage(nextNode, "ELECTING", node.getId());
//                node.streamElectionMessageToNextNode(nextNode, "ELECTING", node.getId());
            }
            else if (message.getValue() == node.getId()) // id uguali, allora è il coordinatore
            {
                if (node.lastMessageSent == null ||
                        (node.lastMessageSent != null && !node.lastMessageSent.getStatus().equals("ELECTED")))
                {
                    System.out.println("I am the new Coordinator (Node " + node + ") !");

                    // Imposta la porta del coordinatore come la sua porta per i nodi
                    node.setCoordinatorPort(node.getNodesPort());

                    // Si imposta coordinatore
                    node.setState(beans.State.COORDINATOR);

                    // Apre la connessione con il Server Cloud
                    node.setNodeClient(NodeClient.getNodeClientInstance(node, node.getServerAddress()));
                    try {
                        node.getNodeClient().start();
                    } catch (Exception e) {
                    }

//                    // Chiude la connessione con gli altri da non coordinatore
//                    getServerToNodes().shutdown();
//
//                    // Apre la connessione con gli altri da coordinatore
//                    NodeServiceImpl nodeService = new NodeServiceImpl(node);
//                    CoordServiceImpl coordService = new CoordServiceImpl(node);
//                    ElectionServiceImpl electionService = new ElectionServiceImpl(node);
//
//                    Server coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
//                            addService(nodeService).
//                            addService(coordService).
//                            addService(electionService).
//                            build();
//                    node.getNodeServerToNodes().setServerToNodes(coordinatorServer);
//                    try {
//                        node.getNodeServerToNodes().getServerToNodes().start();
//                    } catch (IOException e) {
//                        System.out.println("Error in starting server");
//                    }

                    System.out.println("Coordinator server started, node " + node.getId() + "!");

                    // Avvisa il prossimo che il coordinatore è stato eletto
                    sendElectionMessage(nextNode, "ELECTED", node.getNodesPort());

                    System.out.println("ELECTED SENT");

                    node.clearNextNodes();
                    node.clearNodesList();

                    node.addNodeToNodesList(node);

                    System.out.println(node.getNextNodesCopy());
                    System.out.println(node.getNodesListCopy());
                }
                else
                {
                    System.out.println("ELECTED ALREADY SENT");
                }
            }
        }
        else if (nextNode != null && message.getStatus().equals("ELECTED")) // && node.getState() == beans.State.ELECTING_COORDINATOR)
        {

                System.out.println("ELECTED RECEIVED");
                // Elimina il vecchio coordinatore dalle liste
                //            Node exCoordinator = null;
                //            ArrayList<Node> nodesListCopy = node.getNodesListCopy();
                //            for (Node n : nodesListCopy)
                //                if (n.getState() == beans.State.COORDINATOR)
                //                    exCoordinator = n;
                //
                //            if (exCoordinator != null)
                //                node.removeNodeFromNodesList(exCoordinator);
                //
                //            node.updateNextNodes(node);
                //
                //            System.out.println(node.getNextNodesCopy());
                //            System.out.println(node.getNodesListCopy());

                // Aggiorna il nuovo coordinatore
                int coordPort = message.getValue();

                synchronized (node.getNodesList()) {
                    for (Node n : node.getNodesList()) {
                        if (n.getNodesPort() == coordPort)
                            n.setState(beans.State.COORDINATOR);
                    }
            }

            if (node.getNodesPort() != coordPort)
            {
                if (node.lastMessageSent == null || (node.lastMessageSent != null && message.getValue() > node.lastMessageSent.getValue())) {
                    System.out.println("Non sono il coordinatore!");
                    node.setCoordinatorPort(coordPort);

                    synchronized (getCoordService().getElectingCoordinator()) {
                        getCoordService().getElectingCoordinator().notifyAll();
                    }

                    sendElectionMessage(nextNode, "ELECTED", coordPort);
                    //                node.streamElectionMessageToNextNode(nextNode, "ELECTED", coordPort);

                    //                try {
                    //                    Thread.sleep(3000);
                    //                } catch (InterruptedException e) {
                    //                    e.printStackTrace();
                    //                }

                    node.hiCoordinator("NEW_COORDINATOR");

                    node.setState(beans.State.NOT_COORDINATOR);
                }
                else
                {
                    System.out.println("ELECTED ALREADY RECEIVED AND SENT");
                }
            }
            else // porte uguali, allora è il coordinatore stesso
            {
                // Elezione conclusa
                System.out.println("ELEZIONE CONCLUSA");

                // Si imposta coordinatore e conclude l'elezione
                node.setState(beans.State.COORDINATOR);

                // Gestisce i messaggi di hiCoordinator
                synchronized (waitForCoordinatorLock)
                {
                    waitForCoordinatorLock.notifyAll();
                }
            }
        }

    }

    private void manageElectionMessageReceived(ElectionResponseMessage message)
    {
        System.out.println(message.getAck());
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop() {
        this.stop = true;
    }

    public void sendElectionMessage(Node nextNd, String requestStatus, int value)
    {
        Node nextNode = nextNd;
        boolean sent = false;
        while (!sent)
        {
            node.sendTries = 3;

            while (node.sendTries > 0)
            {
                try
                {
                    int idToSend = value;

//                    // Se ha già inviato un messaggio maggiore o uguale
//                    if (node.lastMessageSent.getValue() >= idToSend)
//                    {
//                        System.out.println("Già inviato");
//                        sent = true;
//                        break;
//                    }

                    node.sendElectionMessageToNextNode(node, nextNode, requestStatus, idToSend);
//                                            node.getNodeServerToNodes().sendingLock.wait();

                    if (node.resend)
                    {
                        node.resend = false;
                        System.out.println("ECCEZIONE");
                        throw new Exception();
                    }

                    sent = true;
                    System.out.println("Messaggio inviato al nodo " + nextNode);

                    node.sending = false;

                    break;
                }
                catch (Exception e)
                {
                    System.out.println("Nodo " + nextNode + " non disponibile. Riprovo.");

                    node.sendTries--;
                }
            }

            if (!sent)
            {
                //                e.printStackTrace();
                System.out.println("Nodo " + nextNode + " non disponibile.");

                // Rimuove il nodo non disponibile dai nodi e ricalcola i nodi vicini
                if (node.getNodeFromNodesList(nextNode.getId()) != null)
                {
                    node.removeNodeFromNodesList(nextNode);
                    node.updateNextNodes(node);
                }

                ArrayList<Node> nextNodesCopy = node.getNextNodesCopy();

                if (nextNodesCopy.size() > 0)
                {
                    nextNode = nextNodesCopy.get(0);
//                    node.connectToNextNode(nextNode);
                    System.out.println("Provo con il prossimo nodo: " + nextNode);
                }
                else {
                    System.out.println("NON CI SONO ALTRI NODI: " + node.getNodesListCopy());
                    // Non ci sono altri nodi, quindi diventa lui il coordinatore

                    // Elezione conclusa
                    System.out.println("ELEZIONE CONCLUSA");

                    // Si imposta coordinatore
                    node.setState(beans.State.COORDINATOR);

                    node.setCoordinatorPort(node.getNodesPort());

                    synchronized (getCoordService().getElectingCoordinator()) {
                        getCoordService().getElectingCoordinator().notifyAll();
                    }

                    // Apre la connessione con il Server Cloud
                    System.out.println(node.getServerAddress());
                    node.setNodeClient(NodeClient.getNodeClientInstance(node, node.getServerAddress()));
                    node.getNodeClient().start();

                    // Chiude la connessione con gli altri da non coordinatore
                    node.getNodeServerToNodes().getServerToNodes().shutdownNow();

                    // Apre la connessione con gli altri da coordinatore
                    NodeServiceImpl nodeService = new NodeServiceImpl(node);
                    CoordServiceImpl coordService = new CoordServiceImpl(node);
                    ElectionServiceImpl electionService = new ElectionServiceImpl(node);

                    Server coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
                            addService(nodeService).
                            addService(coordService).
                            addService(electionService).
                            build();
                    node.getNodeServerToNodes().setServerToNodes(coordinatorServer);
                    try {
                        node.getNodeServerToNodes().getServerToNodes().start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

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
                }
            }

        }
    }
}