package beans;

import io.grpc.stub.StreamObserver;
import simulators.Measurement;
import simulators.SensorServiceGrpc;
import simulators.SensorServiceOuterClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class CoordServiceImpl extends CoordServiceGrpc.CoordServiceImplBase {

    private Node node;

    private Object electingCoordinator = new Object();

    public CoordServiceImpl(Node node) {
        this.node = node;
    }

    @Override
    public void askForCoordinator(CoordServiceOuterClass.NodeRequest request, StreamObserver<CoordServiceOuterClass.CoordResponse> responseObserver)
    {
        synchronized (electingCoordinator)
        {
            while(node.getState() == State.ELECTING_COORDINATOR)
            {
                System.out.println("Elezione in corso");
                try {
                    electingCoordinator.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Rilasciato");

                if (node.getState() != State.ELECTING_COORDINATOR)
                {
                    System.out.println("Coordinatore eletto");
                    break;
                }
            }
        }

        Node requestNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(),
                request.getNodesPort(), request.getX(), request.getY());

        Message nodeRequestMessage = new NodeRequestMessage("askForCoordinator",
                request.getTimestamp(), requestNode, request.getType());

        node.getMessagesBuffer().put(nodeRequestMessage);


//        // Se il ricevente è il coordinatore, salva il nodo che ha fatto la richiesta nella lista dei nodi
//        if (node.getNodesPort() == node.getCoordinatorPort()) {
//            Node requestNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(), request.getNodesPort(), request.getX(), request.getY());
//
//            if (!node.getNodesList().contains(requestNode))
//                node.getNodesList().add(requestNode);
//
//            // Aggiorna la lista dei vicini del nodo ricevente
//            node.updateNextNodes(node, node.getNodesList());
//
//            // Avverte i precedenti (1 o 2) del nodo richiedente che si è aggiunto un nuovo nodo e che sarà un loro nuovo vicino
//            node.advicePreviousNodes(requestNode, "AGGIUNTO");
//        }



//        // Avvisa i due nodi precedenti a quello aggiunto che i suoi prossimi nodi sono cambiati
//        if (requestNode.getNodesList().size() == 3)
//        {
//            // Ci sono solo due nodi (quindi avvisa il precedente)
//            Node previousNode = node.getNextNodes().get(0);
//
//            System.out.println("Node: " + previousNode);
//
//            node.sendUpdateNextNodeMessage(previousNode, requestNode);
//
//        }
//        else if (requestNode.getNodesList().size() > 3)
//        {
//            // Ci sono più di due nodi (quindi avvisa i due prima di lui)
//            Node previousNode[] = {null, null};
//            //        int maxPreviousId = -1;
//            //        for (Node n : node.getNodesList())
//            //            if(n.getId() < requestNode.getId() && n.getId() > maxPreviousId)
//            //                maxPreviousId = n.getId();
//
//            boolean found[] = {false, false};
//            int foundIndex = 0;
//            int tries = 1;
//            while (tries <= 100)
//            {
//                for (Node n : node.getNodesList())
//                {
//                    if (n.getId() == (node.getId() - tries) % 101)
//                    {
//                        previousNode[foundIndex] = n;
//                        System.out.println(n);
//                        node.sendUpdateNextNodeMessage(previousNode[foundIndex], requestNode);
//                        found[foundIndex] = true;
//                        foundIndex++;
//                        break;
//                    }
//                }
//
//                if (found[0] && found[1])
//                    break;
//
//                tries++;
//            }
//        }

//        System.out.println(node.getNextNodes());

        CoordServiceOuterClass.CoordResponse response = CoordServiceOuterClass.CoordResponse.newBuilder()
                .setNodeId(node.getId())
                .setCoordPort(node.getCoordinatorPort())
                .setTimestamp(node.deltaTime())
                .build();

        //passo la risposta nello stream
        responseObserver.onNext(response);

//        responseObserver.onError(new Exception("Error"));

        //completo e finisco la comunicazione
        responseObserver.onCompleted();
    }

    @Override
    public void adviceNode(CoordServiceOuterClass.NodeRequest request, StreamObserver<CoordServiceOuterClass.NodeResponse> responseObserver)
    {
        Node requestNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(),
                request.getNodesPort(), request.getX(), request.getY());

        Message nodeRequestMessage = new NodeRequestMessage("adviceNode",
                request.getTimestamp(), requestNode, request.getType());

        node.getMessagesBuffer().put(nodeRequestMessage);


//        System.out.println("ADVICENODE");
//        if (request.getType().equals("AGGIUNTO")) {
//            // Salva il nuovo vicino nella lista dei nodi (così potrà capire che è un suo vicino)
//            Node nextNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(), request.getNodesPort(), request.getX(), request.getY());
//
//            if (!node.getNodesList().contains(nextNode))
//                node.getNodesList().add(nextNode);
//        }
//        else if (request.getType().equals("RIMOSSO"))
//        {
//            System.out.println("RIMOSSO: " + request.getNodeId());
//            node.removeNodeFromNodesList(request.getNodeId());
//        }
//
//        // Aggiorna la lista dei vicini del nodo ricevente
//        node.updateNextNodes(node, node.getNodesList());
//
//        System.out.println("UPDATED NEXTNODES: " + node.getNextNodes());

        CoordServiceOuterClass.NodeResponse response = CoordServiceOuterClass.NodeResponse.newBuilder()
                .setNodeId(node.getId())
                .setTimestamp(node.deltaTime())
                .setAck("Received")
                .build();

        //passo la risposta nello stream
        responseObserver.onNext(response);

//        responseObserver.onError(new Exception("Error"));

        //completo e finisco la comunicazione
        responseObserver.onCompleted();
    }

    @Override
    public void hiCoordinator(CoordServiceOuterClass.NodeRequest request, StreamObserver<CoordServiceOuterClass.NodeResponse> responseObserver)
    {
        synchronized (electingCoordinator)
        {
            while(node.getState() == State.ELECTING_COORDINATOR)
            {
                System.out.println("Elezione in corso");
                try {
                    electingCoordinator.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Rilasciato");

                if (node.getState() != State.ELECTING_COORDINATOR)
                {
                    System.out.println("Coordinatore eletto");
                    break;
                }
            }
        }

        Node reqNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(),
                request.getNodesPort(), request.getX(), request.getY());

        Message nodeRequestMessage = new NodeRequestMessage("hiCoordinator",
                request.getTimestamp(), reqNode, request.getType());

        node.getMessagesBuffer().put(nodeRequestMessage);


//
//        // Salva il nodo che ha fatto la richiesta nella lista dei nodi
//        Node requestNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(), request.getNodesPort(), request.getX(), request.getY());
//
//        if (!node.getNodesList().contains(requestNode))
//            node.getNodesList().add(requestNode);
//
//        // Aggiorna la lista dei vicini del nodo ricevente
//        node.updateNextNodes(node, node.getNodesList());
//
//        System.out.println("UPDATED NEXTNODES: " + node.getNextNodes());
//
//        System.out.println("UPDATED NODESLIST: " + node.getNodesList());
//
//        if (node.getNodesList().size() > 2) {
//            // Avverte i precedenti (1 o 2) del nodo richiedente che si è aggiunto un nuovo nodo e che sarà un loro nuovo vicino
//            node.advicePreviousNodes(node, requestNode, "AGGIUNTO");
//        }

        CoordServiceOuterClass.NodeResponse response = CoordServiceOuterClass.NodeResponse.newBuilder()
                .setNodeId(node.getId())
                .setTimestamp(node.deltaTime())
                .setAck("Hi")
                .build();

        responseObserver.onNext(response);

        //completo e finisco la comunicazione
        responseObserver.onCompleted();
    }


    public Object getElectingCoordinator() {
        return electingCoordinator;
    }
}