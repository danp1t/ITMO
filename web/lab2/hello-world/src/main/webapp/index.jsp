<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.ResultList" %>
<%@ page import="com.example.ResultBean" %>
<!DOCTYPE html>
<html>
<head>
    <title>Лаба №2</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <link rel="stylesheet" href="styles.css">
    <meta charset="UTF-8">
</head>
<body>
<header>
    <span id="myname">Путинцев Данил Денисович</span>
    <span id="mygroup">P3207</span>
    <span id="variant">Вариант: 409323</span>
</header>
<div id="form">
    <form id="myForm" method="GET" action="controller">
        <div id="coor_x">
            <label for="coor_x">Выберите координату X:</label><br>
            <input type="checkbox" id="coor_x1" name="coor_x" value="-3" onclick="onlyOne(this)"/> -3
            <input type="checkbox" id="coor_x2" name="coor_x" value="-2" onclick="onlyOne(this)"/> -2
            <input type="checkbox" id="coor_x3" name="coor_x" value="-1" onclick="onlyOne(this)"/> -1
            <input type="checkbox" id="coor_x4" name="coor_x" value="0" onclick="onlyOne(this)"/> 0
            <input type="checkbox" id="coor_x5" name="coor_x" value="1" onclick="onlyOne(this)"/> 1
            <input type="checkbox" id="coor_x6" name="coor_x" value="2" onclick="onlyOne(this)"/> 2
            <input type="checkbox" id="coor_x7" name="coor_x" value="3" onclick="onlyOne(this)"/> 3
            <input type="checkbox" id="coor_x8" name="coor_x" value="4" onclick="onlyOne(this)"/> 4
            <input type="checkbox" id="coor_x9" name="coor_x" value="5" onclick="onlyOne(this)"/> 5
        </div>
        <div id="coor_y">
            <label for="coor_y1">Введите координату Y:</label>
            <input type="text" id="coor_y1" name="coor_y" required maxlength="8" size="10"/>
        </div>
        <div id="coor_r">
            <label for="coor_r-select">Выберите координату R:</label>
            <select id="coor_r-select" name="coor_r">
                <option value="">--Выберите R--</option>
                <option value="1">1</option>
                <option value="1.5">1.5</option>
                <option value="2">2</option>
                <option value="2.5">2.5</option>
                <option value="3">3</option>
            </select>
        </div>
        <div id="error_message" style="color: red;"></div>
        <div>
            <button type="submit">Отправить</button>
        </div>
    </form>
</div>

<div style="display: flex; margin-top: 20px;">
    <div>
        <canvas id="canvas" width="400" height="400"></canvas>
    </div>
    <div id="results-Container">
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
    </div>
