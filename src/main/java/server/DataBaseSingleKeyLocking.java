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

public class DataBaseSingleKeyLocking implements DBInterface.DB {
    private Logs sessionFile;

    private static HashMap<String, byte[]> dataBase;
    private final ReentrantLock lockDataBase = new ReentrantLock();

    private final HashMap<String, ReentrantLock> keyLocks = new HashMap<>();
    private final ReentrantLock lockKeyLocks = new ReentrantLock();

    private final Map<String, CondKey> waitingCond = new HashMap<>();
    private final ReentrantLock lockWaitingCond = new ReentrantLock();
    private final Condition conditionMet = lockWaitingCond.newCondition();

    public DataBaseSingleKeyLocking(Logs sessionFile) {
        dataBase = new HashMap<>();
        this.sessionFile = sessionFile;
    }

    private ReentrantLock getKeyLock(String key) {
        lockKeyLocks.lock();
        try {
            return keyLocks.computeIfAbsent(key, k -> new ReentrantLock());
        } finally {
            lockKeyLocks.unlock();
        }
    }

    @Override
    public void put(String key, byte[] data) {
        ReentrantLock keyLock = getKeyLock(key);
        keyLock.lock();
        try {
            String timestamp = getCurrentTimestamp();
            String dataS = new String(data, StandardCharsets.UTF_8);
            String message = timestamp + " | Put " + " | ID: " + key + " | value: " + dataS;
            sessionFile.log(message);

            dataBase.put(key, data);

            if (!waitingCond.isEmpty()) {
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

            logAllDataMain();
        } finally {
            keyLock.unlock();
        }
    }

    @Override
    public void multiPut(Map<String, byte[]> pairs) {
        String timestamp = getCurrentTimestamp();
        pairs.forEach((key, value) -> {
            ReentrantLock keyLock = getKeyLock(key);
            keyLock.lock();
            try {
                String dataS = new String(value, StandardCharsets.UTF_8);
                String message = timestamp + " | MultiPut " + " | Key: " + key + " | value: " + dataS;
                sessionFile.log(message);

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

                logAllDataMain();
            } finally {
                keyLock.unlock();
            }
        });
    }

    @Override
    public byte[] get(String key) {
        ReentrantLock keyLock = getKeyLock(key);
        keyLock.lock();
        byte[] data = null;
        try {
            String timestamp = getCurrentTimestamp();
            String message = timestamp + " | Get ";
            sessionFile.log(message);
            message = timestamp + " | Data during Get";
            sessionFile.log(message);
            logAllDataMain();
    
            data = dataBase.get(key);
    
            if (data != null) {
                String dataS = new String(data, StandardCharsets.UTF_8);
                message = timestamp + " | Get result " + " | ID: " + key + " | value: " + dataS;
                sessionFile.log(message);
            }

            logAllDataMain();
        } finally {
            keyLock.unlock();
        }
    
        return data;
    }

    public Map<String, byte[]> multiGetLockAll(Set<String> keys) {
        Map<String, byte[]> resultMap = new HashMap<>();
        String timestamp = getCurrentTimestamp();
    
        sessionFile.log(timestamp + " | MultiGet " + " | nº of keys: " + keys.size());
        sessionFile.log(timestamp + " | Data during MultiGet");
    
        for (String key : keys) {
            ReentrantLock keyLock = getKeyLock(key);
            keyLock.lock();
            try {
                byte[] value = dataBase.get(key);
                resultMap.put(key, value);
    
                sessionFile.log(timestamp + " | MultiGet " + " | Key: " + key + " | Value: " + 
                    (value != null ? new String(value, StandardCharsets.UTF_8) : "null"));

                logAllDataMain();
            } finally {
                keyLock.unlock();
            }
        }
    
        return resultMap;
    }

    @Override
    //public Map<String, byte[]> multiGetLockToCopy(Set<String> keys) {
    public Map<String, byte[]> multiGet(Set<String> keys) {
        String timestamp;
        HashMap<String, byte[]> dataBaseCopy;
        Map<String, byte[]> resultMap = new HashMap<>();
    
        lockDataBase.lock();
        try {
            timestamp = getCurrentTimestamp();
            sessionFile.log(timestamp + " | MultiGetLockToCopy " + " | nº of keys: " + keys.size());
            sessionFile.log(timestamp + " | Data during MultiGetLockToCopy");
            logAllDataMain();
    
            dataBaseCopy = new HashMap<>(dataBase);
        } finally {
            lockDataBase.unlock();
        }
    
        for (String key : keys) {
            byte[] value = dataBaseCopy.get(key);
            resultMap.put(key, value);
    
            sessionFile.log(timestamp + " | MultiGetLockToCopy " + " | Key: " + key + " | Value: " + 
                (value != null ? new String(value, StandardCharsets.UTF_8) : "null"));
        }
        logAllDataMain();
    
        return resultMap;
    }

    @Override
    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {
        byte[] wantedData = verifyIfCondAlreadyMet(key, keyCond, valueCond);
        if (wantedData != null) return wantedData;

        CondKey newCond = new CondKey(valueCond);
        waitingCond.put(keyCond, newCond);

        lockWaitingCond.lock();
        try {
            while (!newCond.isMet()) {
                conditionMet.await();
            }
            logAllDataMain();
            return get(key);
        } finally {
            waitingCond.remove(keyCond);
            lockWaitingCond.unlock();
        }
    }

    private byte[] verifyIfCondAlreadyMet(String key, String keyCond, byte[] valueCond) {
        ReentrantLock condLock = getKeyLock(keyCond);
        ReentrantLock keyLock = getKeyLock(key);

        condLock.lock();
        keyLock.lock();
        try {
            byte[] currentCondValue = dataBase.get(keyCond);
            if (currentCondValue != null && Arrays.equals(currentCondValue, valueCond)) {
                return dataBase.get(key);
            }
            return null;
        } finally {
            keyLock.unlock();
            condLock.unlock();
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
