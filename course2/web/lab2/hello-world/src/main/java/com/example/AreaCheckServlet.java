package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static java.lang.Math.abs;

@WebServlet("/areaCheck")
public class AreaCheckServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        double x = Double.parseDouble(request.getParameter("coor_x"));
        double y = Double.parseDouble(request.getParameter("coor_y"));
        double r = Double.parseDouble(request.getParameter("coor_r"));

        boolean isInArea = checkArea(x, y, r);

        ResultList resultList = (ResultList) request.getSession().getAttribute("resultList");
        if (resultList == null) {
            resultList = new ResultList();
            request.getSession().setAttribute("resultList", resultList);
        }
        resultList.addResult(new ResultBean(x, y, r, isInArea));

        request.setAttribute("isInArea", isInArea);
        request.getRequestDispatcher("/result.jsp").forward(request, response);
    }

    private boolean checkArea(double x, double y, double r) {
        // Проверка попадания точки (x, y) в область с радиусом r
        if (x >= 0 && y >= 0){
            if ((x*x+ y*y) <= (r*r)) {
                return true;
            }
                return false;
        }
        else if (x >= 0 && y <= 0){
            return false;
        }
        else if (x <= 0 && y >= 0){
            if (abs(x)  <= r/2 && y <= r) {
                return true;
            }
            return false;}
        else if (x <= 0 && y <= 0){
            if ((abs(x) + abs(y)) <= r) {
                return true;
            }
            return false;
        }
        return false;
    }
}