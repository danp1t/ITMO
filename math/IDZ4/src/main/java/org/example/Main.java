package org.example;
import java.lang.Math;
import java.util.Random;
import java.util.Scanner;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.print("Введите количество равномерных промежутков: ");
        Scanner scanner = new Scanner(System.in);
        Integer n = scanner.nextInt();
        //Начала промежутка
        double start = 1;

        //Конец промежутка
        double end = Math.E;


        //Метод прямоугольников
        //Выбираем левую точку отрезка
        System.out.println("Площадь при выборе левых точек: " + rectangle_method_left(n, getArray(n, start, end), getDelta(n, start, end)));

        System.out.println("Площадь при выборе правых точек: " + rectangle_method_right(n, getArray(n, start, end), getDelta(n, start, end)));
        //Выбираем рандомную точку из отрезка
        System.out.println("Площадь при выборе рандомных точек из отрезка: " + rectangle_method_random(n, getArray(n, start, end), getDelta(n, start, end)));


        //Метод трапеций
        System.out.println("Площадь при рассчете с помошью метода трапеции: " + trapezoid_method(n, getArray(n, start, end), getDelta(n, start, end)));

        //Метод Симпсона
        System.out.println("Площадь при рассчете с помошью метода Симсона: " + simson_method(n, getArray(n, start, end), getDelta(n, start, end), end));

        XYSeries maeSeries = new XYSeries("MAE");
        XYSeries mseSeries = new XYSeries("MSE");
        for (int k=1; k < (n+1); k++){
            double sum = 0;
            for (int i=1; i < (k+1); i++) {
                double y_ideal = 2*Math.sqrt(Math.E) - 2;
                double y = rectangle_method_left(i, getArray(i, start, end), getDelta(i, start, end));
                double res = Math.abs(y_ideal - y);
                sum = sum + res;
            }
            sum = sum / k;
            maeSeries.add(k, sum);
        }

        // Добавляем данные для MAE
//        maeSeries.add(1, 0.5);
//        maeSeries.add(2, 0.3);
//        maeSeries.add(3, 0.2);
//        maeSeries.add(4, 0.1);

        // Добавляем данные для MSE
        mseSeries.add(1, 0.3);
        mseSeries.add(2, 0.2);
        mseSeries.add(3, 0.15);
        mseSeries.add(4, 0.1);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(maeSeries);
        dataset.addSeries(mseSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "MAE vs MSE", "Epochs", "Error", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);

        JFrame frame = new JFrame("MAE vs MSE Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        ChartPanel chartPanel = new ChartPanel(chart);
        frame.setContentPane(chartPanel);
        frame.setVisible(true);
    }

    private static double function(double x){
        return 1 / Math.sqrt(x);
    }

    private static double rectangle_method_left(int n, Double[][] array, double delta){
        double sum = 0;
        //Выбираем левую точку отрезка
        for (int i = 0; i < n; i++) {
            double f = function(array[i][0]);
            sum = sum + f*delta;
        }
        return sum;
    }

    private static double rectangle_method_right(int n, Double[][] array, double delta){
        double sum = 0;
        //Выбираем правую точку отрезка
        for (int i = 0; i < n; i++) {
            double f = function(array[i][1]);
            sum = sum + f*delta;
        }
        return sum;
    }

    private static double rectangle_method_random(int n, Double[][] array, double delta){
        double sum = 0;
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            double minRange = array[i][0];
            double maxRange = array[i][1];
            double randomNumInRange = minRange + (maxRange - minRange) * random.nextDouble();
            double f = function(randomNumInRange);
            sum = sum + f*delta;
        }
        return sum;
    }
    private static double trapezoid_method(int n, Double[][] array, double delta){
        double sum = 0;
        for (int i=0; i < n; i++) {
            double f_i_1 = array[i][0];
            double f_i = array[i][1];
            sum = sum + (function(f_i) + function(f_i_1)) * (delta / 2);
        }
        return sum;
    }
    private static double simson_method(int n, Double[][] array, double delta, double end){
        double sum = 0;
        for (int i=0; i < n; i++) {
            double f_i_1 = array[i][0];
            double f_i = array[i][1];
            double f_i1;
            if (n != (i+1)) {
                f_i1 = array[i+1][0];
            }
            else {
                f_i1 = end;
            }
            sum = sum + (function(f_i_1) + 4*function((f_i + f_i1) / 2) + function(f_i)) * delta / 6;
        }
        return sum;
    }
    private static Double[][] getArray(int n, double start, double end){
        Double[][] array = new Double[n][2];
        double delta = (end - start) / n;
        double start_i = 1;
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