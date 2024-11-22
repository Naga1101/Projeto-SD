package client;

import enums.Enums.*;
import messagesFormat.*;
import messagesFormat.MsgInterfaces.*;
import utils.BoundedBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

public class Client implements AutoCloseable {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private boolean turnOff = false;
    private ClientMenus menus = new ClientMenus();
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Scanner scanner = new Scanner(System.in);

    private final int BufferSize = 15;
    private BoundedBuffer<CliToServMsg> sendBuffer = new BoundedBuffer<>(BufferSize);
    // BoundedBuffer<ServToCliMsg> recieveBuffer;

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
                    //Thread receiveThread = new Thread(() -> receiveMessage(in));

                    sendThread.start();
                    //receiveThread.start();
                    
                    authenticatedClient();

                    turnOff = true;

                    sendThread.join();
                    //receiveThread.join();
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }


    private boolean clientLogin(DataOutputStream out, DataInputStream in){
        boolean loggedIn = false;
        int reply = -1;
        String name = "";
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
        }

        return loggedIn;
    }

    private void authenticatedClient() throws InterruptedException {
        boolean exit = false;

        while (!exit){
            menus.menuSelectOption();
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (optionCommand.values()[option]) {
                case GET:
                    GetMenu();
                    break;
                case PUT:
                    PutMenu();
                    break;
                case EXIT:
                    exit = true;
                    break;
                default:
                    System.out.println("Opção inválida! Por favor, tente novamente.");
                    break;
            }
        }
    }

    private void GetMenu() throws InterruptedException {
        boolean back = false;
        CliToServMsg msg;

        while (!back){
            menus.menuGet();
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (getCommand.values()[option]) {
                case GET:
                    System.out.print("Escreva a chave que procura: ");
                    String key = scanner.nextLine();
                    msg = new GetMsg(key);
                    System.out.println(msg);

                    sendBuffer.push(msg);
                    break;
                case MULTIGET:
                    System.out.print("Coloque o caminho para o ficheiro que contêm as chaves que procura: ");
                    String filePath = scanner.nextLine();
                    try {
                        Set<String> KeySet = FileParser.parseFileToSet(filePath);
                        msg = new MultiGetMsg(KeySet);
                        sendBuffer.push(msg);
                    } catch (IOException e) {
                        System.out.println("Erro ao ler o ficheiro. Verifique o caminho e tente novamente.");
                    }
                    break;
                case GETWHEN:
                    System.out.print("Escreva a chave que procura: ");
                    String keyWhen = scanner.nextLine();
                    System.out.print("Escreva a chave onde se vai encontrar a condição: ");
                    String keyCond = scanner.nextLine();
                    System.out.print("Escreva a condição de que está à espera: ");
                    String valueCond = scanner.nextLine();
                    msg = new GetWhenMsg(keyWhen, keyCond, valueCond);

                    sendBuffer.push(msg);
                    break;
                case BACK:
                    back = true;
                    break;
                default:
                    System.out.println("Opção inválida! Por favor, tente novamente.");
                    break;
            }
        }
    }

    private void PutMenu() {
        boolean back = false;

        while (!back){
            menus.menuPut();
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (putCommand.values()[option]) {
                case PUT:
                    break;
                case MULTIPUT:
                    break;
                case BACK:
                    back = true;
                    break;
                default:
                    System.out.println("Opção inválida! Por favor, tente novamente.");
                    break;
            }
        }
    }

    // Send Recieve e Close

    private void sendMessage(DataOutputStream out) {
        try {
            while (!turnOff) {
                CliToServMsg msg = sendBuffer.pop();
                msg.serialize(out);
                out.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessage(DataInputStream in) {
        try {
            while (!turnOff) {
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
            scanner.close();
            System.out.println("Cliente desconectou-se com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
