package simulators;

import beans.Node;

import java.util.HashSet;
import java.util.Set;

public class Stream implements SensorStream
{
    private Set<Measurement> measurements;

    private Sensor sensor;

    public Stream(Sensor sensor)
    {
        this.sensor = sensor;
        this.measurements = new HashSet<Measurement>();
        System.out.println("Hi, I am a new stream!");
    }

    @Override
    public void sendMeasurement(Measurement m)
    {
        if (Math.abs(System.currentTimeMillis() - Sensor.lastRequestTime) >= 10000)
            sensor.requestNearestNode();

        if (sensor.getCommunicationNode() == null)
            System.out.print("Sto scartando i dati: ");
        else
        {
            // Apro un canale di comunicazione con il nodo più vicino e gli invio i dati
            System.out.print("Sto inviando dati al nodo " + sensor.getCommunicationNode().getId() + ": ");
        }
        this.measurements.add(m);
        System.out.println(m);
    }
}
