package beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stat")
public class Stat implements Comparable<Stat>
{
    private double mean;
    private long timestamp;

    public Stat() {};

    public Stat(double mean, long timestamp)
    {
        this.mean = mean;
        this.timestamp = timestamp;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Stat o)
    {
        Long thisTimestamp = timestamp;
        Long otherTimestamp = o.getTimestamp();
        return thisTimestamp.compareTo(otherTimestamp);
    }

    @Override
    public String toString() {
        return "Stat [mean=" + mean + ", timestamp=" + timestamp + "]";
    }
}
