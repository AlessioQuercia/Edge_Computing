package beans;

public class StatMessage extends Message
{
    private int nodeId;

    private double value;

    public StatMessage(String header, long timestamp, int nodeId, double value)
    {
        super(header, timestamp);
        this.nodeId = nodeId;
        this.value = value;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
