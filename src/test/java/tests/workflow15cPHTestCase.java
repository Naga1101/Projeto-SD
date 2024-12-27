package tests;

import java.util.ArrayList;
import java.util.List;

public class workflow15cPHTestCase {
    private final static String baseDir = System.getProperty("user.dir");
    private static volatile boolean logMetrics = true;

    public static void main(String[] args) throws Exception {
        String[] multiputPaths = {
            baseDir + "/test/files/clientsCommands/multiPut/client6.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client9.txt"
        };

        String[] multigetPaths = {
            baseDir + "/test/files/clientsCommands/multiGet/client5.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client10.txt"
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
            baseDir + "/test/files/clientsCommands/put/client10.txt"
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
        TestLogger log = new TestLogger(4, startTime);

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
