package messagesFormat;

import enums.Enums.commandType;
import enums.Enums.getCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class GetReply implements MsgInterfaces.ServToCliMsg {
    private static final byte OPCODE = (byte) commandType.GET.ordinal();
    private static final byte SUBCODE = (byte) getCommand.GET.ordinal();
    private long arrivalTimestamp;
    private String key; 
    private byte[] reply;
    private String info; // error info?

    public GetReply() {}

    public GetReply(String key, long arrivalTimestamp, byte[] reply) {
        this.arrivalTimestamp = arrivalTimestamp;
        this.key = key;
        this.reply = reply;
        this.info = "";
    }

    public GetReply(String key, long arrivalTimestamp, byte[] reply, String info) {
        this.arrivalTimestamp = arrivalTimestamp;
        this.key = key;
        this.reply = reply;
        this.info = info;
    }

    public GetReply(GetReply msg) {
        this.arrivalTimestamp = msg.arrivalTimestamp;
        this.key = msg.key;
        this.reply = msg.reply;
        this.info = msg.info;
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
        dos.writeLong(arrivalTimestamp);
        dos.writeUTF(key);

        if (reply != null) {
            dos.writeInt(reply.length);  // Write the length of reply
            dos.write(reply);            // Write the reply byte array
        } else {
            dos.writeInt(-1);            // Indicate a null reply
        }

        dos.writeUTF(info);
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeLong(arrivalTimestamp);
        dos.writeUTF(key);

        if (reply != null) {
            dos.writeInt(reply.length);
            dos.write(reply);
        } else {
            dos.writeInt(-1);
        }

        dos.writeUTF(info);
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.arrivalTimestamp = dis.readLong();
        this.key = dis.readUTF();
        int length = dis.readInt();
        if (length >= 0) {
            reply = new byte[length];
            dis.readFully(reply);
        } else {
            reply = null;  // Handle null case
        }

        this.info = dis.readUTF();
    }

    public byte[] getReply() {
        return reply != null ? Arrays.copyOf(reply, reply.length) : null;
    }

    public String getInfo() {
        return this.info;
    }

    public long getArrivalTimestamp(){
        return this.arrivalTimestamp;
    }

    private void setReply(byte[] reply) {
        this.reply = reply != null ? Arrays.copyOf(reply, reply.length) : null;
    }

    private void setInfo(String info) {
        this.info = info;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public String toString() {
        return "GetReply{timestamp=" + arrivalTimestamp 
            + ", key=" + key 
            + ", reply=" + (reply != null ? Arrays.toString(reply) : "null") 
            + ", info='" + info + "'}";
    }    

    @Override
    public GetReply clone() {
        return new GetReply(this);
    }

    @Override
    public byte[] getResultInBytes() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultInBytes'");
    }
}