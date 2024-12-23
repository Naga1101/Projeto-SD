package tests;

import java.util.ArrayList;
import java.util.List;

public class workflow15cMPH {
    private final static String baseDir = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {
        String[] multiputPaths = {
            baseDir + "/test/files/clientsCommands/multiput/multiput1.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput2.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput3.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput4.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput5.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput6.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput7.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput8.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput9.txt",
            baseDir + "/test/files/clientsCommands/multiput/multiput10.txt"
        };

        String[] multigetPaths = {
            baseDir + "/test/files/clientsCommands/multiget/multiget1.txt",
            baseDir + "/test/files/clientsCommands/multiget/multiget2.txt"
        };

        String[] putPaths = {
            baseDir + "/test/files/clientsCommands/put/put1.txt",
            baseDir + "/test/files/clientsCommands/put/put2.txt"
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

        System.out.println("Multiput-heavy test with 15 clients completed.");
    }
}