import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Request {

    private String method;
    private List<String> headers;
    private String body;
    private String path;
    Server server;

    public Request(String method, List<String> headers, String body, String path) {
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.path = path;
    }

    public Request() {
    }

    public Request returnRequest(BufferedInputStream in, BufferedOutputStream out) throws IOException {

        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = server.indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            server.badRequest(out);
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            server.badRequest(out);
        }

        final var method = requestLine[0];
        if (!server.allowedMethods.contains(method)) {
            server.badRequest(out);
        }
        //System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            server.badRequest(out);
        }
        System.out.println(path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = server.indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            server.badRequest(out);
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        //System.out.println(headers);

        // для GET тела нет
        String body = null;
        if (!method.equals(server.GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = server.extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                body = new String(bodyBytes);
                //System.out.println(body);
            }

        }
        return new Request(method, headers, body, path);
    }

    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(path, StandardCharsets.UTF_8);
    }

public List<NameValuePair> getQueryParam(String name) {
        return getQueryParams().stream()
                .filter(x -> x.getName().equals(name))
                .collect(Collectors.toList());
}

    public String toString() {
        return "метод: " + method + '\n' + "заголовки: " + headers + '\n' + "тело: " + body + '\n' + "путь: " + path;
    }
}
