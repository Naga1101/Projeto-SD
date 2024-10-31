package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class LoginMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = 1; 
    private String username;
    private String password;

    public LoginMsg() {}

    public LoginMsg(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginMsg(LoginMsg msg) {
        this.username = msg.username;
        this.password = msg.password;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE); 
        dos.writeUTF(username); 
        dos.writeUTF(password); 
    }

    @Override
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE); 
        dos.writeUTF(username); 
        dos.writeUTF(password); 
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        this.username = dis.readUTF(); 
        this.password = dis.readUTF(); 
    }

    ///// Getters e Setters

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getRequestN() {
        return -1;
    }

    @Override
    public void setRequestN(int reqN) {}

    @Override
    public String toString() {
        return "LoginMessage{username='" + username + "', password='" + password + "'}";
    }

    @Override
    public LoginMsg clone() {
        return new LoginMsg(this);
    }
}