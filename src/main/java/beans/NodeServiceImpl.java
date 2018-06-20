package beans;

import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Set;

public class NodeServiceImpl extends NodeServiceGrpc.NodeServiceImplBase
{
    private Node coordinator;

    private HashMap<StreamObserver, Integer> observers = new HashMap<StreamObserver, Integer>();

    public NodeServiceImpl(Node coordinator)
    {
        this.coordinator = coordinator;
    }

    @Override
    public void sendToCoordinator(NodeServiceOuterClass.LocalStatRequest request, final StreamObserver<NodeServiceOuterClass.GlobalStatResponse> responseObserver)
    {
        try {
            StatMessage localStatMessage = new StatMessage("localStatFromNode", request.getTimestamp(),
                    request.getNodeId(), request.getValue());

            coordinator.getMessagesBuffer().put(localStatMessage);

            synchronized (observers) {
                if (observers.get(responseObserver) == null) {
                    observers.put(responseObserver, request.getNodeId());
                }
            }

            Set<Stat> globalStats = coordinator.getGlobalStatsCopy();

            NodeServiceOuterClass.GlobalStatResponse response = null;

            if (globalStats.size() > 0) {
                // Costruisco la risposta con la global stat più recente
                Stat globalStat = (Stat) globalStats.toArray()[globalStats.size() - 1];

                response = NodeServiceOuterClass.GlobalStatResponse.newBuilder()
                        .setNodeId(coordinator.getId())
                        .setValue(globalStat.getMean())
                        .setTimestamp(globalStat.getTimestamp())
                        .build();
            }

            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            // Errore durante la ricezione della statistica locale e l'invio della statistica globale
            System.out.println("Errore durante la ricezione della statistica locale e l'invio della statistica globale al nodo " + request.getNodeId());
            synchronized (observers)
            {
//                    System.out.println(throwable.getMessage());
                int crashedNodeId = observers.get(responseObserver);

                if (coordinator.getNodeFromNodesList(crashedNodeId) != null && !coordinator.getNodeServerToNodes().isStop()) {

                    System.out.println("RIMOSSO NODO " + crashedNodeId);

                    observers.remove(responseObserver);

                    Node removedNode = coordinator.getNodeFromNodesList(crashedNodeId);

                    synchronized(coordinator.getRemovedNodes())
                    {
                        coordinator.getRemovedNodes().add(removedNode);
                    }

                    System.out.println(removedNode);

                    coordinator.removeNodeFromNodesList(crashedNodeId);
                    coordinator.updateNextNodes(coordinator);
                }

                observers.remove(responseObserver);
            }
        }
    }

    @Override
    public StreamObserver<NodeServiceOuterClass.LocalStatRequest> streamToCoordinator(final StreamObserver<NodeServiceOuterClass.GlobalStatResponse> responseObserver)
    {
        return new StreamObserver<NodeServiceOuterClass.LocalStatRequest>()
        {
            @Override
            public void onNext(NodeServiceOuterClass.LocalStatRequest request)
            {
                StatMessage localStatMessage = new StatMessage("localStatFromNode", request.getTimestamp(),
                        request.getNodeId(), request.getValue());

                coordinator.getMessagesBuffer().put(localStatMessage);


                synchronized (observers) {
                    if (observers.get(responseObserver) == null)
                    {
                        observers.put(responseObserver, request.getNodeId());
                    }
                }

//                // Aggiunge la statistica locale alle statistiche locali
//                Stat localStat = new Stat(request.getNodeId(), request.getValue(), request.getTimestamp());
//
//                coordinator.addLocalStat(localStat);

                Set<Stat> globalStats = coordinator.getGlobalStatsCopy();

                NodeServiceOuterClass.GlobalStatResponse response = null;

                if (globalStats.size() > 0) {
                    // Costruisco la risposta con la global stat più recente
                    Stat globalStat = (Stat) globalStats.toArray()[globalStats.size()-1];

                    response = NodeServiceOuterClass.GlobalStatResponse.newBuilder()
                            .setNodeId(coordinator.getId())
                            .setValue(globalStat.getMean())
                            .setTimestamp(globalStat.getTimestamp())
                            .build();
                }

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable)
            {
                synchronized (observers)
                {
//                    System.out.println(throwable.getMessage());
                    int crashedNodeId = observers.get(responseObserver);

                    if (coordinator.getNodeFromNodesList(crashedNodeId) != null && !coordinator.getNodeServerToNodes().isStop()) {

                        System.out.println("RIMOSSO NODO " + crashedNodeId);

                        observers.remove(responseObserver);

                        Node removedNode = coordinator.getNodeFromNodesList(crashedNodeId);

                        synchronized(coordinator.getRemovedNodes())
                        {
                            coordinator.getRemovedNodes().add(removedNode);
                        }

                        System.out.println(removedNode);

                        coordinator.removeNodeFromNodesList(crashedNodeId);
                        coordinator.updateNextNodes(coordinator);
                    }

                    observers.remove(responseObserver);
                }

//                System.out.println("Comunicazione con il nodo " + senderId + " interrotta!");
//
//                // DA FARE SOLO UNA VOLTA
//
//                Node removedNode = coordinator.getNodeFromNodesList(senderId);
//
//                coordinator.removeNodeFromNodesList(senderId);
//
//                coordinator.advicePreviousNodes(coordinator, removedNode, "RIMOSSO");
            }

            @Override
            public void onCompleted()
            {
                responseObserver.onCompleted();
            }
        };
    }
}
