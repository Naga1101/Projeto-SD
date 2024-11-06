package server;

import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DataBase {

    /* ConcurrentHashMap > performance do que HashTable
     * Permite valores e keys = null
     * Thread-friendly ao contrário do HashMap
     */

    // private static ConcurrentHashMap<String, String> dataBase;
    private static Hashtable<String, byte[]> dataBase;
    private final ReentrantLock lock = new ReentrantLock();

    public void Database() {

        // dataBase = new ConcurrentHashMap<>();
        dataBase = new Hashtable<>();
    }

    public void put(String messageId, byte[] message) {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lock.lock();
        try {
            dataBase.put(messageId, message);
        } finally {
            lock.unlock();
        }
    }

    public byte[] get(String messageId) {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lock.lock();
        byte[] message = null;
        try{
            message = dataBase.get(messageId);
        } finally {
            lock.unlock();
        }

        return message;
    }

    public void printAllMessages() {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lock.lock();
        try {
            dataBase.forEach((key, value) -> {
                String message = new String(value, StandardCharsets.UTF_8);
                System.out.println("ID: " + key + " - Message: " + message);
            });
        } finally {
            lock.unlock();
        }
    }
}