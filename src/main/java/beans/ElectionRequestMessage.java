package beans;

public class ElectionRequestMessage extends Message
{
    private String status;
    private int value;

    public ElectionRequestMessage(String header, long timestamp, String status, int value) {
        super(header, timestamp);
        this.status = status;
        this.value = value;
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
