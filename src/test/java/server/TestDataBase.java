package server;

import java.util.concurrent.CountDownLatch;

public class TestDataBase {
    public static void main(String[] args) {
        DataBase db = new DataBase();
        db.Database();

        // CountDownLatch para assegurar que todas as threads acabaram
        CountDownLatch latch = new CountDownLatch(10);

        // Simular vários clientes a mandar mensage
        System.out.println("\n-- Mensagens enviadas e recebidas --");
        for (int i = 0; i < 10; i++) {
            final int clientId = i;
            new Thread(() -> {

                // Mensagem enviada pela thread (teste do método put)
                String message = "Message from Client " + clientId;
                db.put(String.valueOf(clientId), message);
                System.out.println("Client " + clientId + " sent message with ID: " + clientId + " saying: " + message);

                // Mensagem recebida pela thread (teste do método get)
                String retrievedMessage = db.get(String.valueOf(clientId));
                if (retrievedMessage == null) {
                    System.out.println("Client " + clientId + " tried to retrieve a message, but it doesn't exist.");
                } else {
                    System.out.println("Client " + clientId + " retrieved message: " + retrievedMessage);
                }


                latch.countDown(); // Decrementar a contagem quando a thread acaba
            }).start();
        }

        try {
            // Esperar que as threads acabem
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Todas as mensagens da dataBase (teste do método printAllMessages)
        System.out.println("\n-- Todas as mensagens --");
        db.printAllMessages();
    }
}
