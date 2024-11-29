package messagesFormat;

import enums.Enums.commandType;
import enums.Enums.putCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PutReply implements MsgInterfaces.ServToCliMsg {
    private static final byte OPCODE = (byte) commandType.PUT.ordinal();
    private static final byte SUBCODE = (byte) putCommand.PUT.ordinal(); 
    private int reply;
    private String key;
    private long arrivalTimestamp;
    private String info;

    public PutReply() { }

    public PutReply(String key, long arrivalTimestamp) {
        this.reply = 0;
        this.key = key;
        this.arrivalTimestamp = arrivalTimestamp;
        this.info = "success";
    }

    public PutReply(String key, int reply, long arrivalTimestamp, String info) {
        this.reply = reply;
        this.key = key;
        this.arrivalTimestamp = arrivalTimestamp;
        this.info = info;
    }

    public PutReply(PutReply msg) {
        this.reply = msg.reply;
        this.key = msg.key;
        this.arrivalTimestamp = msg.arrivalTimestamp;
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
        dos.writeInt(reply);
        dos.writeUTF(key);
        dos.writeLong(arrivalTimestamp);
        dos.writeUTF(info);
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeInt(reply);
        dos.writeUTF(key);
        dos.writeLong(arrivalTimestamp);
        dos.writeUTF(info);
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.reply = dis.readInt();
        this.key = dis.readUTF();
        this.arrivalTimestamp = dis.readLong();
        this.info = dis.readUTF();
    }

    // Getters and Setters

    public int getReply() {
        return this.reply;
    }

    public String getInfo() {
        return this.info;
    }

    public String getKey() {
        return this.key;
    }

    public long getRequestedTimestamp(){
        return this.arrivalTimestamp;
    }

    private void setReply(int reply) {
        this.reply = reply;
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
        return "AuthReply{reply=" + reply + ", info='" + info + "'}";
    }

    @Override
    public AuthReply clone() {
        return new AuthReply(this.clone());
    }

    @Override
    public byte[] getResultInBytes() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultInBytes'");
    }
}
