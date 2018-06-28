package beans;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ElectionServiceImpl extends ElectionServiceGrpc.ElectionServiceImplBase {

    private Node node;

    private HashMap<StreamObserver, Integer> observers = new HashMap<StreamObserver, Integer>();

    public ElectionServiceImpl(Node node)
    {
        this.node = node;
    }

    @Override
    public void sendElectionMessage(ElectionServiceOuterClass.ElectionRequest electionRequest, StreamObserver<ElectionServiceOuterClass.ElectionResponse> responseObserver)
    {
        try {
            Message electionMessage = new ElectionRequestMessage("electionRequestMessage", electionRequest.getTimestamp(),
                    electionRequest.getNodeId(), electionRequest.getStatus(), electionRequest.getValue());

            node.getMessagesBuffer().put(electionMessage);

            synchronized (observers) {
                if (observers.get(responseObserver) == null) {
                    observers.put(responseObserver, electionRequest.getNodeId());
                }
            }

            ElectionServiceOuterClass.ElectionResponse response = ElectionServiceOuterClass.ElectionResponse.newBuilder()
                    .setNodeId(node.getId())
                    .setTimestamp(node.deltaTime())
                    .setAck("Received")
                    .build();

            //passo la risposta nello stream
            responseObserver.onNext(response);

            //completo e finisco la comunicazione
            responseObserver.onCompleted();
        }
        catch (Exception e)
        {
            synchronized (observers) {
                int crashedNodeId = observers.get(responseObserver);

                if (node.getNodeFromNodesList(crashedNodeId) != null && node.getState() == State.ELECTING_COORDINATOR && !node.getNodeServerToNodes().isStop()) {
                    System.out.println("Nodo " + crashedNodeId + " non disponibile");
                    System.out.println("RIMOSSO NODO PRECEDENTE " + crashedNodeId);

                    observers.remove(responseObserver);

                    Node removedNode = node.getNodeFromNodesList(crashedNodeId);

                    //                        synchronized(node.getRemovedNodes())
                    //                        {
                    //                            node.getRemovedNodes().add(removedNode);
                    //                        }

                    System.out.println("Il nodo: " + removedNode + " non è più nella rete, rimosso!");

                    node.removeNodeFromNodesList(crashedNodeId);
                    node.updateNextNodes(node);

                    int value = node.getId();

                    if (node.lastRequestStatus.equals("ELECTED"))
                        value = node.getNodesPort();

                    Node nextNode = null;
                    if (node.getNextNodesCopy().size() > 0)
                        nextNode = node.getNextNodesCopy().get(0);

                    if (nextNode != null) {
//                        node.connectToNextNode(nextNode);

                        System.out.println("Reinvio il mio messaggio al prossimo nodo " + nextNode);

                        node.getNodeServerToNodes().sendElectionMessage(nextNode, node.lastMessageSent.getStatus(), node.lastMessageSent.getValue());
                        System.out.println("MESSAGGIO REINVIATO");
                    } else {
                        // Elezione conclusa
                        System.out.println("ELEZIONE CONCLUSA");

                        // Si imposta coordinatore
                        node.setState(beans.State.COORDINATOR);

                        node.setCoordinatorPort(node.getNodesPort());

                        synchronized (node.getNodeServerToNodes().getCoordService().getElectingCoordinator()) {
                            node.getNodeServerToNodes().getCoordService().getElectingCoordinator().notifyAll();
                        }

                        // Apre la connessione con il Server Cloud
                        System.out.println(node.getServerAddress());
                        node.setNodeClient(NodeClient.getNodeClientInstance(node, node.getServerAddress()));
                        node.getNodeClient().start();

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

    @Override
    public StreamObserver<ElectionServiceOuterClass.ElectionRequest> streamElectionMessage(final StreamObserver<ElectionServiceOuterClass.ElectionResponse> responseObserver)
    {
        return new StreamObserver<ElectionServiceOuterClass.ElectionRequest>() {
            @Override
            public void onNext(ElectionServiceOuterClass.ElectionRequest electionRequest)
            {
                Message electionMessage = new ElectionRequestMessage("electionRequestMessage", electionRequest.getTimestamp(),
                       electionRequest.getNodeId(), electionRequest.getStatus(), electionRequest.getValue());

                node.getMessagesBuffer().put(electionMessage);

                synchronized (observers) {
                    if (observers.get(responseObserver) == null) {
                        observers.put(responseObserver, electionRequest.getNodeId());
                    }
                }

                ElectionServiceOuterClass.ElectionResponse response = ElectionServiceOuterClass.ElectionResponse.newBuilder()
                        .setNodeId(node.getId())
                        .setTimestamp(node.deltaTime())
                        .setAck("Received")
                        .build();

                //passo la risposta nello stream
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable)
            {
                synchronized (observers)
                {
                    int crashedNodeId = observers.get(responseObserver);

                    if (node.getNodeFromNodesList(crashedNodeId) != null && node.getState() == State.ELECTING_COORDINATOR && !node.getNodeServerToNodes().isStop())
                    {
                        System.out.println(throwable.getMessage());
                        System.out.println("Nodo " + crashedNodeId + " non disponibile");
                        System.out.println("RIMOSSO NODO PRECEDENTE " + crashedNodeId);

                        observers.remove(responseObserver);

                        Node removedNode = node.getNodeFromNodesList(crashedNodeId);

                        //                        synchronized(node.getRemovedNodes())
                        //                        {
                        //                            node.getRemovedNodes().add(removedNode);
                        //                        }

                        System.out.println("Il nodo: " + removedNode + " non è più nella rete, rimosso!");

                        node.removeNodeFromNodesList(crashedNodeId);
                        node.updateNextNodes(node);

                        int value = node.getId();

                        if (node.lastRequestStatus.equals("ELECTED"))
                            value = node.getNodesPort();

                        Node nextNode = null;
                        if (node.getNextNodesCopy().size() > 0)
                            nextNode = node.getNextNodesCopy().get(0);

                        if (nextNode != null)
                        {
//                            node.connectToNextNode(nextNode);

                            System.out.println("Reinvio il mio messaggio al prossimo nodo " + nextNode);

                            node.getNodeServerToNodes().sendElectionMessage(nextNode, node.lastMessageSent.getStatus(), node.lastMessageSent.getValue());
                            System.out.println("MESSAGGIO REINVIATO");
                        }
                        else
                        {
                            // Elezione conclusa
                            System.out.println("ELEZIONE CONCLUSA");

                            // Si imposta coordinatore
                            node.setState(beans.State.COORDINATOR);

                            node.setCoordinatorPort(node.getNodesPort());

                            synchronized (node.getNodeServerToNodes().getCoordService().getElectingCoordinator())
                            {
                                node.getNodeServerToNodes().getCoordService().getElectingCoordinator().notifyAll();
                            }

                            // Apre la connessione con il Server Cloud
                            System.out.println(node.getServerAddress());
                            node.setNodeClient(NodeClient.getNodeClientInstance(node, node.getServerAddress()));
                            node.getNodeClient().start();

//                            // Chiude la connessione con gli altri da non coordinatore
//                            node.getNodeServerToNodes().getServerToNodes().shutdownNow();
//
//                            // Apre la connessione con gli altri da coordinatore
//                            NodeServiceImpl nodeService = new NodeServiceImpl(node);
//                            CoordServiceImpl coordService = new CoordServiceImpl(node);
//                            ElectionServiceImpl electionService = new ElectionServiceImpl(node);
//
//                            Server coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
//                                    addService(nodeService).
//                                    addService(coordService).
//                                    addService(electionService).
//                                    build();
//                            node.getNodeServerToNodes().setServerToNodes(coordinatorServer);
//                            try {
//                                node.getNodeServerToNodes().getServerToNodes().start();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }

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

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

//        System.out.println("ELECTION_MESSAGE: " + request.getValue() + " " + node.getId() + " " + request.getStatus());
//        Node nextNode = null;
//
//        if (node.getNextNodes().size() > 0)
//        {
//            nextNode = node.getNextNodes().get(0);
//        }
//
//        if (nextNode != null && nextNode.getState() == beans.State.COORDINATOR && node.getNextNodes().size() > 1)
//        {
//            nextNode = node.getNextNodes().get(1);
////                        node.getNextNodes().remove(nextNode);
////                        node.getNodesList().remove(nextNode);
//        }
//        else if (nextNode != null && nextNode.getState() == beans.State.COORDINATOR && node.getNextNodes().size() <= 1)
//            nextNode = null;
//
//        if (nextNode != null && request.getStatus().equals("ELECTING"))
//        {
//            if (request.getValue() > node.getId())
//            {
//                node.setState(State.ELECTING_COORDINATOR);
//                node.streamElectionMessageToNextNode(nextNode, "ELECTING", request.getValue());
//            }
//            else if (request.getValue() < node.getId() && node.getState() != State.ELECTING_COORDINATOR)
//            {
//                node.setState(State.ELECTING_COORDINATOR);
//                node.streamElectionMessageToNextNode(nextNode, "ELECTING", node.getId());
//            }
//            else if (request.getValue() == node.getId()) // id uguali, allora è il coordinatore
//            {
//                System.out.println("I am the new Coordinator (Node " + node + ") !");
//
//                node.streamElectionMessageToNextNode(nextNode, "ELECTED", node.getNodesPort());
//            }
//        }
//        else if (nextNode != null && request.getStatus().equals("ELECTED"))
//        {
//            // Elimina il vecchio coordinatore dalle liste
//            Node exCoordinator = null;
//            for (Node n : node.getNextNodes())
//                if (n.getState() == beans.State.COORDINATOR)
//                    exCoordinator = n;
//
//            if (exCoordinator != null)
//                node.getNodesList().remove(exCoordinator);
//
//            node.updateNextNodes(node, node.getNodesList());
//
//            System.out.println(node.getNextNodes());
//            System.out.println(node.getNodesList());
//
//            // Aggiorna il nuovo coordinatore
//            int coordPort = request.getValue();
//
//            for (Node n : node.getNodesList())
//            {
//                if (n.getNodesPort() == coordPort)
//                    n.setState(State.COORDINATOR);
//            }
//
//            if (node.getNodesPort() != coordPort)
//            {
//                System.out.println("Non sono il coordinatore!");
//                node.setCoordinatorPort(coordPort);
//                node.setState(State.NOT_COORDINATOR);
//
//                node.streamElectionMessageToNextNode(nextNode, "ELECTED", coordPort);
//            }
//            else // porte uguali, allora è il coordinatore stesso
//            {
//                // Elezione conclusa
//                System.out.println("ELEZIONE CONCLUSA");
//
//                // Si imposta coordinatore
//                node.setState(State.COORDINATOR);
//
//                node.setCoordinatorPort(node.getNodesPort());
//
//                // Apre la connessione con il Server Cloud
//                System.out.println(node.getServerAddress());
//                node.setNodeClient(new NodeClient(node, node.getServerAddress()));
//                node.getNodeClient().start();
//
//                // Chiude la connessione con gli altri da non coordinatore
//                node.getNodeServer().getNotCoordinatorServer().shutdownNow();
//
//                // Apre la connessione con gli altri da coordinatore
//                NodeServiceImpl nodeService = new NodeServiceImpl(node);
//                CoordServiceImpl coordService = new CoordServiceImpl(node);
//                ElectionServiceImpl electionService = new ElectionServiceImpl(node);
//
//                Server coordinatorServer = ServerBuilder.forPort(node.getNodesPort()).
//                        addService(nodeService).
//                        addService(coordService).
//                        addService(electionService).
//                        build();
//                node.getNodeServer().setCoordinatorServer(coordinatorServer);
//                try {
//                    node.getNodeServer().getCoordinatorServer().start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                System.out.println("Coordinator server started, node " + node.getId() + "!");
//
//                for (Node n : node.getNextNodes())
//                    if (n.getState() == beans.State.COORDINATOR)
//                        node.getNextNodes().remove(n);
//
//                for (Node n : node.getNodesList())
//                    if (n.getState() == beans.State.COORDINATOR)
//                        node.getNodesList().remove(n);
//
//                System.out.println(node.getNextNodes());
//                System.out.println(node.getNodesList());
//            }
//        }
    }
}