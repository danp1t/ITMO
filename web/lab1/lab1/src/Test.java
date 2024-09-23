import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Введите координату x: ");
        double x = in.nextDouble();
        System.out.print("Введите координату y: ");
        double y = in.nextDouble();
        System.out.print("Введите координату r: ");
        double r = in.nextDouble();
        System.out.println(Main.getStatus(x, y, r));
    }
}
