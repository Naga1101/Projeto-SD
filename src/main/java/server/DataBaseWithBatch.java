package server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataBaseWithBatch {
    // Main data
    private static HashMap<String, byte[]> dataBase;
    private final ReentrantLock lockDataBase = new ReentrantLock();

    // Batch that recieves all the puts
    private static HashMap<String, byte[]> batch;
    // private final int batchSize = por descobrir;
    private int batchSize;
    private final ReentrantLock lockBatch = new ReentrantLock();

    // Map of conditions waiting in getWhen
    private final Map<String, CondKey> waitingCond = new HashMap<>();
    private final ReentrantLock lockWaitingCond = new ReentrantLock();
    private final Condition conditionMet = lockWaitingCond.newCondition();

    public DataBaseWithBatch(int batchSize) {
        this.batchSize = batchSize;
        dataBase = new HashMap<>();
        batch = new HashMap<>();
    }

    // puts na batch e caso necessário chamam o flush

    public void put(String key, byte[] data){
        //boolean needFlush = false;
        lockBatch.lock();
        try {
            batch.put(key, data);

            if(!waitingCond.isEmpty()){
                lockWaitingCond.lock();
                try {
                    CondKey cond = waitingCond.get(key);
                    if (cond != null && Arrays.equals(data, cond.getData())) {
                        cond.setMet();
                        conditionMet.signalAll(); 
                    }
                } finally {
                    lockWaitingCond.unlock();
                }
            }

            if (batch.size() >= batchSize) {
                //needFlush = true;
                flushBatch();
            }
        } finally {
            lockBatch.unlock();
        }
        /**
        if (needFlush) {
            flushBatch(); 
        } */
    }

    public void multiPut(Map<String, byte[]> pairs){
        if(pairs.size() > batchSize){
            flushBatch();
            flushBiggerBatch(pairs);
        }

        lockBatch.lock();
        try{
            batch.putAll(pairs);
            if (batch.size() >= batchSize) {
                //needFlush = true;
                flushBatch();
            }
        } finally {
            lockBatch.unlock();
        }
    }

    // gets para a main e para a batch

    public byte[] getMain(String key){
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

    public byte[] getBatch(String key){
        if (batch == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockBatch.lock();
        byte[] data = null;
        try{
            data = batch.get(key);
        } finally {
            lockBatch.unlock();
        }

        return data;
    }

    public byte[] get(String key){
        byte[] data = null;
        lockBatch.lock();
        try{
            data = batch.get(key);
            if(data != null) return data;

            data = getMain(key);
        } finally {
            lockBatch.unlock();
        }

        return data;
    }

    public Map<String, byte[]>  multiGetLockToCopy(Set<String> keys){
        HashMap<String, byte[]> batchCopy;
        HashMap<String, byte[]> dataBaseCopy;
        Map<String, byte[]>  resultMap = new HashMap<>();
        
        lockBatch.lock();
        try {
            batchCopy = new HashMap<>(batch);

            lockDataBase.lock();
            try{
                dataBaseCopy = new HashMap<>(dataBase);
            } finally {
                lockDataBase.unlock();
            }
        } finally {
            lockBatch.unlock();
        }

        for (String key : keys) {
            byte[] data = batchCopy.get(key);
            resultMap.put(key, data != null ? data : dataBaseCopy.get(key));
        }        
        
        return resultMap;
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {
        byte[] wantedData = verifyIfCondAlreadyMet(key, keyCond, valueCond);
        if(wantedData != null) return wantedData;
    
        CondKey newCond = new CondKey(valueCond);
        waitingCond.put(keyCond, newCond);  
    
        lockWaitingCond.lock();
        try {
            while (!newCond.isMet()) {  
                conditionMet.await();
            }
            return get(key);
        } finally {
            waitingCond.remove(keyCond);
            lockWaitingCond.unlock();
        }
    }

    private byte[] verifyIfCondAlreadyMet(String key, String keyCond, byte[] valueCond){
        byte[] currentCondValueBatch = null;
        byte[] currentCondValueDB = null;
        byte[] wantedData = null;
        
        lockBatch.lock();
        try {
            currentCondValueBatch = batch.get(keyCond);

            lockDataBase.lock();
            try{

                currentCondValueDB = dataBase.get(keyCond);

                if(currentCondValueBatch != null && Arrays.equals(currentCondValueBatch, valueCond) || 
                   currentCondValueDB != null && Arrays.equals(currentCondValueDB, valueCond)) {
                    wantedData = batch.get(key);
                    if(wantedData == null) wantedData = dataBase.get(key);
                }

            } finally {
                lockDataBase.unlock();
            }

        } finally {
            lockBatch.unlock();
        }
        return wantedData;
    }


    public void flushBatch(){
        System.out.println("Flushing batch");
        lockBatch.lock();
        try {
            if(batch.size() < batchSize){  // pode acontecer de ter chamado o flushBatch mas já alguem ter dado flush antes de adquirir o lock
                return;
            }

            lockDataBase.lock();
            try{
                dataBase.putAll(batch);
                batch.clear();
            } finally {
                lockDataBase.unlock();
            }
        } finally {
            lockBatch.unlock();
        }
    }

    public void flushBiggerBatch(Map<String, byte[]> pairs){
        System.out.println("Flushing batch");
        lockDataBase.lock();
        try{
            dataBase.putAll(pairs);
        } finally {
            lockDataBase.unlock();
        }
    }



    // prints 

    public void printAllDataMain() {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockDataBase.lock();
        try {
            String timestamp = getCurrentTimestamp();
            if(!dataBase.isEmpty()) {
                dataBase.forEach((key, value) -> {
                    String data = new String(value, StandardCharsets.UTF_8);
                    System.out.println(timestamp + " | Main Content: " + " | Size of main: " + dataBase.size() + " | ID: " + key + " - data: " + data);
                });
            }
            else System.out.println(timestamp + " | Main Content: " + " | Size of main: " + dataBase.size() + " | Empty ");
        } finally {
            lockDataBase.unlock();
        }
    }

    public void printAllDataBatch() {
        if (batch == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockBatch.lock();
        try {
            String timestamp = getCurrentTimestamp();
            batch.forEach((key, value) -> {
                String data = new String(value, StandardCharsets.UTF_8);
                System.out.println(timestamp + " | Batch Content: " + " | Size of batch: " + batch.size() + " | ID: " + key + " - data: " + data);
            });
        } finally {
            lockBatch.unlock();
        }
    }

    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
}