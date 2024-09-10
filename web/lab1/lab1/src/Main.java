import com.fastcgi.FCGIInterface;
//24712

//24821
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        var fcgiInterface = new FCGIInterface();
        while (fcgiInterface.FCGIaccept() >= 0) {
            try {
                // Чтение тела запроса
                String requestBody = readRequestBody();
                //coor_x=1&coor_y=-5&coor_r=2
                var coordinates = requestBody.split("&");
                int x = Integer.parseInt(coordinates[0].split("=")[1]);
                int y = Integer.parseInt(coordinates[1].split("=")[1]);
                int r = Integer.parseInt(coordinates[2].split("=")[1]);

                if (x >= 0 && y >= 0) {
                    if (r <= x*x + y*y)  {
                        requestBody = "Попал";
                    }
                    else ()
                }
                else if (x <= 0 && y >= 0) {
                    requestBody = "Вторая четверть";
                }
                else if (x <= 0 && y <= 0) {
                    requestBody = "Третья четверть";
                }
                else if (x >= 0 && y <= 0) {
                    requestBody = "Четвертая четверть";
                }


                // Обработка данных (в данном случае просто выводим их)
                System.out.println("Received data: " + requestBody);

                // Формируем ответ
                String content = """
                        <head><title>Java FastCGI Response</title></head>
                        <body><h1>Data Received</h1><p>%s</p></body>
                        </html>""".formatted(requestBody);

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
}