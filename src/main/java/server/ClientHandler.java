package server;

import java.io.*;
import java.net.*;
import java.net.Authenticator;
import java.util.concurrent.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private UsersAuthenticator usersAuthenticator;

    public ClientHandler(Socket socket, UsersAuthenticator usersAuthenticator) {
        this.clientSocket = socket;
        this.usersAuthenticator = usersAuthenticator;
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Start threads for reading and replying to the client
        Thread readerThread = new Thread(this::readFromClient);
        Thread replyThread = new Thread(this::replyToClient);

        readerThread.start();
        replyThread.start();
    }

    private void readFromClient() {
        try {
            while (true) {
                String message = in.readUTF();
                System.out.println("Received from client: " + message);
                // Handle message (could add to queue or other data structure)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replyToClient() {
        try {
            while (true) {
                // Send response to the client
                String response = "Server reply at " + System.currentTimeMillis() + "\n" + usersAuthenticator.toString();
                out.writeUTF(response);
                out.flush();
                Thread.sleep(5000); // Delay for demonstration
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}