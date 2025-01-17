package messagesFormat;

import enums.Enums.commandType;
import enums.Enums.putCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PutMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = (byte) commandType.PUT.ordinal();
    private static final byte SUBCODE = (byte) putCommand.PUT.ordinal();
    private String key;
    private byte[] data;

    public PutMsg() {}

    public PutMsg(String key, String data) {
        this.key = key;
        this.data = data.getBytes();
    }

    public PutMsg(PutMsg msg) {
        this.key = msg.key;
    }

    @Override
    public byte getOpcode() {
        return OPCODE;
    }

    @Override
    public byte getSubcode() {
        return SUBCODE;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeUTF(key);
        dos.writeInt(data.length);
        dos.write(data);
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeUTF(key);
        dos.writeInt(data.length);
        dos.write(data);
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.key = dis.readUTF();
        int dataLength = dis.readInt();
        this.data = new byte[dataLength];
        dis.readFully(this.data);
    }

    public String getKey() {
        return key;
    }

    private void setKey(String key) {
        this.key = key;
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public String toString() {
        return "PutMsg{key='" + key + "' | data='" + new String(data) + "'}";
    }

    @Override
    public void setRequestN(int reqN) {
    }

    @Override
    public PutMsg clone() {
        return new PutMsg(this);
    }
}