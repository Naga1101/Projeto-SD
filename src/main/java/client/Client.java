package client;

import enums.Enums.autenticacao;
import messagesFormat.AuthReply;
import messagesFormat.LoginMsg;
import messagesFormat.RegisterMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client implements AutoCloseable {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private boolean turnOff = false;
    private ClientMenus menus = new ClientMenus();
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
    }

    private void startClient(){
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            
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

                if(!turnOff) menus.printReply(reply, name);
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

    @Override
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Cliente desconectou-se com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
