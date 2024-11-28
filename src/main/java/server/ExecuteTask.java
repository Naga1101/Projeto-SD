package server;

import messagesFormat.MsgInterfaces.IMessage;

public class ExecuteTask {
    public void executeTask(ScheduledTask<EncapsulatedMsg<? extends IMessage>> task) {
        EncapsulatedMsg<? extends IMessage> encapsulatedMsg = task.getMessage();

        IMessage command = encapsulatedMsg.getMessage();

        // Now you can process the command
        System.out.println("Executing command for user: " + encapsulatedMsg.getUser());
        System.out.println("Command: " + command.toString());

        byte opcode = command.getOpcode();
        byte subcode = command.getSubcode();
    }
}
