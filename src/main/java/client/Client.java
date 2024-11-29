package client;

import enums.Enums.autenticacao;
import enums.Enums.commandType;
import enums.Enums.getCommand;
import enums.Enums.optionCommand;
import enums.Enums.putCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import messagesFormat.*;
import messagesFormat.MsgInterfaces.CliToServMsg;
import utils.BoundedBuffer;

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
    private List<SavedResponse> arrivedReplys = new LinkedList<>();

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
                    Thread receiveThread = new Thread(() -> {
                        try {
                            receiveMessage(in);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    sendThread.start();
                    receiveThread.start();
                    
                    authenticatedClient();

                    turnOff = true;

                    sendThread.join();
                    receiveThread.join();
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
            try{
                int option = scanner.nextInt();
                scanner.nextLine();

                if(option >= 0 && option < optionCommand.values().length){
                    switch (optionCommand.values()[option]) {
                        case GET:
                            GetMenu();
                            break;
                        case PUT:
                            PutMenu();
                            break;
                        case REPLYS:
                            PrintSavedReplys();
                            break;
                        case EXIT:
                            CliToServMsg exitMsg = new ExitMsg();
                            sendBuffer.push(exitMsg);
                            exit = true;
                            break;
                        default:
                            System.out.println("Opção inválida! Por favor, tente novamente.");
                            break;
                    }
                } else {
                    System.out.println("Opção inválida! Por favor, tente novamente.");
                }
            } catch (Exception e) {
                System.out.println("Opção inválida! Por favor, tente novamente.");
                scanner.nextLine();
            }
        }
    }

    
    private void GetMenu() throws InterruptedException {
        boolean back = false;
        CliToServMsg msg;

        while (!back){
            menus.menuGet();
            try{
                int option = scanner.nextInt();
                scanner.nextLine();

                if(option >= 0 && option < getCommand.values().length){
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
                } else {
                    System.out.println("Opção inválida! Por favor, tente novamente.");
                }
            } catch (Exception e) {
                System.out.println("Opção inválida! Por favor, tente novamente.");
                scanner.nextLine();
            }
        }
    }

    private void PutMenu() throws InterruptedException {
        boolean back = false;

        while (!back){
            menus.menuPut();
            try{
                int option = scanner.nextInt();
                scanner.nextLine();

                if(option >= 0 && option < putCommand.values().length){
                    switch (putCommand.values()[option]) {
                        case PUT:
                            System.out.print("Escreva a chave que pretende armazenar: ");
                            String key = scanner.nextLine();
                            System.out.print("Escreva o conteúdo a armazenar: ");
                            String value = scanner.nextLine();
                            CliToServMsg putMsg = new PutMsg(key, value);

                            System.out.println(putMsg);

                            sendBuffer.push(putMsg);
                            break;
                        case MULTIPUT:
                            System.out.print("Coloque o caminho para o ficheiro que contém as chaves e conteúdos: ");
                            String filePath = scanner.nextLine();
                            try {
                                Map<String, byte[]> keyValuePairs = FileParser.parseFileToMap(filePath);

                                CliToServMsg multiPutMsg = new MultiPutMsg(keyValuePairs);
                                sendBuffer.push(multiPutMsg);
                            } catch (IOException e) {
                                System.out.println("Erro ao ler o ficheiro. Verifique o caminho e o formato e tente novamente.");
                            }
                            break;
                        case BACK:
                            back = true;
                            break;
                        default:
                            System.out.println("Opção inválida! Por favor, tente novamente.");
                            break;
                    }
                } else {
                    System.out.println("Opção inválida! Por favor, tente novamente.");
                }
            } catch (Exception e) {
                System.out.println("Opção inválida! Por favor, tente novamente.");
                scanner.nextLine();
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

    private void receiveMessage(DataInputStream in) throws IOException {
        try {
            while (!turnOff) {
                SavedResponse newResponse = null;
                byte opcodeByte = in.readByte();
                long arrivedTime = Instant.now().toEpochMilli();
    
                commandType opcode;
                try {
                    opcode = commandType.fromCode(opcodeByte);
                } catch (IllegalArgumentException e) {
                    System.out.println("Opcode inválido recebido: " + opcodeByte);
                    continue;
                }
    
                switch (opcode) {
                    case GET:
                        byte commandGetCodeByte = in.readByte();
                        getCommand commandGetCode;
                        try {
                            commandGetCode = getCommand.fromCode(commandGetCodeByte);
                        } catch (IllegalArgumentException e) {
                            System.out.println("commandCode inválido recebido: " + commandGetCodeByte);
                            continue;
                        }
    
                        switch (commandGetCode) {
                            case GET:
                                GetReply getReply = new GetReply();
                                try {
                                    getReply.deserialize(in);
                                    newResponse = new SavedResponse(commandGetCode, getReply.getKey(),
                                            getReply.getReply(), getReply.getRequestedTimestamp(), arrivedTime);
                                } catch (IOException e) {
                                    System.out.println("Erro ao desserializar GetReply: " + e.getMessage());
                                    continue;
                                }
                                break;
    
                            case MULTIGET:
                                MultiGetReply multiGetReply = new MultiGetReply();
                                try {
                                    multiGetReply.deserialize(in);
                                    newResponse = new SavedResponse(commandGetCode, multiGetReply.getReply(),
                                            multiGetReply.getRequestedTimestamp(), arrivedTime);
                                } catch (IOException e) {
                                    System.out.println("Erro ao desserializar MultiGetReply: " + e.getMessage());
                                    continue;
                                }
                                break;
    
                            case GETWHEN:
                                GetWhenReply getWhenReply = new GetWhenReply();
                                try {
                                    getWhenReply.deserialize(in);
                                    newResponse = new SavedResponse(commandGetCode, getWhenReply.getKey(),
                                            getWhenReply.getReply(), getWhenReply.getRequestedTimestamp(), arrivedTime);
                                } catch (IOException e) {
                                    System.out.println("Erro ao desserializar GetWhenReply: " + e.getMessage());
                                    continue;
                                }
                                break;
    
                            default:
                                System.out.println("Unhandled GET command: " + commandGetCode);
                                break;
                        }
                        break;
    
                    case PUT:
                        byte commandPutCodeByte = in.readByte();
                        putCommand commandPutCode;
                        try {
                            commandPutCode = putCommand.fromCode(commandPutCodeByte);
                        } catch (IllegalArgumentException e) {
                            System.out.println("commandCode inválido recebido: " + commandPutCodeByte);
                            continue;
                        }
    
                        switch (commandPutCode) {
                            case PUT:
                                PutReply putReply = new PutReply();
                                try {
                                    putReply.deserialize(in);
                                    newResponse = new SavedResponse(commandPutCode, putReply.getKey(),
                                            putReply.getRequestedTimestamp(), arrivedTime);
                                } catch (IOException e) {
                                    System.out.println("Erro ao desserializar PutReply: " + e.getMessage());
                                    continue;
                                }
                                break;
    
                            case MULTIPUT:
                                MultiPutReply multiPutReply = new MultiPutReply();
                                try {
                                    multiPutReply.deserialize(in);
                                    newResponse = new SavedResponse(commandPutCode,
                                            multiPutReply.getRequestedTimestamp(), arrivedTime);
                                } catch (IOException e) {
                                    System.out.println("Erro ao desserializar MultiPutReply: " + e.getMessage());
                                    continue;
                                }
                                break;
    
                            default:
                                System.out.println("Unhandled PUT command: " + commandPutCode);
                                break;
                        }
                        break;
    
                    default:
                        System.out.println("Unhandled opcode: " + opcode);
                        break;
                }
    
                if (newResponse != null) {
                    arrivedReplys.addLast(newResponse);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao receber mensagem: " + e.getMessage());
            throw e;
        }
    }

    private void PrintSavedReplys() {
        if (arrivedReplys.isEmpty()) {
            System.out.println("Ainda não chegou nenhuma resposta aos pedidos realizados.");
        } else {
            System.out.println("");
            System.out.println("Comando | Data de Pedido | Data de Chegada |    Chave(s)   |    Data");
            for (SavedResponse response : arrivedReplys) {
                System.out.println(response.toString());
            }
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
