package client;

public class ClientInterface {
    // meter aqui as funções que vão fazer os prints e receber os valores das replys e dar print ao que eles significam

    public static void printRegisterUserReply(int reply , String user) {
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

