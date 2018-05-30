package beans;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import simulators.Measurement;
import simulators.SensorServiceImpl;
import java.util.ArrayList;
import java.util.Calendar;

public class NodeServer extends Thread
{
    private Node node;

    private Server server;

    private boolean stop;

    private beans.State nodeState;

    private long midnight;

    private long lastTime;

    public SensorServiceImpl getSensorService() {
        return sensorService;
    }

    private SensorServiceImpl sensorService;

    public NodeServer(Node node, beans.State nodeState)
    {
        this.node = node;
        this.nodeState = nodeState;
        this.stop = false;
        this.midnight = computeMidnightMilliseconds();
        this.lastTime = System.currentTimeMillis();
    }

    @Override
    public void run ()
    {
        // Il nodo si mette a disposizione come server in ascolto sulla porta dedicata ai sensori
        try {

            sensorService = new SensorServiceImpl();

            server = ServerBuilder.forPort(node.getSensorsPort()).addService(sensorService).build();

            server.start();

            System.out.println("Server started for node " + node.getId() + "!");

            synchronized (sensorService.bufferLock)
            {
                while (sensorService.insertCounter != 40 && !stop)
                {
                    sensorService.bufferLock.wait();
                    if (sensorService.insertCounter == 40) {
                        double sum = 0;
                        ArrayList<Measurement> bufferCopy = sensorService.getMeasurementsBuffer();
                        for (int i = 0; i < 40; i++)
                            sum += bufferCopy.get(i).getValue();
                        double mean = sum / 40;
                        Stat stat = new Stat(mean, deltaTime());
                        node.addStat(stat);
//                        System.out.println();
//                        System.out.println("Node: " + node.getId() + "  " + stat.getMean() + "  " + stat.getTimestamp());
//                        System.out.print("Type 1 to remove the node from the Server Cloud: ");

                        sensorService.resetInsertCounter();
                        sensorService.getBuffer().freeTwenty();
                    }

                    // Print panel each 5 seconds (if not coordinator)
                    if (Math.abs(System.currentTimeMillis() - lastTime) >= 5000 && nodeState != beans.State.COORDINATOR)
                    {
                        System.out.println("\n");
                        System.out.println("Last local stats:");

                        Stat[] statArray = node.getStats().toArray(new Stat[node.getStats().size()]);
                        int loopDim = Math.min(statArray.length, 3);
                        for (int i = 0; i<loopDim; i++)
                            System.out.println("Node: " + node.getId() + "  " + statArray[statArray.length-1-i].getMean() + "  " + statArray[statArray.length-1-i].getTimestamp());

                        System.out.println();
                        System.out.print("Type \'q\' to remove the node from the Server Cloud: ");

                        lastTime = System.currentTimeMillis();
                    }

                }
            }

//            server.awaitTermination();
            System.out.println("SONO FUORI");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Server getServer()
    {
        return server;
    }

    public void setStop()
    {
        stop = true;
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
}
