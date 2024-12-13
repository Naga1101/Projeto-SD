package server;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataEntry {
    private final Lock lock = new ReentrantLock();
    private byte[] data;

    public DataEntry(byte[] initialData) {
        this.data = initialData;
    }

    public Lock getLock() {
        return lock;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}