package messagesFormat;

import enums.Enums.commandType;
import enums.Enums.putCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MultiPutReply implements MsgInterfaces.ServToCliMsg {
    private static final byte OPCODE = (byte) commandType.PUT.ordinal();
    private static final byte SUBCODE = (byte) putCommand.MULTIPUT.ordinal(); 
    private int reply;
    private long arrivalTimestamp;
    private String info;


    public MultiPutReply(long arrivalTimestamp) {
        this.reply = 0;
        this.arrivalTimestamp = arrivalTimestamp;
        this.info = "succes";
    }

    public MultiPutReply(int reply, long arrivalTimestamp, String info) {
        this.reply = reply;
        this.arrivalTimestamp = arrivalTimestamp;
        this.info = info;
    }

    public MultiPutReply(MultiPutReply msg) {
        this.reply = msg.reply;
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
        dos.writeLong(arrivalTimestamp);
        dos.writeUTF(info);
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeByte(SUBCODE);
        dos.writeInt(reply);
        dos.writeLong(arrivalTimestamp);
        dos.writeUTF(info);
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.reply = dis.readInt();
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

    public long getArrivalTimestamp(){
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
