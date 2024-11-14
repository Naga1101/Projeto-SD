package server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static client.FileParser.parseFileToMap;

public class TestDataBaseComBatch {
    public static void main(String[] args) throws IOException {
        Map<String, byte[]> test1 = parseFileToMap("test/files/test_file1.txt");
        Map<String, byte[]> test2 = parseFileToMap("test/files/test_file2.txt");
        Map<String, byte[]> test3 = parseFileToMap("test/files/test_file3.txt");
        Map<String, byte[]> test4 = parseFileToMap("test/files/test_file4.txt");
        Map<String, byte[]> test5 = parseFileToMap("test/files/test_file5.txt");
        Map<String, byte[]> test6 = parseFileToMap("test/files/test_file6.txt");
        Map<String, byte[]> test7 = parseFileToMap("test/files/test_file7.txt");
        Map<String, byte[]> test8 = parseFileToMap("test/files/test_file8.txt");

        List<Map<String, byte[]>> testMaps = Arrays.asList(test1, test2, test3, test4, test5, test6, test7, test8);

        DataBaseWithBatch db = new DataBaseWithBatch(20);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new DatabaseOperationTask(db, testMaps));
            threads.add(thread);
        }

        // Start and time the threads
        long startTime = System.currentTimeMillis();
        threads.forEach(Thread::start);

        // Wait for all threads to finish
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

    // Runnable task that performs random database operations
    public static class DatabaseOperationTask implements Runnable {
        private final DataBaseWithBatch db;
        private final List<Map<String, byte[]>> testMaps;

        public DatabaseOperationTask(DataBaseWithBatch db, List<Map<String, byte[]>> testMaps) {
            this.db = db;
            this.testMaps = testMaps;
        }

        @Override
        public void run() {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < 10; i++) { // Each thread performs 10 operations
                int operation = random.nextInt(5); // Randomly choose operation type (0-4)

                switch (operation) {
                    case 0 -> doGet();
                    //case 1 -> doGetWhen();
                    case 2 -> doMultiGet();
                    case 3 -> doPut();
                    case 4 -> doMultiPut();
                }

                //System.out.println("Batch content: ");
                //db.printAllDataBatch();
                //System.out.println("Data content: ");
                //db.printAllDataMain();
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
            }
            else System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - no data");
        }

        private void doGetWhen() {
            String randomKey = getRandomKey();
            String conditionKey = getRandomKey();
            byte[] conditionValue = getRandomValue();
            System.out.println(" | Thread " + Thread.currentThread().getId() + " | - Executing getWhen for key: " + randomKey + " with condition on " + conditionKey);
            try {
                db.getWhen(randomKey, conditionKey, conditionValue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void doMultiGet() {
            String timestamp = db.getCurrentTimestamp();
            Set<String> randomKeys = getRandomKeys(5);
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - Executing multiGet for keys: " + randomKeys);
            Map<String, byte[]> reply = db.multiGetLockToCopy(randomKeys);
            if (reply != null) {
                reply.forEach((key, value) -> {
                    String data = "null";
                    if(value != null) data = new String(value, StandardCharsets.UTF_8);
                    System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | ID: " + key + " - data: " + data);
                });
            }
            else System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - No data found");
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
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | Before multiPut");
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | Batch content: ");
            db.printAllDataBatch();
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | Data content: ");
            db.printAllDataMain();
            Map<String, byte[]> randomPairs = getRandomPairs(10);
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | - Executing multiPut");
            db.multiPut(randomPairs);
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | After multiPut");
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | Batch content: ");
            db.printAllDataBatch();
            System.out.println(timestamp + " | Thread " + Thread.currentThread().getId() + " | Data content: ");
            db.printAllDataMain();
        }

        // Utility methods for generating random keys and values
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

        private Set<String> getRandomKeys(int count) {
            int mapIndex = ThreadLocalRandom.current().nextInt(testMaps.size());
            Map<String, byte[]> map = testMaps.get(mapIndex);
            return map.keySet().stream().limit(count).collect(HashSet::new, HashSet::add, HashSet::addAll);
        }

        private Map<String, byte[]> getRandomPairs(int count) {
            int mapIndex = ThreadLocalRandom.current().nextInt(testMaps.size());
            Map<String, byte[]> map = testMaps.get(mapIndex);
            Map<String, byte[]> result = new HashMap<>();
            map.entrySet().stream().limit(count).forEach(entry -> result.put(entry.getKey(), entry.getValue()));
            return result;
        }
    }
}