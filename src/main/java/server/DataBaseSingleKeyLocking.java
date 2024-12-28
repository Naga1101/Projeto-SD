package server;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataBaseSingleKeyLocking implements DBInterface.DB {
    private Logs sessionFile;

    private static HashMap<String, DataEntry> dataBase;
    private final ReentrantLock lockDataBase = new ReentrantLock();

    private final Map<String, CondKey> waitingCond = new HashMap<>();
    private final ReentrantLock lockWaitingCond = new ReentrantLock();
    private final Condition conditionMet = lockWaitingCond.newCondition();

    public DataBaseSingleKeyLocking(Logs sessionFile) {
        dataBase = new HashMap<>();
        this.sessionFile = sessionFile;
    }

    @Override
    public void put(String key, byte[] data) {
        DataEntry entry;
        Lock entryLock;

        lockDataBase.lock();
        try {
            entry = dataBase.get(key);
            if (entry == null) {
                entry = new DataEntry(null);
                dataBase.put(key, entry);
            }

            entryLock = entry.getLock();
            entryLock.lock();
        } finally {
            lockDataBase.unlock();
        }

        try {
            String timestamp = getCurrentTimestamp();
            String dataS = new String(data, StandardCharsets.UTF_8);
            String message = timestamp + " | Put " + " | ID: " + key + " | value: " + dataS;
            sessionFile.log(message);

            entry.setData(data);

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

            //logAllDataMain();
        } finally {
            entryLock.unlock();
        }
    }

    @Override
    public void multiPut(Map<String, byte[]> pairs) {
        Map<String, Lock> entryLocks = new HashMap<>();
        List<String> sortedKeys = new ArrayList<>();

        lockDataBase.lock();
        try {
            for (String key : pairs.keySet()) {
                DataEntry entry = dataBase.get(key);
                if (entry == null) {
                    entry = new DataEntry(null);
                    dataBase.put(key, entry);
                }
                entryLocks.put(key, entry.getLock());
            }

            sortedKeys = new ArrayList<>(entryLocks.keySet());
            Collections.sort(sortedKeys);

            for (String key : sortedKeys) {
                entryLocks.get(key).lock();
            }
        } finally {
            lockDataBase.unlock();
        }

        try {
            String timestamp = getCurrentTimestamp();
            for (Map.Entry<String, byte[]> pair : pairs.entrySet()) {
                String key = pair.getKey();
                byte[] value = pair.getValue();
                DataEntry entry = dataBase.get(key);

                String dataS = new String(value, StandardCharsets.UTF_8);
                String message = timestamp + " | MultiPut " + " | Key: " + key + " | Value: " + dataS;
                sessionFile.log(message);

                entry.setData(value);

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
            }

            //logAllDataMain();
        } finally {
            for (String key : sortedKeys) {
                entryLocks.get(key).unlock();
            }
        }
    }

    @Override
    public byte[] get(String key) {
        DataEntry entry;
        Lock entryLock;
    
        lockDataBase.lock();
        try {
            entry = dataBase.get(key);
            if (entry == null) {
                return null;
            }
    
            entryLock = entry.getLock();
            entryLock.lock();
        } finally {
            lockDataBase.unlock();
        }
    
        try {
            String timestamp = getCurrentTimestamp();
            sessionFile.log(timestamp + " | Get " + " | ID: " + key);
    
            byte[] data = entry.getData();
            String dataS;
            if (data != null) {
                dataS = new String(data, StandardCharsets.UTF_8);
            } else {
                dataS = "null";
                data = dataS.getBytes();
            }
            sessionFile.log(timestamp + " | Get result " + " | value: " + dataS);
            return data;
        } finally {
            entryLock.unlock();
        }
    }

    //@Override
    public Map<String, byte[]> multiGetLockAll(Set<String> keys) {
        Map<String, byte[]> resultMap = new HashMap<>();
        Map<String, Lock> entryLocks = new HashMap<>();
        List<String> sortedKeys = new ArrayList<>();

        lockDataBase.lock();
        try {
            for (String key : keys) {
                DataEntry entry = dataBase.get(key);
                if (entry != null) {
                    entryLocks.put(key, entry.getLock());
                }
            }

            sortedKeys = new ArrayList<>(entryLocks.keySet());
            Collections.sort(sortedKeys);

            for (String key : sortedKeys) {
                entryLocks.get(key).lock();
            }
        } finally {
            lockDataBase.unlock();
        }

        try {
            String timestamp = getCurrentTimestamp();
            sessionFile.log(timestamp + " | MultiGet " + " | nº of keys: " + keys.size());
            sessionFile.log(timestamp + " | Data during MultiGet");

            for (String key : keys) {
                DataEntry entry = dataBase.get(key);
                byte[] value = entry != null ? entry.getData() : "null".getBytes();
                resultMap.put(key, value);

                sessionFile.log(timestamp + " | MultiGet " + " | Key: " + key + " | Value: " +
                    (new String(value, StandardCharsets.UTF_8)));
            }

            //logAllDataMain();
        } finally {
            for (String key : sortedKeys) {
                entryLocks.get(key).unlock();
            }
        }

        return resultMap;
    }

    @Override
    //public Map<String, byte[]> multiGetLockToCopy(Set<String> keys) {
    public Map<String, byte[]> multiGet(Set<String> keys) {
        Map<String, byte[]> resultMap = new HashMap<>();
        Map<String, DataEntry> entryMap = new HashMap<>();
        List<String> sortedKeys = new ArrayList<>();
        
        lockDataBase.lock();
        try {
            for (String key : keys) {
                DataEntry entry = dataBase.get(key);
                if (entry != null) {
                    entryMap.put(key, entry);
                }
            }
        
            sortedKeys = new ArrayList<>(entryMap.keySet());
            Collections.sort(sortedKeys);

            for (String key : sortedKeys) {
                entryMap.get(key).getLock().lock();
            }
        } finally {
                lockDataBase.unlock();
        }
        
        try {
            String timestamp = getCurrentTimestamp();
            for (String key : sortedKeys) {
                DataEntry entry = entryMap.get(key);
        
                sessionFile.log(timestamp + " | MultiGet | Key: " + key);
        
                byte[] value = entry.getData();
                resultMap.put(key, value);
        
                sessionFile.log(timestamp + " | MultiGet result | Key: " + key + " | Value: " +
                    (value != null ? new String(value, StandardCharsets.UTF_8) : "null"));
            }
        
            //logAllDataMain();
        } finally {
            for (String key : sortedKeys) {
                entryMap.get(key).getLock().unlock();
            }
        }
        
        return resultMap;
    }

    @Override
    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {
        byte[] result = verifyIfCondAlreadyMet(key, keyCond, valueCond);
        if (result != null) {
            return result;
        }
    
        CondKey newCond = new CondKey(valueCond, lockWaitingCond);
    
        lockWaitingCond.lock();
        try {
            if (!waitingCond.containsKey(keyCond)) {
                waitingCond.put(keyCond, newCond);
            }
    
            while (!newCond.isMet()) {
                conditionMet.await();
            }
    
            //logAllDataMain();
            return verifyIfCondAlreadyMet(key, keyCond, valueCond);
        } finally {
            if (waitingCond.get(keyCond) == newCond) {
                waitingCond.remove(keyCond);
            }
            lockWaitingCond.unlock();
        }
    }

    private byte[] verifyIfCondAlreadyMet(String key, String keyCond, byte[] valueCond) {
        String firstKey = key.compareTo(keyCond) <= 0 ? key : keyCond;
        String secondKey = key.compareTo(keyCond) > 0 ? key : keyCond;

        lockDataBase.lock();
        try {
            if (dataBase.get(firstKey) == null || dataBase.get(secondKey) == null) {
                return null;
            }
        
            Lock firstLock = dataBase.get(firstKey).getLock();
            Lock secondLock = dataBase.get(secondKey).getLock();
        
            firstLock.lock();
            secondLock.lock();
            try {
                DataEntry condEntry = dataBase.get(keyCond);
                DataEntry keyEntry = dataBase.get(key);
        
                if (condEntry == null || keyEntry == null) {
                    return null;
                }
        
                byte[] currentCondValue = condEntry.getData();
                if (currentCondValue != null && Arrays.equals(currentCondValue, valueCond)) {
                    return keyEntry.getData();
                }
                return null;
            } finally {
                firstLock.unlock();
                secondLock.unlock();
            }
        } finally {
            lockDataBase.unlock();
        }
    }

    public void printAllData() {
        if (dataBase == null) {
            throw new IllegalStateException("Database not initialized");
        }
        
        lockDataBase.lock();
        try {
            dataBase.forEach((key, value) -> {
                String data = new String(value.getData(), StandardCharsets.UTF_8);
                // System.out.println("ID: " + key + " - data: " + data);
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
        
            String timestamp = getCurrentTimestamp();
            if(!dataBase.isEmpty()) {
                dataBase.forEach((key, value) -> {
                    String data = new String(value.getData(), StandardCharsets.UTF_8);
                    String message = timestamp + " | Main Content: " + " | Size of main: " + dataBase.size() + " | ID: " + key + " - data: " + data;
                    // System.out.println(message);
                    sessionFile.log(message);
                });
            }
            else {
                String message = timestamp + " | Main Content: " + " | Size of main: " + dataBase.size() + " | Empty ";
                // System.out.println(message);
                sessionFile.log(message);
            }
    }
}
