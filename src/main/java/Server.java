
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

class Server {

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    final int port = 9999;

    public void serverStart() {

        System.out.println("Server started");
        try (var serverSocket = new ServerSocket(port)) {
            while (true) {

                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final var out = new BufferedOutputStream(socket.getOutputStream());

                ) { connect(socket, in, out); }
                System.out.println("New request completed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(Socket socket, BufferedReader in, BufferedOutputStream out) throws IOException {

        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            socket.close();
            //continue;
        }

        final var path = parts[1];
        if (!validPaths.contains(path)) {
            badRequest(out);
            //socket.close();
            //continue;
        }

        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            requestClassic(out, filePath, mimeType);
        }
        requestFile(out, filePath, mimeType);
    }

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

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write(("HTTP/1.1 400 Bad Request\r\n" +
                "Content-Length: \r\n" +
                "Connection: close\r\n +" +
                "\r\n").getBytes());
        out.flush();
    }
}