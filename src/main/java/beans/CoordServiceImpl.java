package beans;

import io.grpc.stub.StreamObserver;
import simulators.Measurement;
import simulators.SensorServiceGrpc;
import simulators.SensorServiceOuterClass;

import java.util.ArrayList;

public class CoordServiceImpl extends CoordServiceGrpc.CoordServiceImplBase {

    private Node node;

    public CoordServiceImpl(Node node) {
        this.node = node;
    }

    @Override
    public void askForCoordinator(CoordServiceOuterClass.NodeRequest request, StreamObserver<CoordServiceOuterClass.CoordResponse> responseObserver)
    {
//        // Salva il nodo che ha fatto la richiesta nella lista dei nodi del ricevente (così potrà capire che è un suo vicino)
//        Node requestNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(), request.getNodesPort(), request.getX(), request.getY());

//        if (!node.getNodesList().contains(requestNode))
//            node.getNodesList().add(requestNode);

//        // Aggiorna la lista dei vicini del nodo ricevente
//        node.updateNextNodes(node, node.getNodesList());

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

        CoordServiceOuterClass.CoordResponse response = CoordServiceOuterClass.CoordResponse.newBuilder().setCoordPort(node.getCoordinatorPort()).build();

        //passo la risposta nello stream
        responseObserver.onNext(response);

//        responseObserver.onError(new Exception("Error"));

        //completo e finisco la comunicazione
        responseObserver.onCompleted();
    }

    @Override
    public void adviceNode(CoordServiceOuterClass.NodeRequest request, StreamObserver<CoordServiceOuterClass.NodeResponse> responseObserver)
    {
        // Salva il nuovo vicino nella lista dei nodi (così potrà capire che è un suo vicino)
        Node nextNode = new Node(request.getNodeId(), request.getIpAddress(), request.getSensorsPort(), request.getNodesPort(), request.getX(), request.getY());

        if (!node.getNodesList().contains(nextNode))
            node.getNodesList().add(nextNode);

        // Aggiorna la lista dei vicini del nodo ricevente
        node.updateNextNodes(node, node.getNodesList());

        System.out.println(node.getNextNodes());

        CoordServiceOuterClass.NodeResponse response = CoordServiceOuterClass.NodeResponse.newBuilder().setAck("Received").build();

        //passo la risposta nello stream
        responseObserver.onNext(response);

//        responseObserver.onError(new Exception("Error"));

        //completo e finisco la comunicazione
        responseObserver.onCompleted();
    }
}