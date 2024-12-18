package server;

import java.util.*;

public class ThreadTask implements Runnable {
    private final DataBase db;
    private final int threadId;
    private boolean isFirstRound;
    private final ThreadRequestData requestData;

    public ThreadTask(DataBase db, int threadId, boolean isFirstRound, ThreadRequestData requestData) {
        this.db = db;
        this.threadId = threadId;
        this.isFirstRound = isFirstRound;
        this.requestData = requestData;  
    }

    @Override
    public void run() {
        if (isFirstRound) {
            for (int j = 0; j < requestData.getRequestedKeySets().size(); j++) {
                int numKeys = requestData.getRequestedKeyCounts().get(j);
                Set<String> keys = requestData.getRequestedKeySets().get(j);
    
                Map<String, byte[]> result = db.multiGetLockAll(keys);
                
                System.out.println("Thread " + threadId + " retrieved:");
                for (Map.Entry<String, byte[]> entry : result.entrySet()) {
                    System.out.println("  " + entry.getKey() + " => " + (entry.getValue() != null ? Arrays.toString(entry.getValue()) : "null"));
                }
                System.out.println();
                
            }
        }

        else {
            for (int i = 0; i < requestData.getRequestedKeySets().size(); i++) {
                Set<String> keys = requestData.getRequestedKeySets().get(i);
                Map<String, byte[]> result = db.multiGet(keys);
                
                System.out.println("Thread " + threadId + " retrieved (Second round):");
                for (Map.Entry<String, byte[]> entry : result.entrySet()) {
                    System.out.println("  " + entry.getKey() + " => " + (entry.getValue() != null ? Arrays.toString(entry.getValue()) : "null"));
                }
                System.out.println();  
            }
        }
    }
}
