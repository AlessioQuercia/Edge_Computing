package beans;

public class ElectionRequestMessage extends Message
{
    private int nodeId;
    private String status;
    private int value;

    public ElectionRequestMessage(String header, long timestamp, int nodeId, String status, int value) {
        super(header, timestamp);
        this.nodeId = nodeId;
        this.status = status;
        this.value = value;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
