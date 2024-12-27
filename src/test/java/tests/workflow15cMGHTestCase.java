package tests;

import java.util.ArrayList;
import java.util.List;

public class workflow15cMGHTestCase {
    private final static String baseDir = System.getProperty("user.dir");
    private static volatile boolean logMetrics = true;

    public static void main(String[] args) throws Exception {
        String[] multiputPaths = {
            baseDir + "/test/files/clientsCommands/multiPut/client5.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client10.txt"
        };

        String[] multigetPaths = {
            baseDir + "/test/files/clientsCommands/multiGet/client1.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client2.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client3.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client4.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client5.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client6.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client7.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client8.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client9.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client10.txt"
        };

        String[]getEputPaths = {
            baseDir + "/test/files/clientsCommands/put/client4.txt",
            baseDir + "/test/files/clientsCommands/get/client7.txt"
        };

        String mixedPath = baseDir + "/test/files/clientsCommands/mixedCommands/client3.txt";

        List<TestClient> tasks = new ArrayList<>();

        for (String path : multigetPaths) {
            tasks.add(new TestClient(path));
        }

        for (String path : multiputPaths) {
            tasks.add(new TestClient(path));
        }

        for (String path :getEputPaths) {
            tasks.add(new TestClient(path));
        }

        tasks.add(new TestClient(mixedPath));

        long startTime = System.currentTimeMillis();
        TestLogger log = new TestLogger(2, startTime);

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

        for (TestClient task : tasks) {
            System.out.println("Client with command file " + task.getPath() + " took " + task.getDuration() + " ms.");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Multiput-heavy test with 15 clients completed in " + duration + " ms.");
    }
}