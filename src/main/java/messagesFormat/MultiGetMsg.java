package messagesFormat;

import enums.Enums.commandType;
import enums.Enums.getCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MultiGetMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = (byte) commandType.GET.ordinal();
    private static final byte SUBCODE = (byte) getCommand.MULTIGET.ordinal();
    private Set<String> keySet;

    public MultiGetMsg() {}

    public MultiGetMsg(Set<String> keySet) {
        this.keySet = keySet;
    }

    public MultiGetMsg(MultiGetMsg msg) {
        this.keySet = msg.keySet;
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
        dos.writeInt(keySet.size());
        for (String key : keySet) {
            dos.writeUTF(key);
        }
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeInt(keySet.size());
        for (String key : keySet) {
            dos.writeUTF(key);
        }
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        this.keySet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            this.keySet.add(dis.readUTF());
        }
    }

    ///// Getters e Setters

    public Set<String> getKeySet() {
        return this.keySet;
    }

    private void setKeySet(Set<String> keySet) {
        this.keySet = keySet;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public void setRequestN(int reqN) {}

    @Override
    public String toString() {
        return "GetMessage{key='" + keySet + "'}";
    }

    @Override
    public MultiGetMsg clone() {
        return new MultiGetMsg(this);
    }
}