package tests;

public class workflow1cTestCase {
    private final static String baseDir = System.getProperty("user.dir");
    private static volatile boolean logMetrics = true;

    public static void main(String[] args) throws Exception {
        String client1Path = baseDir + "/test/files/clientsCommands/mixedCommands/client2.txt";

        TestClient clientTask = new TestClient(client1Path);

        Thread client1Thread = new Thread(clientTask);

        long startTime = System.currentTimeMillis();
        TestLogger log = new TestLogger(1, startTime);

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
        client1Thread.start();

        client1Thread.join();  
        logMetrics = false;   
        logger.join();         

        System.out.println("Single client ended.");
    }
}

    
