package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import enums.Enums.getCommand;

public class GetReply implements MsgInterfaces.ServToCliMsg {
    private static final byte OPCODE = (byte) getCommand.GET.ordinal();
    private byte[] reply;
    private String info; // error info?

    public GetReply() {}

    public GetReply(byte[] reply, String info) {
        this.reply = reply;
        this.info = info;
    }

    public GetReply(GetReply msg) {
        this.reply = msg.reply;
        this.info = msg.info;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeByte(OPCODE);

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
        return "GetReply{reply=" + Arrays.toString(reply) + ", info='" + info + "'}";
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