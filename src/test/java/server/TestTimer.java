package server;

public class TestTimer {
    private static final int TIMEOUT = 30000; 
    public static void main(String[] args) {
        // Define what happens when the timer times out
        UsersAuthenticator test = new UsersAuthenticator();
        Runnable onTimeout = () -> System.out.println("Timer finished!");

        // Create a Timer instance with the specified onTimeout action
        Timer timer = new Timer(onTimeout, test);

        System.out.println("Começar o timer...");
        timer.startCountdown();

        try {
            // Sleep for half the timeout duration (15 seconds in this case)
            Thread.sleep(TIMEOUT / 2);
            System.out.println("Vou dar reset ao timer antes dele chegar ao fim...");

            // Reset the timer
            timer.resetCountdown();

            // Allow the timer to reach the timeout after reset
            Thread.sleep(TIMEOUT + 5000); // Sleep slightly longer to ensure timeout
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("O timer acabou sem interupção!");
    }
}

/**
 * Para compilar e correr estes testes é preciso utilizar estes comandos por ordem no terminal aberto na pasta src
 * javac -d out -sourcepath main/java main/java/server/*.java
 * javac -d out -cp out -sourcepath test/java test/java/server/TestTimer.java 
 * java -cp out server.TestTimer
 */