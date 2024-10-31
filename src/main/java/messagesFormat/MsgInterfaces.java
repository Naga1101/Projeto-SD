package messagesFormat;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class MsgInterfaces {
    interface IMessage extends Cloneable {
        public void serialize(DataOutputStream dos) throws IOException;
        public void serializeWithoutFlush(DataOutputStream dos) throws IOException;
        public void deserialize(DataInputStream dis) throws IOException;
        public int getRequestN();
        public IMessage clone();
    }
    
    interface CliToServMsg extends IMessage {
        public void setRequestN(int reqN);
        @Override
        CliToServMsg clone();
    }
    
    interface ServToCliMsg extends IMessage {
        public byte[] getResultInBytes();
        @Override
        ServToCliMsg clone();
    };
}
