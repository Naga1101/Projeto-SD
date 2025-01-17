package messagesFormat;

import enums.Enums.commandType;
import enums.Enums.getCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MultiGetReply implements MsgInterfaces.ServToCliMsg {
    private static final byte OPCODE = (byte) commandType.GET.ordinal();
    private static final byte SUBCODE = (byte) getCommand.MULTIGET.ordinal();
    private long arrivalTimestamp;
    private Map<String, byte[]> reply;
    private String info; // error info?

    public MultiGetReply() {}

    public MultiGetReply(long arrivalTimestamp, Map<String, byte[]> reply) {
        this.arrivalTimestamp = arrivalTimestamp;
        this.reply = reply;
        this.info = "";
    }

    public MultiGetReply(long arrivalTimestamp, Map<String, byte[]> reply, String info) {
        this.arrivalTimestamp = arrivalTimestamp;
        this.reply = reply;
        this.info = info;
    }

    public MultiGetReply(MultiGetReply msg) {
        this.arrivalTimestamp = msg.arrivalTimestamp;
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
        dos.writeInt(reply.size());
        for (Map.Entry<String, byte[]> entry : reply.entrySet()) {
            dos.writeUTF(entry.getKey());
            byte[] value = entry.getValue();
            dos.writeInt(value.length);
            dos.write(value);
        }
        dos.writeUTF(info);
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeLong(arrivalTimestamp);
        dos.writeInt(reply.size());
        for (Map.Entry<String, byte[]> entry : reply.entrySet()) {
            dos.writeUTF(entry.getKey());
            byte[] value = entry.getValue();
            dos.writeInt(value.length);
            dos.write(value);
        }
        dos.writeUTF(info);
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.arrivalTimestamp = dis.readLong();
        int mapSize = dis.readInt();
        this.reply = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            String key = dis.readUTF();
            int valueLength = dis.readInt();
            byte[] value = new byte[valueLength];
            dis.readFully(value);
            this.reply.put(key, value);
        }
        this.info = dis.readUTF();
    }

    // Getters and Setters

    public Map<String, byte[]> getReply() {
        return this.reply;
    }

    public String getInfo() {
        return this.info;
    }

    private void setReply(Map<String, byte[]> reply) {
        this.reply = reply;
    }

    private void setInfo(String info) {
        this.info = info;
    }

    public long getRequestedTimestamp(){
        return this.arrivalTimestamp;
    }

    @Override
    public int getRequestN() {
        return -1; 
    }

    @Override
    public String toString() {
        return "AuthReply{reply=" + reply + ", info='" + info + "'}";
    }

    @Override
    public MultiGetReply clone() {
        return new MultiGetReply(this.clone());
    }

    @Override
    public byte[] getResultInBytes() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultInBytes'");
    }
}