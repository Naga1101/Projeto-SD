package server;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DataBase {

    /* ConcurrentHashMap > performance do que HashTable
     * Permite valores e keys = null
     * Thread-friendly ao contrário do HashMap
     */
    // private static ConcurrentHashMap<String, String> dataBase;
    private static Hashtable<String, String> dataBase;
    private final ReentrantLock lock = new ReentrantLock();

    public void Database() {
        // Inicializa o ConcurrentHashMap
        // dataBase = new ConcurrentHashMap<>();
        dataBase = new Hashtable<>();
    }

    public void put(String messageId, String message) {
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

    public String get(String messageId) {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return dataBase.get(messageId);
    }

    public void printAllMessages() {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lock.lock();
        try {
            dataBase.forEach((key, value) -> {
                System.out.println("ID: " + key + " - Message: " + value);
            });
        } finally {
            lock.unlock();
        }
    }
}