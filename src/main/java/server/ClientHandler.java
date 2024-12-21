package server;

import enums.Enums.TaskPriority;
import enums.Enums.autenticacao;
import enums.Enums.commandType;
import enums.Enums.getCommand;
import enums.Enums.putCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import messagesFormat.*;
import messagesFormat.MsgInterfaces.CliToServMsg;
import messagesFormat.MsgInterfaces.IMessage;
import utils.BoundedBuffer;
import utils.LogCommands;

public class ClientHandler implements Runnable, AutoCloseable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private UsersAuthenticator usersAuthenticator;
    private Timer activeTimer;
    private String user = "";
    private static LogCommands commandLogs ;
    private final int BufferSize = 15;
    private BoundedBuffer<EncapsulatedMsg> inputBuffer;
    private BoundedBuffer<IMessage> outputBuffer;
    private boolean timedOut = false;
    
    private Runnable onTimeout = () -> {
        System.out.println("Inactive for to long!!");
        TimeoutMsg msg = new TimeoutMsg();
        try {
            timedOut = true;
            msg.serialize(out);
            out.flush();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public ClientHandler(Socket socket, UsersAuthenticator usersAuthenticator, LogCommands commandLogs ) {
        this.clientSocket = socket;
        this.usersAuthenticator = usersAuthenticator;
        this.commandLogs = commandLogs;
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
            Thread handleInputBuffer = new Thread(this::handleInputBuffer);
            
            readerThread.start();
            replyThread.start();
            handleInputBuffer.start();
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
                            inputBuffer = new BoundedBuffer<>(BufferSize);
                            outputBuffer = new BoundedBuffer<>(BufferSize);

                            Server.addUserOutputBuffer(name, outputBuffer);
                        }

						break;
					case REGISTER:
						RegisterMsg regRequest = new RegisterMsg();
						regRequest.deserialize(in);

                        //System.out.println(regRequest);

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
            if(!timedOut) Server.clientDisconnected();
            return false; // Assume login failed on error
        }
        return loggedIn;
    }

    private void readFromClient() {
        // System.out.println(user + " já efetuou login e vai começar a enviar mensagens!");
        try {
            while (true) {
                EncapsulatedMsg<CliToServMsg> encapsulatedMsg = null;

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
                    case EXIT:
                        System.out.println("Cliente " + user + " desconectou-se.");
                        usersAuthenticator.logUserOut(user);
                        Server.clientDisconnected();
                        close();
                        return;

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

                                encapsulatedMsg = new EncapsulatedMsg<>(user, getMsg);
                                encapsulatedMsg.setPriority(TaskPriority.HIGH);

                                System.out.println(encapsulatedMsg);
                                break;
                            case MULTIGET:
                                MultiGetMsg multiGetMsg = new MultiGetMsg();
                                multiGetMsg.deserialize(in);

                                encapsulatedMsg = new EncapsulatedMsg<>(user, multiGetMsg);
                                encapsulatedMsg.setPriority(TaskPriority.MEDIUM);

                                System.out.println(encapsulatedMsg);
                                break;
                            case GETWHEN:
                                GetWhenMsg getWhenMsg = new GetWhenMsg();
                                getWhenMsg.deserialize(in);

                                encapsulatedMsg = new EncapsulatedMsg<>(user, getWhenMsg);
                                encapsulatedMsg.setPriority(TaskPriority.HIGH);

                                System.out.println(encapsulatedMsg);
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

                                encapsulatedMsg = new EncapsulatedMsg<>(user, putMsg);
                                encapsulatedMsg.setPriority(TaskPriority.HIGH);                                

                                System.out.println(encapsulatedMsg);
                                break;
                            case MULTIPUT:
                                MultiPutMsg multiPutMsg = new MultiPutMsg();
                                multiPutMsg.deserialize(in);

                                encapsulatedMsg = new EncapsulatedMsg<>(user, multiPutMsg);
                                encapsulatedMsg.setPriority(TaskPriority.LOW);

                                System.out.println(encapsulatedMsg);
                                break;
                        }

                        break;
                }

                if(encapsulatedMsg != null) inputBuffer.push(encapsulatedMsg);
            }

        } catch (IOException e) {
            System.out.println("Client was disconnected by timeout.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void replyToClient() {
        try {
            while (true) {
                IMessage reply = outputBuffer.pop();
                reply.serialize(out);
                out.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleInputBuffer(){
        try {
            while (true) {
                EncapsulatedMsg<CliToServMsg> EncapsulatedMsg = inputBuffer.pop();
                CliToServMsg message = EncapsulatedMsg.getMessage();

                ScheduledTask taskToSchedule = new ScheduledTask<EncapsulatedMsg>(EncapsulatedMsg);
                logNewCommand(message, taskToSchedule.getScheduledTimestamp());

                TaskPriority priority = EncapsulatedMsg.getPriority();
                taskToSchedule.setBasePriority(priority.getCode());
                taskToSchedule.setRealPriority(priority.getCode());       

                //System.out.println("Passei a tarefa do cliente para o main buffer " + taskToSchedule);
                Server.unscheduledTaks.push(taskToSchedule);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logNewCommand(CliToServMsg message, long time){
        byte opcode = message.getOpcode();
        byte subcode = message.getSubcode();

        String comado = CommandMapper.getCommand(opcode, subcode);
        commandLogs.logCommand(user, comado, time);
    }

    @Override
    public void close() {
        try {
            if (activeTimer != null) activeTimer.stopCountdown();
            if (in != null) in.close();
            if (out != null) out.close();
            if(outputBuffer != null) Server.removeUserOutputBuffer(user);
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            System.out.println("Recursos de ClientHandler fechados corretamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}