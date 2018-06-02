package beans;

import io.grpc.stub.StreamObserver;
import simulators.Measurement;
import simulators.SensorServiceGrpc;
import simulators.SensorServiceOuterClass;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class NodeServiceImpl extends NodeServiceGrpc.NodeServiceImplBase
{
    private Node coordinator;

    public NodeServiceImpl(Node coordinator)
    {
        this.coordinator = coordinator;
    }

    @Override
    public StreamObserver<NodeServiceOuterClass.LocalStatRequest> streamToCoordinator(final StreamObserver<NodeServiceOuterClass.GlobalStatResponse> responseObserver)
    {
        return new StreamObserver<NodeServiceOuterClass.LocalStatRequest>()
        {
            @Override
            public void onNext(NodeServiceOuterClass.LocalStatRequest request)
            {
                // Aggiunge la statistica locale alle statistiche locali
                Stat localStat = new Stat(request.getValue(), request.getTimestamp());

                coordinator.addLocalStat(localStat);

                Set<Stat> globalStats = coordinator.getGlobalStats();

                NodeServiceOuterClass.GlobalStatResponse response = null;

                if (globalStats.size() > 0) {
                    // Costruisco la risposta con la global stat più recente
                    Stat globalStat = (Stat) globalStats.toArray()[globalStats.size()-1];

                    response = NodeServiceOuterClass.GlobalStatResponse.newBuilder().
                            setValue(globalStat.getMean()).setTimestamp(globalStat.getTimestamp()).build();
                }

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable)
            {
                System.out.println("Errore! " + throwable.getMessage());
            }

            @Override
            public void onCompleted()
            {
                responseObserver.onCompleted();
            }
        };
    }
}