</div>
<script>
    const results = [

            <% if (resultList != null) { for (ResultBean result : resultList.getResults()) { %>
                { x: <%= result.getX() %>, y: <%= result.getY() %>, inArea: <%= result.isInArea() ? "true" : "false" %> },
            <% } }%>
        ];
    function drawPoints(R) {
            const scale = 60; // Масштаб для отображения
            results.forEach(result => {
                const xCanvas = 200 + result.x * scale; // Преобразование координаты X
                const yCanvas = 200 - result.y * scale; // Преобразование координаты Y (инверсия по Y)

                ctx.fillStyle = result.inArea ? 'green' : 'red'; // Цвет точки
                ctx.beginPath();
                ctx.arc(xCanvas, yCanvas, 5, 0, Math.PI * 2); // Рисуем точку
                ctx.fill();
            });
        }
	document.getElementById('myForm').addEventListener('submit', function(event) {
        event.preventDefault(); // Отменяем стандартное поведение формы

        const checkboxes = document.querySelectorAll('input[name="coor_x"]:checked');
        const yValue = document.getElementById('coor_y1').value;
        const rValue = document.getElementById('coor_r-select').value;

        const errorMessageDiv = document.getElementById('error_message');
        errorMessageDiv.textContent = ''; // Сбрасываем сообщение об ошибке

        if (checkboxes.length === 0 || yValue === '' || rValue === '') {
            errorMessageDiv.textContent = 'Ошибка: Не все поля заполнены!';
            return; // Прерываем выполнение, если есть ошибка
        }

        // Если все поля заполнены, можно отправить форму
        this.submit();
    });

    const canvas = document.getElementById('canvas');
    const ctx = canvas.getContext('2d');

    function drawAxes(R) {
    	ctx.clearRect(0, 0, canvas.width, canvas.height);
    	let halfR = R / 2;
        ctx.beginPath();
        ctx.moveTo(200, 0);
        ctx.lineTo(200, 400); // Ось Y
        ctx.moveTo(0, 200);
        ctx.lineTo(400, 200); // Ось X
        ctx.stroke();
        ctx.fillStyle = 'black'
        ctx.fillText(-R, 200 - R*60, 215);
        ctx.fillText(R, 200 + R*60, 215);
        ctx.fillText(halfR, 200 + halfR*60, 215);
        ctx.fillText(-halfR, 200 - halfR*60, 215);
        ctx.fillText(-R, 205, 200 + R * 60);
        ctx.fillText(R, 205, 200 - R * 60);
        ctx.fillText(-halfR, 205, 200 + halfR * 60);
        ctx.fillText(halfR, 205, 200 - halfR * 60);

        ctx.fillText("y", 205, 8);
        ctx.fillText("x", 400, 215);


    }

    function drawShapes(R) {
        const scale = 60; // Масштаб для отображения

        // Первая четверть: часть окружности
        ctx.beginPath();
		ctx.arc(200, 200, R * scale, -(Math.PI * 0.5), 0); // Часть окружности сверху
		ctx.lineTo(200, 200);
		ctx.fillStyle = 'rgba(51, 153, 255, 0.5)';
		ctx.fill();
		ctx.stroke();

        // Вторая четверть: прямоугольник
        ctx.beginPath();
        ctx.rect(200 - (R * scale) / 2, 200 - R * scale, (R * scale) / 2, R * scale);
        ctx.fillStyle = 'rgba(51, 153, 255, 0.5)';
        ctx.fill();
        ctx.stroke();

        // Третья четверть: треугольник
        ctx.beginPath();
        ctx.moveTo(200 - R * scale, 200);
        ctx.lineTo(200, 200 + R * scale);
        ctx.lineTo(200, 200);
        ctx.closePath();
        ctx.fillStyle = 'rgba(51, 153, 255, 0.5)';
        ctx.fill();
        ctx.stroke();

    }

    function onlyOne(checkbox) {
            const checkboxes = document.getElementsByName('coor_x');
            checkboxes.forEach((item) => {
                if (item !== checkbox) item.checked = false;
            });
        }

    document.getElementById('coor_r-select').addEventListener('change', function() {
        const R = parseFloat(this.value);
        const errorMessageDiv = document.getElementById('error_message');
        errorMessageDiv.textContent = ''

        drawAxes(R);
        drawShapes(R);
        drawPoints();
    });

    // Инициализация с начальным значением R
    const R = parseFloat(document.getElementById('coor_r-select').value);
    drawAxes(R);
    drawShapes(R);
    drawPoints();

    document.getElementById('coor_y1').addEventListener('input', function() {
    const input = this.value;
    const errorMessage = document.getElementById('error_message');

    // Проверяем, является ли введенное значение числом
    const isNumber = /^-?\d*\.?\d*$/.test(input); // Регулярное выражение для проверки числа

    if (!isNumber) {
        this.style.backgroundColor = 'lightcoral'; // Красим поле в светло-красный
        coor_y1.textContent = ''; // Не передаем данные в форму
        errorMessage.textContent = 'Ошибка: введите число.'; // Сообщение об ошибке
    } else if (input < -5 || input > 3) {
        this.value = null; // Очищаем поле
        this.style.backgroundColor = 'lightcoral'; // Красим поле в светло-красный
        coor_y1.textContent = ''; // Не передаем данные в форму
        errorMessage.textContent = 'Ошибка ввода координаты Y. X должен быть от -5 до 3'; // Сообщение об ошибке
    } else {
        this.style.backgroundColor = ''; // Сбрасываем цвет поля
        coor_y1.textContent = input; // Передаем данные в форму
        errorMessage.textContent = ''; // Очищаем сообщение об ошибке
    }
});

    canvas.addEventListener('click', function(event) {
        const rect = canvas.getBoundingClientRect();
        const x = event.clientX - rect.left; // Получаем координату X
        const y = event.clientY - rect.top; // Получаем координату Y

        // Преобразуем координаты в систему координат вашей области
        const scaledX = (x - 220) / 60; // Масштабируем по X
        const scaledY = (220 - y) / 60; // Масштабируем по Y

        const rValue = document.getElementById('coor_r-select').value;

        const errorMessageDiv = document.getElementById('error_message');
        errorMessageDiv.textContent = ''; // Сбрасываем сообщение об ошибке

        if (rValue === '') {
            errorMessageDiv.textContent = 'Выберете значение R';
            return; // Прерываем выполнение, если есть ошибка
        }

        // Отправляем данные на сервер
        $.ajax({
    	url: '/lab2/controller',
    	method: 'GET',
    	dataType: 'html',
    	data: {
        	coor_x: scaledX,
        	coor_y: scaledY,
        	coor_r: parseFloat(document.getElementById('coor_r-select').value)
    	},
    	success: function(response) {
                // Обновляем содержимое таблицы с результатами
                location.reload();
    }
});

    });
</script>
	</body>
</html>
