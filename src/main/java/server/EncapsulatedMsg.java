package server;

import messagesFormat.MsgInterfaces.IMessage;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EncapsulatedMsg<T extends IMessage>{
    private String user;
    private T message;

    //to be filled in deserialize
    EncapsulatedMsg () {
    }

    EncapsulatedMsg(T msg) {
        this.message = msg;
    }

    EncapsulatedMsg(String user, T msg) {
        this.user = user;
        this.message = msg;
    }

    public String getUser() {
        return this.user;
    }

    public T getMessage() {
        return this.message;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setMessage(T msg) {
        this.message = msg;
    }

    public void serialize(DataOutputStream dos) throws IOException{
        this.message.serializeWithoutFlush(dos);
        dos.writeUTF(user);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException {
        this.message.deserialize(dis);
        this.setUser(dis.readUTF());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User: " + this.user);
        sb.append("Command: " + this.message.toString());
        return sb.toString();
    }

    @Override
    public EncapsulatedMsg<T> clone() {
        return new EncapsulatedMsg(this.user, this.message.clone());
    }
}
