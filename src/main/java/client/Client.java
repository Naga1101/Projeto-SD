package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import messagesFormat.*;
import enums.Enums.*;

import static client.ClientMenus.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private boolean turnOff = false;
    private ClientMenus menus = new ClientMenus();

    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
    }

    private void startClient(){
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            while(!turnOff){
                boolean loggedIn = clientLogin(out, in);
            
                if(loggedIn){
                    Thread sendThread = new Thread(() -> sendMessage(out));
                    Thread receiveThread = new Thread(() -> receiveMessage(in));
    
                    sendThread.start();
                    receiveThread.start();
    
                    sendThread.join();
                    receiveThread.join();
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(DataOutputStream out) {
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

    private boolean clientLogin(DataOutputStream out, DataInputStream in){
        boolean loggedIn = false;
        int reply = -1;
        String name = "";
        Scanner scanner = new Scanner(System.in);
        try {
            while (!loggedIn && !turnOff) {
                // print das opções do menu 1 login | 2 registar | 3 fechar
                menus.menuLogin();
                int option = scanner.nextInt();
                scanner.nextLine();

                if(option == autenticacao.LOGIN.ordinal()){
                    System.out.print("Escreva o seu username: ");
                    name = scanner.nextLine();
                    System.out.print("Escreva a sua password: ");
                    String password = scanner.nextLine();

                    LoginMsg msg = new LoginMsg(name, password);

                    System.out.println(msg);

                    msg.serialize(out);
            
                    out.flush();

                    AuthReply response = new AuthReply();
                    in.readByte(); 
                    response.deserialize(in); 
                    reply = response.getReply();

                    if(reply == 2) loggedIn = true;
                    
                }
                else if(option == autenticacao.REGISTER.ordinal()){
                    System.out.print("Escreva o seu username: ");
                    name = scanner.nextLine();
                    System.out.print("Escreva a sua password: ");
                    String password = scanner.nextLine();

                    RegisterMsg msg = new RegisterMsg(name, password);

                    System.out.println(msg);

                    msg.serialize(out);
            
                    out.flush();

                    AuthReply response = new AuthReply();
                    in.readByte(); 
                    response.deserialize(in); 
                    reply = response.getReply();
                }
                else{
                    turnOff = true;
                }
                out.flush();

                if(!turnOff) menus.printRegisterUserReply(reply, name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }

        return loggedIn;
    }

    private void receiveMessage(DataInputStream in) {
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
