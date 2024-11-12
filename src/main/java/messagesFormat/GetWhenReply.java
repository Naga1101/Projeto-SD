package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import enums.Enums.command;

public class GetWhenReply implements MsgInterfaces.ServToCliMsg {
    private static final byte OPCODE = (byte) command.GETWHEN.ordinal();
    private byte[] reply;
    private String info;

    public GetWhenReply() {}

    public GetWhenReply(byte[] reply, String info) {
        this.reply = reply != null ? Arrays.copyOf(reply, reply.length) : null;
        this.info = info;
    }

    public GetWhenReply(GetWhenReply msg) {
        this.reply = msg.reply != null ? Arrays.copyOf(msg.reply, msg.reply.length) : null;
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
            reply = null;
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
        return "GetWhenReply{reply=" + Arrays.toString(reply) + ", info='" + info + "'}";
    }

    @Override
    public GetWhenReply clone() {
        return new GetWhenReply(this.clone());
    }

    @Override
    public byte[] getResultInBytes() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultInBytes'");
    }
}
