package server;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DataBase implements DBInterface.DB {
    private Logs sessionFile;

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

    public DataBase(Logs sessionFile) {

        // dataBase = new ConcurrentHashMap<>();
        dataBase = new HashMap<>();
        this.sessionFile = sessionFile;
    }

    @Override
    public void put(String key, byte[] data) {
        String timestamp;
        lockDataBase.lock();
        try {
            timestamp = getCurrentTimestamp();
            String dataS = new String(data, StandardCharsets.UTF_8);
            String message = timestamp + " | Put " + " | ID: " + key + " | value: " + dataS;
            sessionFile.log(message);
            message = timestamp + " | Data during Put";
            sessionFile.log(message);
            logAllDataMain();

            dataBase.put(key, data);

            if(!waitingCond.isEmpty()){
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
            }

            message = timestamp + " | Data after Put";
            sessionFile.log(message);
            logAllDataMain();

        } finally {
            lockDataBase.unlock();
        }
    }

    @Override
    public void multiPut(Map<String, byte[]> pairs) {
        String timestamp;
        lockDataBase.lock();
        try {
            timestamp = getCurrentTimestamp();
            String message = timestamp + " | MultiPut " + " | size of multiPut: " + pairs.size();
            sessionFile.log(message);
            message = timestamp + " | Data during MultiPut";
            sessionFile.log(message);
            logAllDataMain();

            pairs.forEach((key, value) -> {
                dataBase.put(key, value);

                if (!waitingCond.isEmpty()) {
                    lockWaitingCond.lock();
                    try {
                        CondKey cond = waitingCond.get(key);
                        if (cond != null && Arrays.equals(value, cond.getData())) {
                            cond.setMet();
                            conditionMet.signalAll();
                        }
                    } finally {
                        lockWaitingCond.unlock();
                    }
                }
            });

        } finally {
            lockDataBase.unlock();
        }

    }

    @Override
    public byte[] get(String key) {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }

        String timestamp;

        lockDataBase.lock();
        byte[] data = null;
        try{
            timestamp = getCurrentTimestamp();
            String message = timestamp + " | Get ";
            sessionFile.log(message);
            message = timestamp + " | Data during Get";
            sessionFile.log(message);
            logAllDataMain();

            data = dataBase.get(key);

            if(data != null) {
                String dataS = new String(data, StandardCharsets.UTF_8);
                message = timestamp + " | Get result from batch " + " | ID: " + key + " | value: " + dataS;
                sessionFile.log(message);
            }

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

    @Override
    //public Map<String, byte[]>  multiGetLockToCopy(Set<String> keys){
    public Map<String, byte[]>  multiGet(Set<String> keys){
        String timestamp;
        HashMap<String, byte[]> dataBaseCopy;
        Map<String, byte[]>  resultMap = new HashMap<>();

        lockDataBase.lock();
        try{
            timestamp = getCurrentTimestamp();
            String message = timestamp + " | MultiGet " + " | nº of keys: " + keys.size();
            sessionFile.log(message);
            message = timestamp + " | Data during MultiGet";
            sessionFile.log(message);
            logAllDataMain();

            dataBaseCopy = new HashMap<>(dataBase);
        } finally {
            lockDataBase.unlock();
        }
        for (String key : keys) {
            String message = timestamp + " | MultiGet " + " | Key: " + " | ID: " + key ;
            sessionFile.log(message);
            resultMap.put(key, dataBaseCopy.get(key));
        }

        return resultMap;
    }

    @Override
    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {
        byte[] wantedData = verifyIfCondAlreadyMet(key, keyCond, valueCond);
        if(wantedData != null) return wantedData;

        CondKey newCond = new CondKey(valueCond, lockWaitingCond);
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
        byte[] currentCondValue = null;
        byte[] wantedData = null;
        lockDataBase.lock();
        try {
            currentCondValue = dataBase.get(keyCond);
            if(currentCondValue != null && Arrays.equals(currentCondValue, valueCond)){
                wantedData = dataBase.get(key);
            }
        } finally {
            lockDataBase.unlock();
        }
        return wantedData;
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

    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }

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
