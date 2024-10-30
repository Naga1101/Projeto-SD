package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Start threads for sending and receiving messages
            Thread sendThread = new Thread(() -> sendMessage(out));
            Thread receiveThread = new Thread(() -> receiveMessage(in));

            sendThread.start();
            receiveThread.start();

            sendThread.join();
            receiveThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(DataOutputStream out) {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.print("Enter message to send: ");
                String message = scanner.nextLine();
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveMessage(DataInputStream in) {
        try {
            while (true) {
                String response = in.readUTF();
                System.out.println("Server response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
