package server;

import enums.Enums.*;
import messagesFormat.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private UsersAuthenticator usersAuthenticator;
    private Timer activeTimer;
    private static Runnable onTimeout = () -> System.out.println("Inactive for to long!!");
    private String user = "";

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
                byte opcodeByte = in.readByte();
                activeTimer.resetCountdown();

                autenticacao opcode;
                try {
                    opcode = autenticacao.fromCode(opcodeByte);
                } catch (IllegalArgumentException e) {
                    System.out.println("Opcode inválido recebido: " + opcodeByte);
                    continue; // Ignora e espera o próximo opcode válido
                }

                switch(opcode){
					case LOGIN:
						LoginMsg logRequest = new LoginMsg();
						logRequest.deserialize(in);

                        System.out.println(logRequest);

                        name = logRequest.getUsername();
                        password = logRequest.getPassword();

                        reply = usersAuthenticator.logUserIn(name, password);

                        if(reply == 2){
                            loggedIn = true;
                            user = name;
                            activeTimer.assignUsernameToTimer(name);
                        }

						break;
					case REGISTER:
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
    //TODO encapsular o comando com o cliente que o pediu
    private void readFromClient() {
        System.out.println(user + " já efetuou login e vai começar a enviar mensagens!");
        try {
            while (true) {
                byte opcodeByte = in.readByte();
                activeTimer.resetCountdown();

                commandType opcode;
                try {
                    opcode = commandType.fromCode(opcodeByte);
                } catch (IllegalArgumentException e) {
                    System.out.println("Opcode inválido recebido: " + opcodeByte);
                    continue;
                }

                switch(opcode){
                    case GET:
                        byte commandGetCodeByte = in.readByte();
                        activeTimer.resetCountdown();

                        getCommand commandGetCode;
                        try {
                            commandGetCode = getCommand.fromCode(commandGetCodeByte);
                        } catch (IllegalArgumentException e) {
                            System.out.println("commandCode inválido recebido: " + commandGetCodeByte);
                            continue;
                        }

                        switch (commandGetCode){
                            case GET:
                                GetMsg getMsg = new GetMsg();
                                getMsg.deserialize(in);

                                System.out.println(getMsg);
                                break;
                            case MULTIGET:
                                MultiGetMsg multiGetMsg = new MultiGetMsg();
                                multiGetMsg.deserialize(in);

                                System.out.println(multiGetMsg);
                                break;
                            case GETWHEN:
                                GetWhenMsg getWhenMsg = new GetWhenMsg();
                                getWhenMsg.deserialize(in);

                                System.out.println(getWhenMsg);
                                break;
                        }

                        break;

                    case PUT:
                        byte commandPutCodeByte = in.readByte();
                        activeTimer.resetCountdown();

                        putCommand commandPutCode;
                        try {
                            commandPutCode = putCommand.fromCode(commandPutCodeByte);
                        } catch (IllegalArgumentException e) {
                            System.out.println("commandCode inválido recebido: " + commandPutCodeByte);
                            continue;
                        }

                        switch (commandPutCode){
                            case PUT:
                                PutMsg putMsg = new PutMsg();
                                putMsg.deserialize(in);

                                System.out.println(putMsg);
                                break;
                            case MULTIPUT:
                                MultiPutMsg multiPutMsg = new MultiPutMsg();
                                multiPutMsg.deserialize(in);

                                System.out.println(multiPutMsg);
                                break;
                        }

                        break;
                }
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