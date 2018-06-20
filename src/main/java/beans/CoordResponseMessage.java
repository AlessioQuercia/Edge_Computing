package beans;

public class CoordResponseMessage extends Message
{
    private int nodeId;
    private int coordPort;

    public CoordResponseMessage(String header, long timestamp, int nodeId, int coordPort)
    {
        super(header, timestamp);
        this.nodeId = nodeId;
        this.coordPort = coordPort;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getCoordPort() {
        return coordPort;
    }

    public void setCoordPort(int coordPort) {
        this.coordPort = coordPort;
    }
}
