package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import enums.Enums.getCommand;

public class GetMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = (byte) getCommand.GET.ordinal(); 
    private String key;

    public GetMsg() {}

    public GetMsg(String key) {
        this.key = key;
    }

    public GetMsg(GetMsg msg) {
        this.key = msg.key;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE); 
        dos.writeUTF(key); 
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE); 
        dos.writeUTF(key); 
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.key = dis.readUTF(); 
    }

    ///// Getters e Setters

    public String getKey() {
        return this.key;
    }

    private void setKey(String key) {
        this.key = key;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public void setRequestN(int reqN) {}

    @Override
    public String toString() {
        return "GetMessage{key='" + key + "'}";
    }

    @Override
    public GetMsg clone() {
        return new GetMsg(this);
    }
}