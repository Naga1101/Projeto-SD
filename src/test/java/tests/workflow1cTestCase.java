package tests;

public class workflow1cTestCase {
    private final static String baseDir = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {
        String client1Path = baseDir + "/test/files/clientsCommands/mixedCommands/client2.txt";

        TestClient clientTask = new TestClient(client1Path);

        Thread client1Thread = new Thread(clientTask);

        client1Thread.start();
        client1Thread.join();

        System.out.println("Single client ended.");
    }
}
    
