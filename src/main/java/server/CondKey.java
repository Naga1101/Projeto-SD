package server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CondKey {
    private final byte[] data;
    private boolean met;
    private final ReentrantLock lock = new ReentrantLock();

    CondKey(byte[] data) {
        this.data = data;
        this.met = false;
    }

    public byte[] getData(){
        return this.data;
    }

    public void setMet() {
        lock.lock();
        try {
            this.met = true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isMet() {
        lock.lock();
        try {
            return this.met;
        } finally {
            lock.unlock();
        }
    }
}

/** com var volatile
public class CondKey {
    private final byte[] data;
    private volatile boolean met;
    CondKey(byte[] data) {
        this.data = data;
        this.met = false;
    }
    public void setMet() {
        this.met = true;
    }
} */