package server;

import server.ClientData;
import server.UsersAuthenticator;

public class TestAuthenticator {
    public static void main(String[] args) {
        try {
            String test1 = "testUser1";
            String test2 = "testUser2";

            UsersAuthenticator listUsers = new UsersAuthenticator();
    
            int reply = listUsers.registerUser(test1, "testPassword");
            printRegisterUserReply(reply, test1);
            reply = listUsers.registerUser(test1, "testPassword");
            printRegisterUserReply(reply, test1);
            reply = listUsers.registerUser(test2, "testPassword");
            printRegisterUserReply(reply, test2);
    
            System.out.println(listUsers);

            reply = listUsers.logUserIn(test1, "testPassword");
            printRegisterUserReply(reply, test1);
            reply = listUsers.logUserIn(test1, "testPassword");
            printRegisterUserReply(reply, test1);
            reply = listUsers.logUserIn(test2, "test");
            printRegisterUserReply(reply, test2);

            System.out.println(listUsers);

            reply = listUsers.logUserIn("testUser", "testPassword");
            printRegisterUserReply(reply, "testUser");
            reply = listUsers.logUserIn(test2, "testPassword");
            printRegisterUserReply(reply, test2);

            System.out.println(listUsers);

            reply = listUsers.logUserOut(test1);
            printRegisterUserReply(reply, test1);

            System.out.println(listUsers);

        } catch (Exception e) {
            e.printStackTrace(); // Print the exception to diagnose issues
        }
    }

    private static void printRegisterUserReply(int reply , String user) {
        switch (reply) {
            case 0:
                System.out.println("User " + user + " já existe.\n");
                break;
            case 1:
                System.out.println("User " + user + " registado com sucesso.\n");
                break;
            case 2:
                System.out.println("Acesso validado ao user " + user + ", encontra-se agora online.\n");
                break;
            case 3:
                System.out.println("User " + user + " desconectou-se, agora está offline.\n");
                break;
            case 4: 
                System.out.println("User " + user + " não existe.\n");
                break;
            case 5: 
                System.out.println("User " + user + " já se encontra online.\n");
                break;
            case 6: 
                System.out.println("Credenciais do user " + user + " erradas.\n");
                break;
            default:
                System.out.println("Unknown response.\n");
                break;
        }
    }
}

/**
 * Para compilar e correr estes testes é preciso utilizar estes comandos por ordem no terminal aberto na pasta src
 * javac -d out -sourcepath main/java main/java/server/*.java
 * javac -d out -cp out -sourcepath test/java test/java/server/TestAuthenticator.java 
 * java -cp out server.TestAuthenticator
 */