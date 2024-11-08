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

public class DataBase {

    /* ConcurrentHashMap > performance do que HashTable
     * Permite valores e keys = null
     * Thread-friendly ao contrário do HashMap
     */

    // private static ConcurrentHashMap<String, String> dataBase;
    private static HashMap<String, byte[]> dataBase;
    private final ReentrantLock lockDataBase = new ReentrantLock();

    private final Map<String, CondKey> waitingCond = new HashMap<>();
    private final ReentrantLock lockWaitingCond = new ReentrantLock();
    private final Condition conditionMet = lockWaitingCond.newCondition();

    public void Database() {

        // dataBase = new ConcurrentHashMap<>();
        dataBase = new HashMap<>();
    }

    public void put(String key, byte[] data) {
        lockDataBase.lock();
        try {
            dataBase.put(key, data);
    
            lockWaitingCond.lock();
            try {
                CondKey cond = waitingCond.get(key);
                if (cond != null && Arrays.equals(data, cond.getData())) {
                    cond.setMet();
                    conditionMet.signalAll();  // Notify waiting threads
                }
            } finally {
                lockWaitingCond.unlock();
            }
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

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {
        byte[] currentCondValue = get(keyCond); 
        
        if (currentCondValue != null && Arrays.equals(currentCondValue, valueCond)) {
            return get(key);
        }
    
        CondKey newCond = new CondKey(valueCond);
        waitingCond.put(keyCond, newCond);  
    
        lockWaitingCond.lock();
        try {
            while (!newCond.isMet()) {  
                conditionMet.await();
            }
            waitingCond.remove(keyCond);
            return get(key);
        } finally {
            lockWaitingCond.unlock();
        }
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

/** getWhen se a variavel for volatil
public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {
    byte[] currentCondValue = get(keyCond); // this uses the get function above
    
    if (currentCondValue != null && Arrays.equals(currentCondValue, valueCond)) {
        return get(key);
    }

    CondKey newCond = new CondKey(valueCond);
    waitingCond.put(keyCond, newCond);  // Track condition in waitingCond

    lockWaitingCond.lock();
    try {
        while (!newCond.met) {  // Wait until met flag is true
            conditionMet.await();
        }
        waitingCond.remove(keyCond);
        return get(key);
    } finally {
        lockWaitingCond.unlock();
    }
}  */
