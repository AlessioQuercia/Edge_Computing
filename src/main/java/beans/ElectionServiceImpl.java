package beans;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class ElectionServiceImpl extends ElectionServiceGrpc.ElectionServiceImplBase {

    private Node node;

    public ElectionServiceImpl(Node node)
    {
        this.node = node;
    }

    @Override
    public void sendElectionMessage(ElectionServiceOuterClass.ElectionRequest request, StreamObserver<ElectionServiceOuterClass.ElectionResponse> responseObserver)
    {
        Message electionMessage = new ElectionRequestMessage("sendElectionMessage", request.getTimestamp(),
                request.getStatus(), request.getValue());

        node.getMessagesBuffer().put(electionMessage);

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
//                node.sendElectionMessage(nextNode, "ELECTING", request.getValue());
//            }
//            else if (request.getValue() < node.getId() && node.getState() != State.ELECTING_COORDINATOR)
//            {
//                node.setState(State.ELECTING_COORDINATOR);
//                node.sendElectionMessage(nextNode, "ELECTING", node.getId());
//            }
//            else if (request.getValue() == node.getId()) // id uguali, allora è il coordinatore
//            {
//                System.out.println("I am the new Coordinator (Node " + node + ") !");
//
//                node.sendElectionMessage(nextNode, "ELECTED", node.getNodesPort());
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
//                node.sendElectionMessage(nextNode, "ELECTED", coordPort);
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
}