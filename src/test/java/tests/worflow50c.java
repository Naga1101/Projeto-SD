package tests;

import java.util.ArrayList;
import java.util.List;

public class worflow50c {
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
            baseDir + "/test/files/clientsCommands/get/client1.txt",
            baseDir + "/test/files/clientsCommands/get/client2.txt",
            baseDir + "/test/files/clientsCommands/get/client3.txt",
            baseDir + "/test/files/clientsCommands/get/client4.txt",
            baseDir + "/test/files/clientsCommands/get/client5.txt",
            baseDir + "/test/files/clientsCommands/get/client6.txt",
            baseDir + "/test/files/clientsCommands/get/client7.txt",
            baseDir + "/test/files/clientsCommands/get/client8.txt",
            baseDir + "/test/files/clientsCommands/get/client9.txt",
            baseDir + "/test/files/clientsCommands/get/client10.txt"
        };

        String[]mixedPath = {
            baseDir + "/test/files/clientsCommands/mixedCommands/client1.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client2.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client3.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client4.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client5.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client6.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client7.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client8.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client9.txt",
            baseDir + "/test/files/clientsCommands/mixedCommands/client10.txt"
        };

        List<testClient> tasks = new ArrayList<>();

        for (String path : multigetPaths) {
            tasks.add(new testClient(path));
        }

        for (String path : multiputPaths) {
            tasks.add(new testClient(path));
        }

        for (String path : getEputPaths) {
            tasks.add(new testClient(path));
        }

        for (String path : mixedPath) {
            tasks.add(new testClient(path));
        }

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

