document.getElementById('coor_x').addEventListener('input', function() {
    const input = this.value;
    const form_x = document.getElementById('form_x');
    const errorMessage = document.getElementById('error_message');

    // Проверяем, является ли введенное значение числом
    const isNumber = /^-?\d*\.?\d*$/.test(input); // Регулярное выражение для проверки числа

    if (!isNumber) {
        this.style.backgroundColor = 'lightcoral'; // Красим поле в светло-красный
        form_x.textContent = ''; // Не передаем данные в форму
        errorMessage.textContent = 'Ошибка: введите число.'; // Сообщение об ошибке
    } else if (input < -3 || input > 3) {
        this.value = null; // Очищаем поле
        this.style.backgroundColor = 'lightcoral'; // Красим поле в светло-красный
        form_x.textContent = ''; // Не передаем данные в форму
        errorMessage.textContent = 'Ошибка ввода координаты X. X должен быть от -3 до 3'; // Сообщение об ошибке
    } else {
        this.style.backgroundColor = ''; // Сбрасываем цвет поля
        form_x.textContent = input; // Передаем данные в форму
        errorMessage.textContent = ''; // Очищаем сообщение об ошибке
    }
});

        const buttons = document.querySelectorAll('.coor_y');

		buttons.forEach(button => {
    	button.addEventListener('click', function() {
        
        const value = this.textContent; 
        
        const form_y = document.getElementById('form_y');
        
        form_y.textContent = value;
    });
});
		const radioButtons = document.querySelectorAll('.coor_r');

		radioButtons.forEach(radio => {
    	radio.addEventListener('change', function() {
        if (this.checked) {
            const label = document.querySelector(`label[for="${this.id}"]`).textContent;
            const form_r = document.getElementById('form_r');
            const floatValue = parseFloat(label) / 2;

            form_r.textContent = label;

            //Изменение для надписей у рисунка
            const north_top = document.getElementById('north-top');
            north_top.textContent = label;
            const north_middle = document.getElementById('north-middle');
            north_middle.textContent = floatValue;
            const south_buttom = document.getElementById('south-buttom');
            south_buttom.textContent = "-" + label;
            const south_middle = document.getElementById('south-middle');
            south_middle.textContent = "-" + floatValue;
            const west_left = document.getElementById('west-left');
            west_left.textContent = "-" + label;
            const west_middle = document.getElementById('west-middle');
            west_middle.textContent = "-" + floatValue;
            const east_right = document.getElementById('east-right');
            east_right.textContent = label
            const east_middle = document.getElementById('east-middle');
            east_middle.textContent = floatValue;
        }
    });
});
	document.getElementById('main_button').addEventListener('click', function() {
    const x = document.getElementById('form_x').textContent;
    const y = document.getElementById('form_y').textContent;
    const r = document.getElementById('form_r').textContent;
    const messageDiv = document.getElementById('message');

    // Сбрасываем сообщение перед проверкой
    messageDiv.style.display = 'none';
    messageDiv.textContent = '';

    if (r == 'Значение R') {
        messageDiv.textContent = "Выберите значение для числа R";
        messageDiv.style.display = 'block';
        return; // Прерываем выполнение функции
    }

    if (y == 'Значение Y') {
        messageDiv.textContent = "Выберите значение для числа Y";
        messageDiv.style.display = 'block';
        return; // Прерываем выполнение функции
    }

    if (x == 'Значение X' || x == "") {
        messageDiv.textContent = "Выберите значение для числа X";
        messageDiv.style.display = 'block';
        return; // Прерываем выполнение функции
    }

      
      	
      //Написать GET запрос, который передает данные на сервер
      $.ajax({
    url: '/fcgi-bin/lab1.jar',
    method: 'GET',
    dataType: 'html',
    data: {
        coor_x: document.getElementById('form_x').textContent,
        coor_y: document.getElementById('form_y').textContent,
        coor_r: document.getElementById('form_r').textContent
    },
    success: function(data, textStatus, jqXHR) {
        document.getElementById('resultTable').innerHTML = data;
    }
});
    });