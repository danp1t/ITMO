import com.fastcgi.FCGIInterface;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var fcgiInterface = new FCGIInterface();
        while (fcgiInterface.FCGIaccept() >= 0) {
            FCGIInterface.request.inStream.fill();
            String content = (String) FCGIInterface.request.params.get("QUERY_STRING");
            //coor_x=1&coor_y=0&coor_r=
            String[] coors = content.split("&");
            Integer coor_x = Integer.parseInt(coors[0].split("=")[1]);
            Integer coor_y = Integer.parseInt(coors[1].split("=")[1]);
            Integer coor_z = Integer.parseInt(coors[2].split("=")[1]);
            var httpResponse = """
                    HTTP/1.1 200 OK
                    Content-Type: text/plain
                    Content-Length: %d
                    
                    
                    %s
                    
                    """.formatted(content.length(), content);
            System.out.println(httpResponse);
        }
    }

}