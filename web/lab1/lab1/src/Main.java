import com.fastcgi.FCGIInterface;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static List<Result> results = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        var fcgiInterface = new FCGIInterface();
        while (fcgiInterface.FCGIaccept() >= 0) {
            LocalDateTime now = LocalDateTime.now();
            long startTime = System.nanoTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            FCGIInterface.request.inStream.fill();
            String content = (String) FCGIInterface.request.params.get("QUERY_STRING");
            //coor_x=1&coor_y=0&coor_r=
            Double coor_x = 0.0;
            Double coor_y = 0.0;
            Double coor_r = 0.0;
            try {
                String[] coors = content.split("&");
                if (coors.length != 3 || !(coors[0].split("=")[0].equals("coor_x")) || !(coors[1].split("=")[0].equals("coor_y")) || !(coors[2].split("=")[0].equals("coor_r"))
                || (coors[0].split("=")[1] == null) || (coors[1].split("=")[1] == null) || (coors[2].split("=")[1] == null)) {
                    throw new IllegalArgumentException("Неверный формат запроса... Ты кого собираешься надурить, Мамкин Хакер?");
                }
                coor_x = Double.parseDouble(coors[0].split("=")[1]);
                coor_y = Double.parseDouble(coors[1].split("=")[1]);
                coor_r = Double.parseDouble(coors[2].split("=")[1]);


                boolean status = getStatus(coor_x, coor_y, coor_r);
                String statusMessage = status ? "Попал" : "Не попал";

                // Добавляем результат в список
                long elapsedTime = (System.nanoTime() - startTime);
                results.add(new Result(coor_x, coor_y, coor_r, statusMessage, now.format(formatter), elapsedTime));

                // Формируем HTML-таблицу
                StringBuilder tableBuilder = new StringBuilder();
                 // Время работы скрипта
                tableBuilder.append("<table border='1' id='resultTable123'><tr><th>№</th><th>x</th><th>y</th><th>r</th><th>Результат</th><th>Текущее время</th><th>Время работы (нс)</th></tr>");
                for (int i = 0; i < results.size(); i++) {
                    Result result = results.get(i);
                    if (result.getStatus().equals("Попал")) {

                    tableBuilder.append("<tr>")
                            .append("<td>").append(i + 1).append("</td>")
                            .append("<td>").append(result.getX()).append("</td>")
                            .append("<td>").append(result.getY()).append("</td>")
                            .append("<td>").append(result.getR()).append("</td>")
                            .append("<td id='resultStatus' class='green'>").append(result.getStatus()).append("</td>")
                            .append("<td>").append(result.getNow()).append("</td>") // Текущее время
                            .append("<td>").append(result.getTime()).append("</td>") // Время работы
                            .append("</tr>");
                    }
                    else {
                        tableBuilder.append("<tr>")
                                .append("<td>").append(i + 1).append("</td>")
                                .append("<td>").append(result.getX()).append("</td>")
                                .append("<td>").append(result.getY()).append("</td>")
                                .append("<td>").append(result.getR()).append("</td>")
                                .append("<td id='resultStatus' class='red'>").append(result.getStatus()).append("</td>")
                                .append("<td>").append(result.getNow()).append("</td>") // Текущее время
                                .append("<td>").append(result.getTime()).append("</td>") // Время работы
                                .append("</tr>");
                    }

                }
                tableBuilder.append("</table>");

                content = tableBuilder.toString();


                var httpResponse = """
                        HTTP/1.1 200 OK
                        Content-Type: text/html; charset=UTF-8
                        Content-Length: %d
                                            
                                            
                        %s
                                            
                        """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);
                System.out.println(httpResponse);
            }
            catch (IllegalArgumentException e) {
                sendErrorResponse(e.getMessage());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                sendErrorResponse("Неверный формат запроса... Ты кого собираешься надурить, Мамкин Хакер?");
            }
        }
    }

    public static boolean getStatus(double coor_x, double coor_y, double coor_r) {
        //Разделить на координатные четверти
        //Первая четверть
        if (coor_x > 0 && coor_y > 0) {
            return false;
        }
        //Вторая четверть
        else if (coor_x <= 0 && coor_y >= 0) {
            if (coor_x*coor_x + coor_y*coor_y <= coor_r*coor_r) {
                return true;
            }
            else {
                return false;
            }
        }
        //Третья четверть
        else if (coor_x <= 0 && coor_y <= 0) {
            if (-coor_x <= coor_r && -coor_y <= coor_r) {
                return true;
            }
            else {
                return false;
            }
        }
        //Четвертая четверть
        else if (coor_x >= 0 && coor_y <= 0) {
            if (Math.abs(coor_x) + Math.abs(coor_y) <= coor_r) {
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

    private static void sendErrorResponse(String message){
        message = URLDecoder.decode(message, StandardCharsets.UTF_8);

        var httpResponse = """
                    HTTP/1.1 400 Bad Request
                    Content-Type: text/plain; charset=UTF-8
                    Content-Length: %d
                    
                    
                    %s
                    
                    """.formatted(message.getBytes(StandardCharsets.UTF_8).length, message);
        System.out.println(httpResponse);
    }

}