package server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestConditionalGet {
    public static void main(String[] args) throws InterruptedException {
        DataBase db = new DataBase();
        db.Database();

        // Initial data setup
        db.put("key1", "Dados chave 1".getBytes(StandardCharsets.UTF_8));
        db.put("keyCond", "Valor Inicial".getBytes(StandardCharsets.UTF_8));
        db.put("key2", "Dados chave 2".getBytes(StandardCharsets.UTF_8));
        db.put("keyCondInit", "aprovado".getBytes(StandardCharsets.UTF_8));
        db.put("key3", "Condição já estava satisfeita quando se realizou o pedido!".getBytes(StandardCharsets.UTF_8));

        byte[] result1 = db.getWhen("key3", "keyCondInit", "aprovado".getBytes(StandardCharsets.UTF_8));
        String message1 = result1 != null ? new String(result1, StandardCharsets.UTF_8) : "null";
        System.out.println("Condição satisfeita: " + message1);

        System.out.println("-- Começar o teste com a condição a chegar dps do pedido --");

        // Create a thread that waits for a condition to be met on "keyCond"
        Thread getWhenThread = new Thread(() -> {
            try {
                System.out.println("Thread 1: A aguardar que a condição chegue...");
                byte[] result2 = db.getWhen("key1", "keyCond", "aprovado".getBytes(StandardCharsets.UTF_8));
                String message2 = result2 != null ? new String(result2, StandardCharsets.UTF_8) : "null";
                System.out.println("Thread 1: Condição satisfeita: " + message2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Create another thread that will satisfy the condition by updating "keyCond" after a delay
        Thread putThread = new Thread(() -> {
            try {
                Thread.sleep(3000);  // Wait for 3 seconds before satisfying the condition
                System.out.println("Thread 2: Atualizar a keyCond para receber a mensagem...");
                db.put("keyCond", "aprovado".getBytes(StandardCharsets.UTF_8));
                System.out.println("Thread 2: keyCond atualizada para 'aprovado'");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start both threads
        getWhenThread.start();
        putThread.start();

        // Wait for both threads to finish
        try {
            getWhenThread.join();
            putThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        db.printAllData();
    }
}

/**
 * Para compilar e correr estes testes é preciso utilizar estes comandos por ordem no terminal aberto na pasta src
 * javac -d out -sourcepath main/java main/java/server/*.java
 * javac -d out -cp out -sourcepath test/java test/java/server/TestConditionalGet.java 
 * java -cp out server.TestConditionalGet
 */