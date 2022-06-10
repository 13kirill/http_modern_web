import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        final Server server = new Server();
        final var port = 9999;

        server.serverStart(port);
    }
}