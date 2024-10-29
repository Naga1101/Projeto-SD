package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static int maxConcurrentUsers;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Insira o número de clientes que podem estar conectados em simultâneo: ");
        int maxConcurrentUsers = scanner.nextInt();
        System.out.println("O número máximo de clientes é: " + maxConcurrentUsers);
        scanner.close();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                threadPool.execute(new server.ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}