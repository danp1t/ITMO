import com.fastcgi.FCGIInterface;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        var fcgiInterface = new FCGIInterface();
        while (fcgiInterface.FCGIaccept() >= 0) {
            String requestBody = "";
            try {
                requestBody = readRequestBody();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }

            String content = generateResponse(requestBody);
            var httpResponse = """
                HTTP/1.1 200 OK
                Content-Type: text/html
                Content-Length: %d

                %s
                """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);

            System.out.println(httpResponse);
        }
    }

    private static String readRequestBody() throws IOException {
        FCGIInterface.request.inStream.fill();

        var contentLength = FCGIInterface.request.inStream.available();
        var buffer = ByteBuffer.allocate(contentLength);
        var readBytes = FCGIInterface.request.inStream.read(buffer.array(), 0, contentLength);

        var requestBodyRaw = new byte[readBytes];
        buffer.get(requestBodyRaw);
        buffer.clear();

        return new String(requestBodyRaw, StandardCharsets.UTF_8);
    }

    private static String generateResponse(String requestBody) {
        // Разбор данных из requestBody
        String[] params = requestBody.split("&");
        String formX = "", formY = "", formZ = "";

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                switch (keyValue[0]) {
                    case "form_x" -> formX = keyValue[1];
                    case "form_y" -> formY = keyValue[1];
                    case "form_z" -> formZ = keyValue[1];
                }
            }
        }

        // Генерация HTML-контента с таблицей
        return """
            <html>
                <head><title>Java FastCGI Hello World</title></head>
                <body>
                    <h1>Hello, World!</h1>
                    <h2>Submitted Data:</h2>
                    <table border="1">
                        <tr>
                            <th>Parameter</th>
                            <th>Value</th>
                        </tr>
                        <tr>
                            <td>form_x</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>form_y</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td>form_z</td>
                            <td>%s</td>
                        </tr>
                    </table>
                </body>
            </html>
            """.formatted(formX, formY, formZ);
    }
}