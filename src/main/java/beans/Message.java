package beans;

public abstract class Message
{
    private String header;

    private long timestamp;

    public Message(String header, long timestamp)
    {
        this.header = header;
        this.timestamp = timestamp;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
