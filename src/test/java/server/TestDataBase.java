package server;

import java.nio.charset.StandardCharsets;

public class TestDataBase {
    public static void main(String[] args) {
        DataBase db = new DataBase();
        db.Database();

        // Simular vários clientes a mandar mensage
        System.out.println("\n-- Mensagens enviadas e recebidas --");
        for (int i = 0; i < 10; i++) {
            final int clientId = i;
            new Thread(() -> {

                // Mensagem enviada pela thread (teste do método put)
                String message = "Message from Client " + clientId;
                db.put(String.valueOf(clientId), message.getBytes());
                System.out.println("Client " + clientId + " sent message with ID: " + clientId + " saying: " + message);

                // Mensagem recebida pela thread (teste do método get)
                byte[] retrievedMessage = db.get(String.valueOf(clientId));
                String formattedRetrievedMessage = new String(retrievedMessage, StandardCharsets.UTF_8);
                if (retrievedMessage == null) {
                    System.out.println("Client " + clientId + " tried to retrieve a message, but it doesn't exist.");
                } else {
                    System.out.println("Client " + clientId + " retrieved message: " + formattedRetrievedMessage);
                }

            }).start();
        }

        try {
            // Esperar um tempo para que todas as threads terminem
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Todas as mensagens da dataBase (teste do método printAllMessages)
        System.out.println("\n-- Todas as mensagens --");
        db.printAllMessages();
    }
}
