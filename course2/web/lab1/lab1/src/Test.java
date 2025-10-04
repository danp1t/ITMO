import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        String content = new Scanner(System.in).nextLine();
        String[] coors = content.split("&");
        Double coor_x = Double.parseDouble(coors[0].split("=")[1]);
        Double coor_y = Double.parseDouble(coors[1].split("=")[1]);
        Double coor_r = Double.parseDouble(coors[2].split("=")[1]);
    }
}
