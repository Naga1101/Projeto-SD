package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import enums.Enums.commandType;
import enums.Enums.getCommand;
import enums.Enums.putCommand;
import messagesFormat.AuthReply;
import messagesFormat.ExitMsg;
import messagesFormat.GetMsg;
import messagesFormat.GetReply;
import messagesFormat.GetWhenMsg;
import messagesFormat.GetWhenReply;
import messagesFormat.LoginMsg;
import messagesFormat.MsgInterfaces.CliToServMsg;
import messagesFormat.MultiGetMsg;
import messagesFormat.MultiGetReply;
import messagesFormat.MultiPutMsg;
import messagesFormat.MultiPutReply;
import messagesFormat.PutMsg;
import messagesFormat.PutReply;
import messagesFormat.RegisterMsg;
import utils.BoundedBuffer;

public class SimplifiedClient implements AutoCloseable {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private boolean turnOff = false;
    private ClientMenus menus = new ClientMenus();
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String commandFilePath;
    private long lastReadPosition = 0;
    private Scanner scanner = new Scanner(System.in);

    private final int BufferSize = 15;
    private BoundedBuffer<CliToServMsg> sendBuffer = new BoundedBuffer<>(BufferSize);
    private List<SavedResponse> arrivedReplys = new LinkedList<>();
    private String username = "";

    public void startClient(String clientFilePath) throws Exception {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            commandFilePath = clientFilePath;

            try (RandomAccessFile fileReader = new RandomAccessFile(commandFilePath, "r")) {
                fileReader.seek(lastReadPosition);
                String command;
                while ((command = fileReader.readLine()) != null && !turnOff) {
                    lastReadPosition = fileReader.getFilePointer();
                    executeAuthentication(command.trim());
                    Thread.sleep(100);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void executeAuthentication(String command) throws Exception {
        String[] parts = command.split(" ");
        String action = parts[0];

        System.out.println(action + " " + parts[1]);

        switch (action.toUpperCase()) {
            case "LOGIN":
                login(parts[1], parts[2]);
                break;
            case "REGISTER":
                register(parts[1], parts[2]);
                break;
            case "EXIT":
                exit();
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }

    private boolean login(String username, String password) throws Exception {
        boolean loggedIn = false;
        try {
            LoginMsg msg = new LoginMsg(username, password);
            msg.serialize(out);
            out.flush();

            AuthReply response = new AuthReply();
            in.readByte();
            response.deserialize(in);
            int reply = response.getReply();

            if (reply == 2) {
                loggedIn = true;
                this.username = username;
                System.out.println("Logged in as " + username);
                startAuthenticatedClient();
            } else {
                System.out.println("Login failed for user " + username);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loggedIn;
    }

    private void register(String username, String password) throws Exception {
        try {
            RegisterMsg msg = new RegisterMsg(username, password);
            msg.serialize(out);
            out.flush();

            AuthReply response = new AuthReply();
            in.readByte();
            response.deserialize(in);
            int reply = response.getReply();

            System.out.println("Registration " + (reply == 1 ? "successful" : "failed") + " for user " + username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void exit() {
        turnOff = true;
        System.out.println("Exiting client");
    }

    private void startAuthenticatedClient() throws Exception {
        try { 
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

            try (RandomAccessFile fileReader = new RandomAccessFile(commandFilePath, "r")) {
                fileReader.seek(lastReadPosition);
                String command;
                while ((command = fileReader.readLine()) != null && !turnOff) {
                    lastReadPosition = fileReader.getFilePointer();
                    executeCommand(command.trim());
                    Thread.sleep(100);
                }
            }

            sendThread.join(); 
            receiveThread.join(); 
        } catch (InterruptedException e) { 
            e.printStackTrace();
        }
    } 

    private void executeCommand(String command) throws Exception { 
    String[] parts = command.split(" ", 2);
    String action = parts[0];

    System.out.println(action + " " + (parts.length > 1 ? parts[1] : ""));

    switch (action.toUpperCase()) { 
        case "PUT": 
        if (parts.length > 1) {
            String[] putParts = parts[1].split(" ", 2);
            if (putParts.length == 2) {
                put(putParts[0], putParts[1]);
            } else {
                System.out.println("Missing arguments for PUT command.");
            }
        } else {
            System.out.println("Missing arguments for PUT command.");
        }
        break; 
        case "MULTIPUT": 
        if (parts.length > 1) {
            multiPut(parts[1]); 
        } else {
            System.out.println("Missing argument for MULTIPUT command.");
        }
        break; 
        case "GET": 
            if (parts.length > 1) {
                get(parts[1]); 
            } else {
                System.out.println("Missing argument for GET command.");
            }
            break; 
        case "MULTIGET":
            if (parts.length > 1) {
                multiGet(parts[1]);
            } else {
                System.out.println("Missing argument for MULTIGET command.");
            }
            break;
        case "GETWHEN":
            if (parts.length > 1) {
                String[] getWhenParts = parts[1].split(" ", 3);
                if (getWhenParts.length == 3) {
                    getWhen(getWhenParts[0], getWhenParts[1], getWhenParts[2]);
                } else {
                    System.out.println("Missing arguments for GETWHEN command.");
                }
            } else {
                System.out.println("Missing argument for GETWHEN command.");
            }
            break;
        case "EXIT": 
            exitLogout(); 
            break; 
        default: 
            System.out.println("Unknown command: " + command); 
        }
    }

    private void put(String key, String value) throws Exception {
        PutMsg msg = new PutMsg(key, value);
        sendBuffer.push(msg);
    }

    private void multiPut(String filePath) throws Exception {
        if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
            filePath = filePath.substring(1, filePath.length() - 1);
        }

        Map<String, byte[]> keyValuePairs = FileParser.parseFileToMap(filePath);
        MultiPutMsg msg = new MultiPutMsg(keyValuePairs);
        sendBuffer.push(msg);
    }

    private void get(String key) throws Exception {
        GetMsg msg = new GetMsg(key);
        sendBuffer.push(msg);
    }

    private void multiGet(String filePath) throws Exception {
        Set<String> keySet = FileParser.parseFileToSet(filePath);
        MultiGetMsg msg = new MultiGetMsg(keySet);
        sendBuffer.push(msg);
    }

    private void getWhen(String keyWhen, String keyCond, String valueCond) {
        try {
            GetWhenMsg msg = new GetWhenMsg(keyWhen, keyCond, valueCond);
            sendBuffer.push(msg);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send GetWhen message");
        }
    }
  
    private void exitLogout() throws Exception {
        CliToServMsg exitMsg = new ExitMsg();
        sendBuffer.push(exitMsg);
        System.out.println("Exiting client");
        turnOff = true;
    }

    // Send e Receive iguais ao cliente normal

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
        SavedResponse newResponse = null;
        try {
            while (!turnOff) {
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
                                            getReply.getReply(), getReply.getRequestedTimestamp(), arrivedTime, username);
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
                                            multiGetReply.getRequestedTimestamp(), arrivedTime, username);
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
                                            getWhenReply.getReply(), getWhenReply.getRequestedTimestamp(), arrivedTime, username);
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
                                            putReply.getRequestedTimestamp(), arrivedTime, username);
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
                                            multiPutReply.getRequestedTimestamp(), arrivedTime, username);
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
            if (newResponse == null) {
                return;
            }
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
    public void close() throws Exception {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Cliente desconectou-se com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        SimplifiedClient client = new SimplifiedClient();
        client.startClient("/home/naguiar/code/Projeto_Git/Projeto-SD/src/test/files/clientsCommands/client1.txt");
    }
}
