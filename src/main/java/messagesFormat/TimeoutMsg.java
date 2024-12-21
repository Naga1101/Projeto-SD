package messagesFormat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import enums.Enums.commandType;
import messagesFormat.MsgInterfaces.CliToServMsg;

public class TimeoutMsg implements CliToServMsg {
    private static final byte OPCODE = (byte) commandType.TIMEOUT.ordinal();

    public TimeoutMsg() {}

    @Override
    public byte getOpcode() {
        return OPCODE;
    }

    @Override
    public byte getSubcode() {
        return 0;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {

    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public void setRequestN(int reqN) {}

    @Override
    public String toString() {
        return "TimeoutMsg";
    }

    @Override
    public TimeoutMsg clone() {
        return new TimeoutMsg();
    }
}