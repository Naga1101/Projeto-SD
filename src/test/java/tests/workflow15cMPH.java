package tests;

import java.util.ArrayList;
import java.util.List;

public class workflow15cMPH {
    private final static String baseDir = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {
        String[] multiputPaths = {
            baseDir + "/test/files/clientsCommands/multiPut/client1.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client2.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client3.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client4.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client5.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client6.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client7.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client8.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client9.txt",
            baseDir + "/test/files/clientsCommands/multiPut/client10.txt"
        };

        String[] multigetPaths = {
            baseDir + "/test/files/clientsCommands/multiGet/client5.txt",
            baseDir + "/test/files/clientsCommands/multiGet/client8.txt"
        };

        String[] putPaths = {
            baseDir + "/test/files/clientsCommands/put/client1.txt",
            baseDir + "/test/files/clientsCommands/put/client2.txt"
        };

        String mixedPath = baseDir + "/test/files/clientsCommands/mixedCommands/client1.txt";

        List<testClient> tasks = new ArrayList<>();

        for (String path : multiputPaths) {
            tasks.add(new testClient(path));
        }

        for (String path : multigetPaths) {
            tasks.add(new testClient(path));
        }

        for (String path : putPaths) {
            tasks.add(new testClient(path));
        }

        tasks.add(new testClient(mixedPath));

        long startTime = System.currentTimeMillis();

        List<Thread> threads = new ArrayList<>();
        for (testClient task : tasks) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (testClient task : tasks) {
            System.out.println("Client with command file " + task.getPath() + " took " + task.getDuration() + " ms.");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Multiput-heavy test with 15 clients completed in " + duration + " ms.");
    }
}