package server;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DataBase {

    /* ConcurrentHashMap > performance do que HashTable
     * Permite valores e keys = null
     * Thread-friendly ao contrário do HashMap
     */

    // private static ConcurrentHashMap<String, String> dataBase;
    private static HashMap<String, byte[]> dataBase;
    private final ReentrantLock lockDataBase = new ReentrantLock();

    public void Database() {

        // dataBase = new ConcurrentHashMap<>();
        dataBase = new HashMap<>();
    }

    public void put(String key, byte[] data) {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockDataBase.lock();
        try {
            dataBase.put(key, data);
        } finally {
            lockDataBase.unlock();
        }
    }

    public byte[] get(String key) {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockDataBase.lock();
        byte[] data = null;
        try{
            data = dataBase.get(key);
        } finally {
            lockDataBase.unlock();
        }

        return data;
    }

    public Map<String, byte[]>  multiGetLockAll(Set<String> keys){
        Map<String, byte[]>  resultMap = new HashMap<>();
        
        lockDataBase.lock();
        try{
            for (String key : keys) {
                resultMap.put(key, dataBase.get(key));
            }
        } finally {
            lockDataBase.unlock();
        }

        return resultMap;
    }

    public Map<String, byte[]>  multiGetLockToCopy(Set<String> keys){
        HashMap<String, byte[]> dataBaseCopy;
        Map<String, byte[]>  resultMap = new HashMap<>();
        
        lockDataBase.lock();
        try{
            dataBaseCopy = new HashMap<>(dataBase);
        } finally {
            lockDataBase.unlock();
        }
        for (String key : keys) {
            resultMap.put(key, dataBaseCopy.get(key));
        }
        
        return resultMap;
    }

    public void printAllData() {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockDataBase.lock();
        try {
            dataBase.forEach((key, value) -> {
                String data = new String(value, StandardCharsets.UTF_8);
                System.out.println("ID: " + key + " - data: " + data);
            });
        } finally {
            lockDataBase.unlock();
        }
    }
}