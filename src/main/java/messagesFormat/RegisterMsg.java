package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import enums.Enums.autenticacao;

public class RegisterMsg implements MsgInterfaces.CliToServMsg {
    private static final byte OPCODE = (byte) autenticacao.REGISTER.ordinal(); 
    private String username;
    private String password;

    public RegisterMsg() {}

    public RegisterMsg(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public RegisterMsg(RegisterMsg msg) {
        this.username = msg.username;
        this.password = msg.password;
    }

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
    public int getRequestN() { // estava a dar erro sem isto
        return -1; 
    }

    @Override
    public void setRequestN(int reqN) {} // estava a dar erro sem isto

    @Override
    public String toString() {
        return "RegisterMessage{username='" + username + "', password='" + password + "'}";
    }

    @Override
    public RegisterMsg clone() {
        return new RegisterMsg(this);
    }
}