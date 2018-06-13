package beans;

import simulators.Measurement;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class MeasurementsBuffer
{
    public ArrayList<Measurement> buffer = new ArrayList<Measurement>();

    public synchronized void put(Measurement measurement)
    {
        buffer.add(measurement);
        notify();
    }

    public synchronized Measurement take()
    {
        Measurement measurement = null;

        while(buffer.size() == 0)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if(buffer.size() > 0) {
            measurement = buffer.get(0);
            buffer.remove(0);
        }

        return measurement;
    }

    public synchronized void freeTwenty()
    {
        while(buffer.size() == 0)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if(buffer.size() > 20)
        {
            for (int i = 0; i<20; i++)
            {
                buffer.remove(0);
            }
        }
    }

}
