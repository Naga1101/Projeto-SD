package tests;

public class worflow1c {
    private final static String baseDir = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {
        String client1Path = baseDir + "/test/files/clientsCommands/mixedCommands/client2.txt";

        testClient clientTask = new testClient(client1Path);

        Thread client1Thread = new Thread(clientTask);

        client1Thread.start();
        client1Thread.join();

        //System.out.println("Client with command file " + client1Path + " took " + clientTask.getDuration() + " ms.");
        System.out.println("Single client ended.");
    }
}
    
