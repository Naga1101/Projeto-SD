package messagesFormat;

import enums.Enums.commandType;
import enums.Enums.getCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class GetWhenMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = (byte) commandType.GET.ordinal();
    private static final byte SUBCODE = (byte) getCommand.GETWHEN.ordinal();
    private String key;
    private String keyCond;
    private byte[] valueCond;

    public GetWhenMsg() {}

    public GetWhenMsg(String key, String keyCond, byte[] valueCond) {
        this.key = key;
        this.keyCond = keyCond;
        this.valueCond = valueCond;
    }

    public GetWhenMsg(String key, String keyCond, String valueCond) {
        this.key = key;
        this.keyCond = keyCond;
        this.valueCond = valueCond.getBytes();
    }

    // Copy constructor
    public GetWhenMsg(GetWhenMsg msg) {
        this.key = msg.key;
        this.keyCond = msg.keyCond;
        this.valueCond = msg.valueCond != null ? Arrays.copyOf(msg.valueCond, msg.valueCond.length) : null;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeUTF(key);
        dos.writeUTF(keyCond);

        if (valueCond != null) {
            dos.writeInt(valueCond.length);  // Write length of valueCond
            dos.write(valueCond);            // Write the byte array itself
        } else {
            dos.writeInt(-1);                // Indicate a null valueCond
        }
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeUTF(key);
        dos.writeUTF(keyCond);

        if (valueCond != null) {
            dos.writeInt(valueCond.length);
            dos.write(valueCond);
        } else {
            dos.writeInt(-1);
        }
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.key = dis.readUTF();
        this.keyCond = dis.readUTF();

        int length = dis.readInt();
        if (length >= 0) {
            valueCond = new byte[length];
            dis.readFully(valueCond);
        } else {
            valueCond = null;  // Handle null case
        }
    }

    public String getKey() {
        return key;
    }

    public String getKeyCond() {
        return keyCond;
    }

    public byte[] getValueCond() {
        return valueCond != null ? Arrays.copyOf(valueCond, valueCond.length) : null;
    }

    private void setKey(String key) {
        this.key = key;
    }

    private void setKeyCond(String keyCond) {
        this.keyCond = keyCond;
    }

    private void setValueCond(byte[] valueCond) {
        this.valueCond = valueCond != null ? Arrays.copyOf(valueCond, valueCond.length) : null;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public String toString() {
        return "GetWhenMsg{key='" + key + "', keyCond='" + keyCond + "', valueCond=" + Arrays.toString(valueCond) + "}";
    }

    @Override
    public void setRequestN(int reqN) {}

    @Override
    public GetWhenMsg clone() {
        return new GetWhenMsg(this);
    }
}
