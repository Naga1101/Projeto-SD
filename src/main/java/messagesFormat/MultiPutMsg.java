package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import enums.Enums.command;

public class MultiPutMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = (byte) command.MULTIPUT.ordinal(); 
    private Map<String, byte[]> pairs;

    public MultiPutMsg() {
        this.pairs = new HashMap<>();
    }

    public MultiPutMsg(Map<String, byte[]> pairs) {
        this.pairs = pairs;
    }

    public MultiPutMsg(MultiPutMsg msg) {
        this.pairs = new HashMap<>(msg.pairs);
    }

    // Serialization method
    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeInt(pairs.size());
        for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
            dos.writeUTF(entry.getKey());
            byte[] value = entry.getValue();
            dos.writeInt(value.length);
            dos.write(value);
        }
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeInt(pairs.size());
        for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
            dos.writeUTF(entry.getKey());
            byte[] value = entry.getValue();
            dos.writeInt(value.length);
            dos.write(value);
        }
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        this.pairs = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = dis.readUTF();     // Read each key
            int length = dis.readInt();     // Read the length of the byte array
            byte[] value = new byte[length];
            dis.readFully(value);           // Read the byte array itself
            pairs.put(key, value);
        }
    }

    public Map<String, byte[]> getPairs() {
        return pairs;
    }

    private void setPairs(Map<String, byte[]> pairs) {
        this.pairs = pairs;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public String toString() {
        return "MultiPutMsg{pairs=" + pairs + "}";
    }

    @Override
    public void setRequestN(int reqN) {}

    @Override
    public MultiPutMsg clone() {
        return new MultiPutMsg(this);
    }
}

