package lab6.main.java.client;

public class ClientMain {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        Client client = new Client(host, port);
        client.start();
    }
}