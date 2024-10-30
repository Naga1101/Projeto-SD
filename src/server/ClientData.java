package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.*;

public class ClientData {
    private String username;
    private String password;
    private boolean isOnline;

    public ClientData(String username, String password){
        this.username = username;
        this.password = password;
        this.isOnline = false;
    }

    // setters e getters

    public void setUserOnline(){
        this.isOnline = true;
    }

    public void setUserOffline(){
        this.isOnline = false;
    }

    public String getPassword(){ 
        return password;
    }

    public boolean isOnline(){ 
        return isOnline;
    }

    // equals

    public boolean verifyCreds(String password){
        return this.password.equals(password);
    }
}
