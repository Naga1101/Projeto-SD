package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import messagesFormat.*;
import enums.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private UsersAuthenticator usersAuthenticator;
    private Timer activeTimer;
    private static Runnable onTimeout = () -> System.out.println("Inactive for to long!!");

    public ClientHandler(Socket socket, UsersAuthenticator usersAuthenticator) {
        this.clientSocket = socket;
        this.usersAuthenticator = usersAuthenticator;
        this.activeTimer = new Timer(onTimeout, usersAuthenticator);
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean loggedIn = false;

        activeTimer.startCountdown();

        loggedIn = authenticateUser(in, out);

        if(loggedIn){
            Thread readerThread = new Thread(this::readFromClient);
            Thread replyThread = new Thread(this::replyToClient);
            
            readerThread.start();
            replyThread.start();
        }
    }

    private boolean authenticateUser(DataInputStream in, DataOutputStream out) {
        boolean loggedIn = false;
        int reply = -1;
        String name;
        String password;
        try {
            while(!loggedIn){
                byte opcode = in.readByte();
                activeTimer.resetCountdown();
                switch(opcode){
					case 1:
						LoginMsg logRequest = new LoginMsg();
						logRequest.deserialize(in);

                        System.out.println(logRequest);

                        name = logRequest.getUsername();
                        password = logRequest.getPassword();

                        reply = usersAuthenticator.logUserIn(name, password);

                        if(reply == 2){
                            loggedIn = true;
                            activeTimer.assignUsernameToTimer(name);
                        }

						break;
					case 2:
						RegisterMsg regRequest = new RegisterMsg();
						regRequest.deserialize(in);

                        System.out.println(regRequest);

                        name = regRequest.getUsername();
                        password = regRequest.getPassword();

                        reply = usersAuthenticator.registerUser(name, password);
                        
						break;
                    default:
                        break;
                    }

                AuthReply answReply = new AuthReply(reply, "test");
                answReply.serialize(out);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Server.clientDisconnected();
            return false; // Assume login failed on error
        }
        return loggedIn;
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
            Server.clientDisconnected();
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