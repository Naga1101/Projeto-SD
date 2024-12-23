package server;

import java.util.Map;
import java.util.Set;

import enums.Enums.commandType;
import enums.Enums.getCommand;
import enums.Enums.putCommand;
import messagesFormat.GetMsg;
import messagesFormat.GetReply;
import messagesFormat.GetWhenMsg;
import messagesFormat.GetWhenReply;
import messagesFormat.MsgInterfaces.IMessage;
import messagesFormat.MultiGetMsg;
import messagesFormat.MultiGetReply;
import messagesFormat.MultiPutMsg;
import messagesFormat.MultiPutReply;
import messagesFormat.PutMsg;
import messagesFormat.PutReply;


public class ExecuteTask {
    public void executeTask(ScheduledTask<EncapsulatedMsg<? extends IMessage>> task) throws InterruptedException {
        long arrivalTimestamp = task.getScheduledTimestamp(); 
        EncapsulatedMsg<? extends IMessage> encapsulatedMsg = task.getMessage();

        String user = encapsulatedMsg.getUser();
        IMessage command = encapsulatedMsg.getMessage();

        IMessage reply = null;
        EncapsulatedMsg<IMessage> replyEnc = new EncapsulatedMsg<>(user, reply);

        //System.out.println("Executing command for user: " + encapsulatedMsg.getUser());
        //System.out.println("Command: " + command.toString());

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

                            reply = new GetReply(key, arrivalTimestamp, data);

                            ////System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                    case MULTIGET:
                        if (command instanceof MultiGetMsg) {
                            MultiGetMsg multiGetMsg = (MultiGetMsg) command;

                            Set<String> keySet = multiGetMsg.getKeySet();

                            Map<String, byte[]> dataReply = Server.db.multiGet(keySet);

                            reply = new MultiGetReply(arrivalTimestamp, dataReply);

                            //System.out.println(reply);
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

                            reply = new GetWhenReply(arrivalTimestamp, key, data);
                            

                            //System.out.println("Getwhen successfully");
                            //System.out.println(reply);
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

                            reply = new PutReply(key, arrivalTimestamp);

                            //System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                    case MULTIPUT:
                        if (command instanceof MultiPutMsg) {
                            MultiPutMsg multiPutMsg = (MultiPutMsg) command;

                            Map<String, byte[]> pairs = multiPutMsg.getPairs();

                            Server.db.multiPut(pairs);

                            reply = new MultiPutReply(arrivalTimestamp);

                            //System.out.println(reply);
                        } else {
                            throw new IllegalStateException("Invalid message type for GET operation");
                        }
                        break;
                }
                break;
        }

        replyEnc.setMessage(reply);
        Server.finishedTasks.push(replyEnc);
    }
}
