package server;

import java.util.*;

public class DBInterface {
    public interface DB {
        void put(String key, byte[] data);
        void multiPut(Map<String, byte[]> pairs);
        byte[] get(String key);
        Map<String, byte[]> multiGet(Set<String> keys);
        byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException;
    }
}
