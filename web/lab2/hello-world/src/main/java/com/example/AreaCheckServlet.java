package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/areaCheck")
public class AreaCheckServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        double x = Double.parseDouble(request.getParameter("coor_x"));
        double y = Double.parseDouble(request.getParameter("coor_y"));
        double r = Double.parseDouble(request.getParameter("coor_r"));

        boolean isInArea = checkArea(x, y, r);

        request.setAttribute("isInArea", isInArea);
        request.getRequestDispatcher("/result.jsp").forward(request, response);
    }

    private boolean checkArea(double x, double y, double r) {
        // Проверка попадания точки (x, y) в область с радиусом r
        return (x >= 0 && y >= 0 && x * x + y * y <= r * r) || // В первой четверти
                (x <= 0 && y >= 0 && x + y <= r) || // Вторая четверть
                (x <= 0 && y <= 0 && x * x + y * y <= r * r); // Третья четверть
    }
}