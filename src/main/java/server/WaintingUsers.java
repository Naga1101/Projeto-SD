package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.Timestamp;
import java.util.concurrent.*;

public class WaintingUsers {
    private Socket mySocket;
    private Timestamp timeOfArrival;

    public WaintingUsers(Socket mySocket) {
        this.mySocket = mySocket;
        this.timeOfArrival = new Timestamp(System.currentTimeMillis());
    }

    public Socket getMySocket() {
        return this.mySocket;
    }

    @Override
    public String toString() {
        return "WaintingUsers{" +
                "mySocket=" + mySocket +
                ", timeOfArrival=" + timeOfArrival +
                '}';
    }
}
