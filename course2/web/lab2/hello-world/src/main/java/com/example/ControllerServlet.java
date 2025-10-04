package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/controller")
public class ControllerServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String xParam = request.getParameter("coor_x");
        String yParam = request.getParameter("coor_y");
        String rParam = request.getParameter("coor_r");

        if (xParam != null && yParam != null && rParam != null) {
            request.setAttribute("coor_x", xParam);
            request.setAttribute("coor_y", yParam);
            request.setAttribute("coor_r", rParam);
            request.getRequestDispatcher("/areaCheck").forward(request, response);
        } else {
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}