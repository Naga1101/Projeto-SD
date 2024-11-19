package server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UsersAuthenticator { // flags entre 0-9 são para funções relativas ao autenticador

    private HashMap<String, ClientData> usersList;
    private ReentrantReadWriteLock lockUserAuth;
    private final Lock readLock;
    private final Lock writeLock;
    private final Condition condUserAuth;

    public UsersAuthenticator() {
        usersList = new HashMap<>();
        lockUserAuth = new ReentrantReadWriteLock();
        readLock = lockUserAuth.readLock();
        writeLock = lockUserAuth.writeLock();
        condUserAuth = writeLock.newCondition();
    }

    // getters e setter

    public ClientData getUserOnline(String name) { // como ele só vem buscar um user se ele tiver passado as verificações das credenciais não é preciso verificar se existe
        readLock.lock();
        try {
            return usersList.get(name); // Simplified return
        } finally {
            readLock.unlock();
        }
    }

    private void setUserOnline(ClientData userLoggingIn) {
        writeLock.lock();
        try {
            userLoggingIn.setUserOnline();
        } finally {
            writeLock.unlock();
        }
    }

    ///////////////////////////// funções para tratar de criar/login/logout de um user /////////////////////////////

    public int registerUser(String name, String password) { // dá return a uma flag que indica se o user se registou ou não com sucesso
        writeLock.lock();
        try {
            if (usersList.containsKey(name)) return 0; // 0 se o user já existir

            ClientData newUser = new ClientData(name, password);
            usersList.put(name, newUser);
            return 1; // 1 se o user for adicionado com sucesso      
        } finally {
            writeLock.unlock();
        }
    }

    public int logUserIn(String name, String password) {
        writeLock.lock();
        try {
            if (!usersList.containsKey(name)) return 4; // 4 se o user não existir ou utilizar o 6?

            ClientData userLoggingIn = usersList.get(name);

            if (userLoggingIn.isOnline()) return 5; // 5 se o user já se encontrar online
            
            if (!userLoggingIn.verifyCreds(password)) return 6; // 6 se a password não estiver correta

            //setUserOnline(userLoggingIn);
            userLoggingIn.setUserOnline();
            return 2; // 2 se os credenciais forem verificados com sucesso      
        } finally {
            writeLock.unlock();
        }
    }

    public int logUserOut(String name) {
        writeLock.lock();
        try {
            ClientData userLoggingOff = usersList.get(name);
            if(userLoggingOff != null) {
                userLoggingOff.setUserOffline();
            }
            return 3; // 3 quando o user dá log out
        } finally {
            writeLock.unlock();
        }
    }

    // toString do map de users
    @Override
    public String toString() {
        readLock.lock();
        try {
            StringBuilder sb = new StringBuilder("UsersAuthenticator Users:\n");
            for (Map.Entry<String, ClientData> entry : usersList.entrySet()) {
                String username = entry.getKey();
                ClientData clientData = entry.getValue();
                sb.append("Username: ").append(username)
                  .append(", Password: ").append(clientData.getPassword()) // Use getter for password
                  .append(", Online: ").append(clientData.isOnline()) // Use getter for isOnline
                  .append("\n");
            }
            return sb.toString();
        } finally {
            readLock.unlock();
        }
    }
}