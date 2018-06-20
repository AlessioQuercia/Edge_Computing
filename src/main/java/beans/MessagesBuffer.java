package beans;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MessagesBuffer
{
    public ArrayList<Message> buffer = new ArrayList<Message>();

    public synchronized void put(Message message) {
        buffer.add(message);
        notify();
    }

    public synchronized Message take() {
        Message message = null;

        while (buffer.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (buffer.size() > 0) {
            message = buffer.get(0);
            buffer.remove(0);
        }

        return message;
    }

    public ArrayList<Message> getBuffer() {
        return buffer;
    }

    public ArrayList<Message> getBufferCopy()
    {
        ArrayList<Message> copy;
        synchronized (this)
        {
            copy = new ArrayList<>(buffer);
        }
        return copy;
    }
}
