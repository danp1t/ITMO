<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Результат проверки</title>
</head>
<body>
<h1>Результаты проверки</h1>

<%
    Boolean isInArea = (Boolean) request.getAttribute("isInArea");
%>

<p>Точка (X: <%= request.getParameter("coor_x") %>, Y: <%= request.getParameter("coor_y") %>) попадает в область:
<%= isInArea ? "Да" : "Нет" %></p>

<a href="/my-web-app/index.jsp">Назад</a>

</body>
</html>