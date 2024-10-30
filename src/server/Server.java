package server;

import java.io.*;
import java.net.*;
import java.net.Authenticator;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static int maxConcurrentUsers;
    private static UsersAuthenticator usersAuthenticator;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        usersAuthenticator = new UsersAuthenticator();
        int reply;

        reply = usersAuthenticator.registerUser("admin1", "123");
        reply = usersAuthenticator.registerUser("admin2", "123");
        reply = usersAuthenticator.registerUser("admin3", "123");

        System.out.println(usersAuthenticator);

        System.out.println("Insira o número de clientes que podem estar conectados em simultâneo: ");
        int maxConcurrentUsers = scanner.nextInt();
        System.out.println("O número máximo de clientes é: " + maxConcurrentUsers);
        scanner.close();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new ClientHandler(clientSocket, usersAuthenticator));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}