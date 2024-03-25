package org.example;
import java.lang.Math;;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("Введите количество равномерных промежутков: ");
        Random random = new Random();
        Scanner scanner = new Scanner(System.in);
        Integer n = scanner.nextInt();
        Double[][] array = new Double[n][2];
        //Начала промежутка
        double start = 1;
        double start_i = 1;
        //Конец промежутка
        double end = Math.E;
        double delta = (end - start) / n;


        double end_i = 1;
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
        //Метод прямоугольников
        double sum = 0;
        //Выбираем левую точку отрезка
        for (int i = 0; i < n; i++) {
            double f = function(array[i][0]);
            sum = sum + f*delta;
        }
        System.out.println("Площадь при выборе левых точек: " + sum);
        sum = 0;
        //Выбираем правую точку отрезка
        for (int i = 0; i < n; i++) {
            double f = function(array[i][1]);
            sum = sum + f*delta;
        }
        System.out.println("Площадь при выборе правых точек: " + sum);
        sum = 0;
        //Выбираем рандомную точку из отрезка
        for (int i = 0; i < n; i++) {
            double minRange = array[i][0];
            double maxRange = array[i][1];
            double randomNumInRange = minRange + (maxRange - minRange) * random.nextDouble();
            double f = function(randomNumInRange);
            sum = sum + f*delta;
        }
        System.out.println("Площадь при выборе рандомных точек из отрезка: " + sum);


        //
    }

    private static double function(double x){
        return 1 / Math.sqrt(x);
    }
}