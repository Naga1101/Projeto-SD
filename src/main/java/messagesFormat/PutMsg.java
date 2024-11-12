package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import enums.Enums.putCommand;

public class PutMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = (byte) putCommand.PUT.ordinal(); 
    private String key;

    public PutMsg() {}

    public PutMsg(String key) {
        this.key = key;
    }

    public PutMsg(PutMsg msg) {
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

    public String getKey() {
        return key;
    }

    private void setKey(String key) {
        this.key = key;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public String toString() {
        return "PutMsg{key='" + key + "'}";
    }

    @Override
    public void setRequestN(int reqN) {
    }

    @Override
    public PutMsg clone() {
        return new PutMsg(this);
    }
}
