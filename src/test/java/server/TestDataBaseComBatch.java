package server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static client.FileParser.parseFileToMap;

public class TestDataBaseComBatch {
    public static void main(String[] args) throws IOException {
        Logs logFile = new Logs();

        List<Map<String, byte[]>> testMaps = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Path filePath = Paths.get("test", "files", "MultiPutTestFiles", "test_file" + i + ".txt");
            if (Files.exists(filePath)) {
                testMaps.add(parseFileToMap(filePath.toString()));
            } else {
                System.out.println("File not found: " + filePath.toAbsolutePath());
            }
            //testMaps.add(parseFileToMap("test/files/test_file" + i + ".txt"));
        }

        DataBaseWithBatch db = new DataBaseWithBatch(logFile, 50);
        /** BatchSize dif time
         *  100: 1º- 540, 2º - 759, 3º- 559, 4º- 515, 5º- 643
         *  75: 1º- 548, 2º - 771, 3º- 582, 4º- 628, 5º- 639
         *  50: 1º- 693, 2º - 711, 3º- 545, 4º- 664, 5º- 617
         */

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            threads.add(new Thread(new DatabaseOperationTask(db, testMaps, "doPut")));
            threads.add(new Thread(new DatabaseOperationTask(db, testMaps, "doMultiPut")));
            threads.add(new Thread(new DatabaseOperationTask(db, testMaps, "doGet")));
            threads.add(new Thread(new DatabaseOperationTask(db, testMaps, "doMultiGet")));
        }

        long startTime = System.currentTimeMillis();
        threads.forEach(Thread::start);

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + " ms");
    }

    public static class DatabaseOperationTask implements Runnable {
        private final DataBaseWithBatch db;
        private final List<Map<String, byte[]>> testMaps;
        private final String operationType;

        public DatabaseOperationTask(DataBaseWithBatch db, List<Map<String, byte[]>> testMaps, String operationType) {
            this.db = db;
            this.testMaps = testMaps;
            this.operationType = operationType;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) { 
                switch (operationType) {
                    case "doPut" -> doPut();
                    case "doMultiPut" -> doMultiPut();
                    case "doGet" -> doGet();
                    case "doMultiGet" -> doMultiGet();
                }
                
                sleepRandomTime();
            }
        }

        private void doGet() {
            String timestamp = db.getCurrentTimestamp();
            String randomKey = getRandomKey();
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - Executing get for key: " + randomKey);
            byte[] dataB = db.get(randomKey);
            if (dataB != null) {
                String data = new String(dataB, StandardCharsets.UTF_8);
                System.out.println(timestamp + "Thread " + Thread.currentThread().getId() + " - got data: " + data);
            } else {
                System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - no data");
            }
        }

        private void doMultiGet() {
            String timestamp = db.getCurrentTimestamp();
            Set<String> randomKeys = getRandomKeys(5,20);
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - Executing multiGet for keys: " + randomKeys);
            Map<String, byte[]> reply = db.multiGet(randomKeys);
            if (reply != null) {
                reply.forEach((key, value) -> {
                    String data = value != null ? new String(value, StandardCharsets.UTF_8) : "null";
                    System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | ID: " + key + " - data: " + data);
                });
            } else {
                System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - No data found");
            }
        }

        private void doPut() {
            String timestamp = db.getCurrentTimestamp();
            String randomKey = getRandomKey();
            byte[] randomValue = getRandomValue();
            String data = new String(randomValue, StandardCharsets.UTF_8);
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - Executing put for key: " + randomKey + " with value: " + data);
            db.put(randomKey, randomValue);
        }

        private void doMultiPut() {
            String timestamp = db.getCurrentTimestamp();
            Map<String, byte[]> randomMap = testMaps.get(ThreadLocalRandom.current().nextInt(testMaps.size()));
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - Executing multiPut");
            db.multiPut(randomMap);
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - Completed multiPut");
        }

        private void sleepRandomTime() {
            try {
                int sleepTime = operationType.equals("doMultiGet") 
                    ? ThreadLocalRandom.current().nextInt(5, 30) 
                    : ThreadLocalRandom.current().nextInt(1, 5);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private String getRandomKey() {
            int mapIndex = ThreadLocalRandom.current().nextInt(testMaps.size());
            Map<String, byte[]> map = testMaps.get(mapIndex);
            return map.keySet().stream().skip(ThreadLocalRandom.current().nextInt(map.size())).findFirst().orElse(null);
        }

        private byte[] getRandomValue() {
            int mapIndex = ThreadLocalRandom.current().nextInt(testMaps.size());
            Map<String, byte[]> map = testMaps.get(mapIndex);
            return map.values().stream().skip(ThreadLocalRandom.current().nextInt(map.size())).findFirst().orElse(null);
        }

        private Set<String> getRandomKeys(int minCount, int maxCount) {
            int mapIndex = ThreadLocalRandom.current().nextInt(testMaps.size());
            Map<String, byte[]> map = testMaps.get(mapIndex);
        
            int randomCount = ThreadLocalRandom.current().nextInt(minCount, maxCount + 1); // Random count between minCount and maxCount
            return map.keySet().stream().limit(randomCount).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }        
    }
}

/**
 * Para compilar e correr estes testes é preciso utilizar estes comandos por ordem no terminal aberto na pasta src
 * javac -d out -sourcepath main/java main/java/server/*.java
 * javac -d out -sourcepath main/java main/java/client/*.java
 * javac -d out -cp out -sourcepath test/java test/java/server/TestDataBaseComBatch.java 
 * java -cp out server.TestDataBaseComBatch
 */