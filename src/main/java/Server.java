import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final List<String> allowedMethods = List.of(GET, POST);
    Request request = new Request();

    public void serverStart() {
        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedInputStream(socket.getInputStream());
                        final var out = new BufferedOutputStream(socket.getOutputStream())
                ) {
                    //System.out.println(request.returnRequest(in, out));
                    //System.out.println("query params: " + request.returnRequest(in, out).getQueryParams());
                    System.out.println("query param: " + request.returnRequest(in, out).getQueryParam("param2"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}