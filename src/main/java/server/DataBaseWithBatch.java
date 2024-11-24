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
    Logs sessionFile;
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

    public DataBaseWithBatch(Logs sessionFile, int batchSize) {
        this.sessionFile = sessionFile;
        this.batchSize = batchSize;
        dataBase = new HashMap<>();
        batch = new HashMap<>();
    }

    // puts na batch e caso necessário chamam o flush

    public void put(String key, byte[] data){
        String timestamp;
        lockBatch.lock();
        try {
            timestamp = getCurrentTimestamp();
            String dataS = new String(data, StandardCharsets.UTF_8);
            String message = timestamp + " | Put " + " | ID: " + key + " | value: " + dataS;
            sessionFile.log(message);
            message = timestamp + " | Data during Put";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();

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

            message = timestamp + " | Data after Put";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();

            if (batch.size() >= batchSize) {
                flushBatch();
            }
        } finally {
            lockBatch.unlock();
        }
    }

    public void multiPut(Map<String, byte[]> pairs){
        String timestamp;
        lockBatch.lock();
        try{
            timestamp = getCurrentTimestamp();
            String message = timestamp + " | MultiPut " + " | size of multiPut: " + pairs.size();
            sessionFile.log(message);
            message = timestamp + " | Data during MultiPut";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();

            if(pairs.size() > batchSize){
                if(!batch.isEmpty()) flushBatch();
                flushBiggerBatch(pairs);
                return;
            }

            batch.putAll(pairs);
            if (batch.size() >= batchSize) {
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
        String timestamp;
        byte[] data = null;
        lockBatch.lock();
        try{
            timestamp = getCurrentTimestamp();
            String message = timestamp + " | Get ";
            sessionFile.log(message);
            message = timestamp + " | Data during Get";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();

            data = batch.get(key);

            if(data != null) {
                String dataS = new String(data, StandardCharsets.UTF_8);
                message = timestamp + " | Get result from batch " + " | ID: " + key + " | value: " + dataS;
                sessionFile.log(message);
                return data;
            }

            data = getMain(key);
        } finally {
            lockBatch.unlock();
        }

        String dataS = new String(data, StandardCharsets.UTF_8);
        String message = timestamp + " | Get result from main " + " | ID: " + key + " | value: " + dataS;
        sessionFile.log(message);

        return data;
    }

    public Map<String, byte[]>  multiGetLockToCopy(Set<String> keys){
        String timestamp;
        HashMap<String, byte[]> batchCopy;
        HashMap<String, byte[]> dataBaseCopy;
        Map<String, byte[]>  resultMap = new HashMap<>();

        lockBatch.lock();
        try {
            timestamp = getCurrentTimestamp();
            String message = timestamp + " | MultiGet " + " | nº of keys: " + keys.size();
            sessionFile.log(message);
            message = timestamp + " | Data during MultiGet";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();

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
            String message = timestamp + " | MultiGet " + " | Key: " + " | ID: " + key ;
            sessionFile.log(message);
            byte[] data = batchCopy.get(key);
            data = data != null ? data : dataBaseCopy.get(key);
            resultMap.put(key, data);
            String dataS = new String(data, StandardCharsets.UTF_8);
            message = timestamp + " | MultiGet result: " + " | ID: " + key + " - data: " + dataS;
            sessionFile.log(message);
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
            String timestamp = getCurrentTimestamp();
            String message = timestamp + " | Normal Flush ";
            sessionFile.log(message);
            message = timestamp + " | Data before flush";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();

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

            message = timestamp + " | Data after flush";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();
        } finally {
            lockBatch.unlock();
        }
    }

    public void flushBiggerBatch(Map<String, byte[]> pairs){
        System.out.println("Flushing batch");
        lockDataBase.lock();
        try{
            String timestamp = getCurrentTimestamp();
            String message = timestamp + " | Forced big Flush ";
            sessionFile.log(message);
            message = timestamp + " | Data before flush";
            sessionFile.log(message);
            logAllDataBatch();
            logAllDataMain();

            dataBase.putAll(pairs);

            sessionFile.log(message);
            message = timestamp + " | Data before flush";
            sessionFile.log(message);
        } finally {
            lockDataBase.unlock();
        }
    }



    // prints 

    public void logAllDataMain() {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockDataBase.lock();
        try {
            String timestamp = getCurrentTimestamp();
            if(!dataBase.isEmpty()) {
                dataBase.forEach((key, value) -> {
                    String data = new String(value, StandardCharsets.UTF_8);
                    String message = timestamp + " | Main Content: " + " | Size of main: " + dataBase.size() + " | ID: " + key + " - data: " + data;
                    System.out.println(message);
                    sessionFile.log(message);
                });
            }
            else {
                String message = timestamp + " | Main Content: " + " | Size of main: " + dataBase.size() + " | Empty ";
                System.out.println(message);
                sessionFile.log(message);
            }
        } finally {
            lockDataBase.unlock();
        }
    }

    public void logAllDataBatch() {
        if (batch == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockBatch.lock();
        try {
            String timestamp = getCurrentTimestamp();
            if(!batch.isEmpty()) {
                batch.forEach((key, value) -> {
                    String data = new String(value, StandardCharsets.UTF_8);
                    String message = timestamp + " | Batch Content: " + " | Size of batch: " + batch.size() + " | ID: " + key + " - data: " + data;
                    System.out.println(message);
                    sessionFile.log(message);
                });
            }
            else {
                String message = timestamp + " | Batch Content: " + " | Size of batch: " + batch.size() + " | Empty ";
                System.out.println(message);
                sessionFile.log(message);
            }
        } finally {
            lockBatch.unlock();
        }
    }

    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
}