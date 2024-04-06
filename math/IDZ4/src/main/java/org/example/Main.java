package org.example;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Main {
    public static Expression function;
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите функцию в формате f(x) = выражение (для выхода введите 'exit'): ");
        String input = scanner.nextLine();
        String[] parts = input.split("=");
        if (parts.length != 2) {
            System.out.println("Неверный формат функции. Попробуйте снова.");
        }
        ;
        String functionExpression = parts[1].trim();


        function = new ExpressionBuilder(functionExpression).variables("x").build();



        System.out.print("Введите количество равномерных промежутков: ");

        String line = scanner.nextLine();
        Integer n = Integer.parseInt(line);
        long startTime = System.currentTimeMillis();
        //Начала промежутка
        System.out.print("Введите начало промежутка интегрирования: ");
        line = scanner.nextLine();
        Expression expression = new ExpressionBuilder(line).build();

        double start = expression.evaluate();
        System.out.print("Введите конец промежутка интегрирования: ");
        line = scanner.nextLine();
        expression = new ExpressionBuilder(line).build();

        Double end = expression.evaluate();

        //Метод прямоугольников
        //Выбираем левую точку отрезка
        System.out.println("Площадь при выборе левых точек: " + rectangleMethodLeft(n, getArray(n, start, end), getDelta(n, start, end)));

        System.out.println("Площадь при выборе правых точек: " + rectangleMethodRight(n, getArray(n, start, end), getDelta(n, start, end)));
        //Выбираем рандомную точку из отрезка
        System.out.println("Площадь при выборе рандомных точек из отрезка: " + rectangleMethodRandom(n, getArray(n, start, end), getDelta(n, start, end)));


        //Метод трапеций
        System.out.println("Площадь при рассчете с помошью метода трапеции: " + trapezoidMethod(n, getArray(n, start, end), getDelta(n, start, end)));

        //Метод Симпсона
        System.out.println("Площадь при рассчете с помошью метода Симсона: " + simsonMethod(n, getArray(n, start, end), getDelta(n, start, end), end));
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Время выполнения алгоритма: " + duration + " мс");
    }

    private static double function(double x){
        return 1 / Math.sqrt(x);
    }

    private static double rectangleMethodLeft(int n, Double[][] array, double delta){
        double sum = 0;
        //Выбираем левую точку отрезка
        for (int i = 0; i < n; i++) {
            function.setVariable("x", array[i][0]);
            double f = function.evaluate();
            sum = sum + f*delta;
        }
        return sum;
    }

    private static double rectangleMethodRight(int n, Double[][] array, double delta){
        double sum = 0;
        //Выбираем правую точку отрезка
        for (int i = 0; i < n; i++) {

            function.setVariable("x", array[i][1]);
            double f = function.evaluate();
            sum = sum + f*delta;
        }
        return sum;
    }

    private static double rectangleMethodRandom(int n, Double[][] array, double delta){
        double sum = 0;
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            double minRange = array[i][0];
            double maxRange = array[i][1];
            double randomNumInRange = minRange + (maxRange - minRange) * random.nextDouble();
            function.setVariable("x", randomNumInRange);
            double f = function.evaluate();
            sum = sum + f*delta;
        }
        return sum;
    }
    private static double trapezoidMethod(int n, Double[][] array, double delta){
        double sum = 0;
        for (int i=0; i < n; i++) {
            double f_i_1 = array[i][0];
            double f_i = array[i][1];
            function.setVariable("x", f_i);
            f_i = function.evaluate();
            function.setVariable("x", f_i_1);
            f_i_1 = function.evaluate();
            sum = sum + (f_i + f_i_1) * (delta / 2);
        }
        return sum;
    }
    private static double simsonMethod(int n, Double[][] array, double delta, double end){
        double sum = 0;
        for (int i=0; i < n; i++) {
            double f_i_1 = array[i][0];
            double f_i = array[i][1];
            function.setVariable("x", (f_i + f_i_1) / 2);
            double f_1 = function.evaluate();
            function.setVariable("x", f_i);
            double f_2 = function.evaluate();
            function.setVariable("x", f_i_1);
            double f_3 = function.evaluate();
            sum = sum + (f_3 + 4*f_1 + f_2) * delta / 6;
        }
        return sum;
    }
    private static Double[][] getArray(int n, double start, double end){
        Double[][] array = new Double[n][2];
        double delta = (end - start) / n;
        double start_i = start;
        double end_i;
        for (int i=0; i < n; i++){
            Double part[] = new Double[2];
            part[0] = start_i;
            end_i = start_i + (delta);

            if (i == (n-1)) {
                part[1] = end;
            }
            else {
                part[1] = end_i;
            }
            start_i = end_i;
            array[i] = part;
        }
        return array;
    }
    private static double getDelta(int n, double start, double end){
        return (end - start) / n;
    }
}