package server;

public class TestClient {
    public static void main(String[] args) {
        ClientData user = new ClientData("testUser", "testPassword");
        System.out.println("Username: " + user.getPassword());
    }
}
