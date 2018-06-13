package beans;

public class NodeRequestMessage extends Message
{
    private Node node;
    private String type;
    public NodeRequestMessage(String header, long timestamp, Node node, String type)
    {
        super(header, timestamp);
        this.node = node;
        this.type = type;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
