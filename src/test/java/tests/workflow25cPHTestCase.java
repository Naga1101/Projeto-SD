package tests;

import java.util.ArrayList;
import java.util.List;

public class workflow25cPHTestCase {
    private final static String baseDir = System.getProperty("user.dir");
    private static volatile boolean logMetrics = true;

    public static void main(String[] args) throws Exception {
        String[] multiputPaths = {
            baseDir + "/test/files/clientsCommands/multiPut/client3.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client4.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client10.txt"
        };

        String[] multigetPaths = {
            baseDir + "/test/files/clientsCommands/multiGet/client2.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client5.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client7.txt"
        };

        String[]getEputPaths = {
            baseDir + "/test/files/clientsCommands/put/client1.txt",
            baseDir + "/test/files/clientsCommands/put/client2.txt",
            baseDir + "/test/files/clientsCommands/put/client3.txt",
            baseDir + "/test/files/clientsCommands/put/client4.txt",
            baseDir + "/test/files/clientsCommands/put/client5.txt",
            baseDir + "/test/files/clientsCommands/put/client6.txt",
            baseDir + "/test/files/clientsCommands/put/client7.txt",
            baseDir + "/test/files/clientsCommands/put/client8.txt",
            baseDir + "/test/files/clientsCommands/put/client9.txt",
            baseDir + "/test/files/clientsCommands/put/client10.txt",
            baseDir + "/test/files/clientsCommands/get/client2.txt",
            baseDir + "/test/files/clientsCommands/get/client3.txt",
            baseDir + "/test/files/clientsCommands/get/client7.txt",
            baseDir + "/test/files/clientsCommands/get/client10.txt"
        };

        String[]mixedPath = {
            baseDir + "/test/files/clientsCommands/mixedCommands/client3.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client4.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client5.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client7.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client8.txt"
        };

        List<TestClient> tasks = new ArrayList<>();

        for (String path : multigetPaths) {
            tasks.add(new TestClient(path));
        }

        for (String path : multiputPaths) {
            tasks.add(new TestClient(path));
        }

        for (String path : getEputPaths) {
            tasks.add(new TestClient(path));
        }

        for (String path : mixedPath) {
            tasks.add(new TestClient(path));
        }

        long startTime = System.currentTimeMillis();
        TestLogger log = new TestLogger(7, startTime);

        Thread logger = new Thread(() -> {
            try {
                while (logMetrics) {
                    log.logMetrics();
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.err.println("Logger thread interrupted: " + e.getMessage());
            }
        });
        logger.start();

        List<Thread> threads = new ArrayList<>();
        for (TestClient task : tasks) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        logMetrics = false;   
        logger.join();  

        System.out.println("Results of put-heavy test with 25 clients:");

        long totalDuration = 0;
        long shortestTime = 0;
        long longestTime = 0;
        for (TestClient task : tasks) {
            System.out.println("Client with command file " + task.getPath() + " took " + task.getDuration() + " ms.");
            totalDuration += task.getDuration();
            if(shortestTime == 0 || task.getDuration() < shortestTime) shortestTime = task.getDuration();
            if(task.getDuration() > longestTime) longestTime = task.getDuration();
        }
        System.out.println("Shortest time a client was connected: " + String.format("%.2f", (double) shortestTime) + " ms.");
        System.out.println("Longest time a client was connected: " + String.format("%.2f", (double) longestTime) + " ms.");
        double meanDuration = (double) totalDuration / tasks.size();
        System.out.println("Average duration per client: " + String.format("%.2f", meanDuration) + " ms.");
    }
}

