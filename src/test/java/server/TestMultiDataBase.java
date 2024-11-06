package server;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class TestMultiDataBase {
    public static void main(String[] args) {
        DataBase db = new DataBase();
        db.Database();

        Random random = new Random();

        for (int i = 1; i <= 70; i++) {
            String key = "key" + i;
            byte[] data = new byte[10];
            random.nextBytes(data);
            db.put(key, data);
        }

        HashMap<Integer, ThreadRequestData> threadRequestDataMap = new HashMap<>();
        
        // Generate request data before starting the timer
        List<ThreadRequestData> allRequestData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ThreadRequestData requestData = new ThreadRequestData(i + 1);  
            for (int j = 0; j < 5; j++) {
                int numKeys = 5 + random.nextInt(26);  
                Set<String> keys = new HashSet<>();
                for (int k = 0; k < numKeys; k++) {
                    int keyNum = 1 + random.nextInt(100);  
                    keys.add("key" + keyNum);
                }
                requestData.addRequest(numKeys, keys); 
            }
            allRequestData.add(requestData); 
            threadRequestDataMap.put(i + 1, requestData);  
        }

      
        long firstRoundStartTime = System.nanoTime();

        Thread[] threads = new Thread[10];
        List<ThreadTask> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ThreadRequestData requestData = allRequestData.get(i); 
            ThreadTask task = new ThreadTask(db, i + 1, true, requestData); 
            tasks.add(task);
            threads[i] = new Thread(task);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long firstRoundEndTime = System.nanoTime();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        long firstRoundTime = (firstRoundEndTime - firstRoundStartTime) / 1_000_000; 
        System.out.println("FIRST ROUND WITH THE LOCK ALL TOOK: " + firstRoundTime + " ms");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        long secondRoundStartTime = System.nanoTime();

        for (int i = 0; i < 10; i++) {
            ThreadRequestData requestData = threadRequestDataMap.get(i + 1);  
            ThreadTask task = new ThreadTask(db, i + 1, false, requestData);  
            threads[i] = new Thread(task);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long secondRoundEndTime = System.nanoTime();
        long secondRoundTime = (secondRoundEndTime - secondRoundStartTime) / 1_000_000;
        System.out.println("Second round with the copy method took: " + secondRoundTime + " ms");

        System.out.println("All threads have completed.");
    }
}

/**
 * Para compilar e correr estes testes é preciso utilizar estes comandos por ordem no terminal aberto na pasta src
 * javac -d out -sourcepath main/java main/java/server/*.java
 * javac -d out -cp out -sourcepath test/java test/java/server/TestMultiDataBase.java 
 * java -cp out server.TestMultiDataBase
 */