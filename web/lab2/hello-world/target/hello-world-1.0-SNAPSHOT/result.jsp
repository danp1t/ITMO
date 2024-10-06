<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.ResultList" %>
<%@ page import="com.example.ResultBean" %>
<html>
<head>
    <title>Результат проверки</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<%
    Boolean isInArea = (Boolean) request.getAttribute("isInArea");
%>


    <table border="1">
        <thead>
            <tr>
                <th>X</th>
                <th>Y</th>
                <th>R</th>
                <th>Результат</th>
            </tr>
        </thead>
        <tbody>
            <% ResultList resultList = (ResultList) session.getAttribute("resultList");
               if (resultList != null) {
                   for (ResultBean result : resultList.getResults()) {
                       out.println("<tr>");
                       out.println("<td>" + result.getX() + "</td>");
                       out.println("<td>" + result.getY() + "</td>");
                       out.println("<td>" + result.getR() + "</td>");
                       out.println("<td>" + (result.isInArea() ? "Да" : "Нет") + "</td>");
                       out.println("</tr>");
                   }
               }
            %>
        </tbody>
    </table>
    <a href="/hello-world-1.0-SNAPSHOT/index.jsp">Назад</a>

</body>
</html>