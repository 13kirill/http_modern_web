import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {



//    private Socket socket;
//
//    public Server(Socket socket){
//        this.socket = socket;
//    }
//
//    public Server(){
//
//    }

    ExecutorService executorService = Executors.newFixedThreadPool(64);

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");


    public void requestClassic(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    public void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public void requestFile(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    public void serverStart(int port) {

        System.out.println("Server started");
        try (var serverSocket = new ServerSocket(port)) {
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    //executorService.submit(listen(socket));
                    listen(socket);
                    System.out.println("New request completed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen (Socket socket) throws IOException {
        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final var out = new BufferedOutputStream(socket.getOutputStream());

        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            socket.close();
            // just close socket
        }

        final var path = parts[1];
        if (!validPaths.contains(path)) {
            badRequest(out);
        }

        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            requestClassic(out, filePath, mimeType);
        }
        requestFile(out, filePath, mimeType);
    }

//    @Override
//    public void run() {
//        try {
//            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            final var out = new BufferedOutputStream(socket.getOutputStream());
//
//            // read only request line for simplicity
//            // must be in form GET /path HTTP/1.1
//            final var requestLine = in.readLine();
//            final var parts = requestLine.split(" ");
//
//            if (parts.length != 3) {
//                socket.close();
//                // just close socket
//            }
//
//            final var path = parts[1];
//            if (!validPaths.contains(path)) {
//                badRequest(out);
//            }
//
//            final var filePath = Path.of(".", "public", path);
//            final var mimeType = Files.probeContentType(filePath);
//
//            // special case for classic
//            if (path.equals("/classic.html")) {
//                requestClassic(out, filePath, mimeType);
//            }
//            requestFile(out, filePath, mimeType);
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }
}