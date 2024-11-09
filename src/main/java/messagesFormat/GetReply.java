package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class GetReply implements MsgInterfaces.ServToCliMsg {
    private static final byte OPCODE = 2; 
    private byte[] reply;
    private String info; // error info?

    public AuthReply() {}

    public AuthReply(byte[] reply, String info) {
        this.reply = reply;
        this.info = info;
    }

    public AuthReply(AuthReply msg) {
        this.reply = msg.reply;
        this.info = msg.info;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeInt(reply);
        dos.writeUTF(info);   
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);
        dos.writeInt(reply); 
        dos.writeUTF(info);  
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.reply = dis.readInt();  
        this.info = dis.readUTF();    
    }

    // Getters and Setters

    public int getReply() {
        return this.reply;
    }

    public String getInfo() {
        return this.info;
    }

    private void setReply(byte[] reply) {
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
        return new GetReply(this);
    }

    @Override
    public byte[] getResultInBytes() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultInBytes'");
    }
}