package beans;

public class ElectionResponseMessage extends Message
{
    private int nodeId;
    private String ack;

    public ElectionResponseMessage(String header, long timestamp, int nodeId, String ack)
    {
        super(header, timestamp);
        this.nodeId = nodeId;
        this.ack = ack;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }
}
