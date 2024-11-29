package server;

import enums.Enums;
import messagesFormat.*;
import messagesFormat.MsgInterfaces.IMessage;

import java.util.Map;
import java.util.Set;

import static enums.Enums.getCommand;
import static enums.Enums.commandType;
import static enums.Enums.putCommand;


public class ExecuteTask {
    public void executeTask(ScheduledTask<EncapsulatedMsg<? extends IMessage>> task) throws InterruptedException {
        EncapsulatedMsg<? extends IMessage> encapsulatedMsg = task.getMessage();

        IMessage command = encapsulatedMsg.getMessage();

        System.out.println("Executing command for user: " + encapsulatedMsg.getUser());
        System.out.println("Command: " + command.toString());

        commandType opcode = commandType.fromCode(command.getOpcode());
        byte subcode = command.getSubcode();

        switch(opcode){
            case GET:
                getCommand subcodeGet = getCommand.fromCode(subcode);
                switch (subcodeGet){
                    case GET:
                        if (command instanceof GetMsg) {
                            GetMsg getMsg = (GetMsg) command;

                            String key = getMsg.getKey();

                            byte[] data = Server.db.get(key);

                            GetReply reply = new GetReply(data);

                            System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                    case MULTIGET:
                        if (command instanceof MultiGetMsg) {
                            MultiGetMsg multiGetMsg = (MultiGetMsg) command;

                            Set<String> keySet = multiGetMsg.getKeySet();

                            Map<String, byte[]> dataReply = Server.db.multiGetLockToCopy(keySet);

                            MultiGetReply reply = new MultiGetReply(dataReply);

                            System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                    case GETWHEN:
                        if (command instanceof GetWhenMsg) {
                            GetWhenMsg getWhenMsg = (GetWhenMsg) command;

                            String key = getWhenMsg.getKey();
                            String keyCond = getWhenMsg.getKeyCond();
                            byte[] valueCond = getWhenMsg.getValueCond();

                            byte[] data = Server.db.getWhen(key, keyCond, valueCond);

                            GetWhenReply reply = new GetWhenReply(data);

                            System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                }
                break;
            case PUT:
                putCommand subcodePut = putCommand.fromCode(subcode);
                switch (subcodePut){
                    case PUT:
                        if (command instanceof PutMsg) {
                            PutMsg putMsg = (PutMsg) command;

                            String key = putMsg.getKey();
                            byte[] data = putMsg.getData();

                            Server.db.put(key, data);

                            PutReply reply = new PutReply();

                            System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                    case MULTIPUT:
                        if (command instanceof MultiPutMsg) {
                            MultiPutMsg multiPutMsg = (MultiPutMsg) command;

                            Map<String, byte[]> pairs = multiPutMsg.getPairs();

                            Server.db.multiPut(pairs);

                            MultiPutReply reply = new MultiPutReply();

                            System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                }
                break;
        }
    }
}
