import com.fastcgi.FCGIInterface;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        var fcgiInterface = new FCGIInterface();
        while (fcgiInterface.FCGIaccept() >= 0) {
            try {
                // Чтение строки запроса
                String requestQuery = readRequestQuery();

                // Обработка данных
                System.out.println("Received data: " + requestQuery);

                // Формируем ответ
                String content = """
                        <head><title>Java FastCGI Response</title></head>
                        <body><h1>Data Received</h1><p>%s</p></body>
                        </html>""".formatted(requestQuery);

                // Формирование HTTP-ответа
                String httpResponse = """
                        HTTP/1.1 200 OK
                        Content-Type: text/html
                        Content-Length: %d
                    
                        %s
                        """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);

                System.out.println(httpResponse);

                // Отправка ответа обратно через FastCGI
                FCGIInterface.request.outStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String readRequestQuery() throws IOException {
        FCGIInterface.request.inStream.fill();

        // Чтение заголовков
        StringBuilder headers = new StringBuilder();
        while (true) {
            int ch = FCGIInterface.request.inStream.read();
            if (ch == -1 || ch == '\0') break;
            headers.append((char) ch);
        }

        // Извлечение строки запроса из заголовков
        String requestLine = headers.toString().split("\n")[0];
        return requestLine.split(" ")[1]; // Получаем только часть с параметрами
    }
}