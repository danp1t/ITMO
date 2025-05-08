import re
from math_module import *
import matplotlib.pyplot as plt


#Глобальные переменные
table = None
interval = None
count_point = None
equation = None
flag = False
equations = [("2x^3 + 3.41x^2 - 1.943x + 2.12 = 0"), ("sin(x) + cos(x) - 0.6 = 0"), ("cos(x) - 0.34x - 0.21 = 0"), ("-3.2x^3 - 3.2x - 2 = 0"), ("-33x^3 + 21.23x^2 + 0.68 = 0")]


#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /input_table - ввод таблицы y = f(x) из консоли")
    print("7. /input_table_file - ввод таблицы y = f(x) из файла")
    print("8. /input_interval - ввод интервала")
    print("9. /input_count_point - ввод количества точек на интервале")
    print("10. /choice_equations - выбор функции")

def choice_equations():
    global equation
    for i in range(len(equations)):
        print(f"{i}. {equations[i]}")
    try:
        equation = int(input("Введите номер функции: "))
        if equation >= len(equations):
            print(f"Ошибка: номер equation должен быть в указаном диапазоне. Получено: {equation}")
            return choice_equations()
        if equation < 0:
            print(f"Ошибка: номер equation должен быть неотрицательным. Получено: {equation}")
            return choice_equations()
    except ValueError:
        print(f"Ошибка: номер equation должен быть целым числом. Получено: {equation}")
        return choice_equations()

def parse_equation(eq_str):
    eq_str = eq_str.replace('sin', 'np.sin')
    eq_str = eq_str.replace('cos', 'np.cos')
    eq_str = eq_str.replace('tan', 'np.tan')
    eq_str = eq_str.replace('tg', 'np.tan')
    eq_str = eq_str.replace('sqrt', 'np.sqrt')
    eq_str = eq_str.replace('^', '**')

    eq_str = re.sub(r'(?<=[0-9)])(?=[a-zA-Z])', '*', eq_str)
    eq_str = re.sub(r'(?<=[a-zA-Z])(?=[0-9])', '*', eq_str)

    if '=' in eq_str:
        left, right = eq_str.split('=', 1)
        expr = f'({left.strip()}) - ({right.strip()})'
    else:
        expr = eq_str.strip()

    return expr

def create_table_from_input(equation, interval, count_point):
    eq_str = equations[equation]
    expr = parse_equation(eq_str)
    a, b = interval
    x_values = np.linspace(a, b, count_point)
    y_values = eval(expr, {'np': np, 'x': x_values}, {})
    return [x_values.tolist(), y_values.tolist()]

def input_count_point():
    global count_point
    try:
        count_point = int(input("Введите количество точек на интервале: "))
        if count_point < 2:
            print("Количество точек на интервале не должно быть меньше 2")
            input_count_point()
    except ValueError:
        print("Ошибка: введите целочисленное число. Попробуйте снова")
        input_count_point()

def input_interval():
    global interval
    try:
        a = float(input("Введите нижнюю границу интервала: "))
        b = float(input("Введите верхнюю границу интервала: "))
        if a >= b:
            print("Ошибка: нижняя граница должна быть меньше верхней. Попробуйте снова.")
            input_interval()
        else:
            interval = (a, b)
    except ValueError:
        print("Ошибка: введите числовые значения. Попробуйте снова.")
        input_interval()

def input_table():
    global table, x, y
    x = input("Введите значения x: ")
    y = input("Введите значения y: ")
    try:
        x = list(map(float, x.split()))
        y = list(map(float, y.split()))

        if len(x) != len(y):
            print("Количество чисел должно совпадать")
            input_table()
        if len(set(x)) != len(x):
            print("Точки должны быть различными по x")
            input_table()
        table = [x, y]
    except ValueError:
        print("Обнаружена ошибка при вводе")
        input_table()


def save_plot(table, output_file="plot.png"):
    global flag
    methods = [
        (method_langrange, "Лагранж"),
        (method_newton, "Ньютон"),
        (method_gauss, "Гаусс"),
        (method_stirling, "Стрилинг"),
        (method_bessel, "Бессель")
    ]
    if flag:
        methods = [
            (method_langrange, "Лагранж"),
            (method_newton, "Ньютон")
        ]
    flag = False
    plt.figure(figsize=(12, 7))
    x_min, x_max = min(table[0]), max(table[0])
    x_vals = np.linspace(x_min, x_max, 500)

    plt.scatter(table[0], table[1], s=80, color='black',
                zorder=3, label='Узлы интерполяции')

    # Построение кривых
    for method, label in methods:
        y_vals = [method(x, table) for x in x_vals]
        plt.plot(x_vals, y_vals, linewidth=2, label=label)

    # Оформление
    plt.title("Сравнение методов интерполяции", fontsize=14)
    plt.xlabel("x", fontsize=12)
    plt.ylabel("y", fontsize=12)
    plt.grid(True, linestyle='--', alpha=0.7)
    plt.legend(fontsize=10)

    # Сохранение в файл
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    plt.close()

def input_table_file():
    global table
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()
            x = lines[0]
            y = lines[1]
            try:
                x = list(map(float, x.split()))
                y = list(map(float, y.split()))

                if len(x) != len(y):
                    print("Количество чисел должно совпадать")
                    input_table_file()
                if len(set(x)) != len(x):
                    print("Точки должны быть различными по x")
                    input_table_file()
                table = [x, y]
            except ValueError:
                print("Обнаружена ошибка при вводе")
                input_table_file()
    except (ValueError, IndexError) as e:
        print(e)
        print("Ошибка: некорректный формат данных в файле.")
        input_table_file()
    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_table_file()

def info():
    if table is not None:
        print(f"Введена таблица: ")
        print(f"Значения x: {table[0]}")
        print(f"Значения y: {table[1]}")
    if equation is not None:
        print(f"Выбрано уравнение: {equations[equation]}")
    if interval is not None:
        print(f"Интервал: {interval}")
    if count_point is not None:
        print(f"Количество точек на интервале: {count_point}")

def clear():
    global table, equation, interval, count_point, flag
    table = None
    equation = None
    interval = None
    flag = False
    count_point = None

def start():
    global table, flag
    if table is None:
         if equation is not None and interval is not None and count_point is not None:
             table = create_table_from_input(equation, interval, count_point)
         print("Выберете, каким образом будете вводить информацию")
         print("1. Введу таблицу")
         print("2. Введу таблицу из файла")
         print("3. Выберу функцию и введу параментры")
         n_choice = input("Ваш выбор: ")
         if n_choice == "1":
             input_table()
         elif n_choice == "2":
             input_table_file()
         elif n_choice == "3":
             choice_equations()
             input_interval()
             input_count_point()
             table = create_table_from_input(equation, interval, count_point)
         else:
             print("Ваш выбор некорректен")
             print("Попробуйте заново")
             clear()
             start()
    print("Выберете метод для интерполяции функции")
    print("1. Многочлен Лагранжа")
    print("2. Многочлен Ньютона с разделенными разностями")
    print("3. Многочлен Гаусса")
    print("4. Многочлен Стирлинга")
    print("5. Многочлен Бесселя")
    print("6. Все методы")
    n_choice = input("Выберете метод для интерполяции функции: ")
    try:
        x = float(input("Введите значения для нахождения приближенного значения функции: "))
        if min(table[0]) > x or x > max(table[0]):
            print("Ошибка: x должен быть внутри интервала")
            clear()
            start()
    except ValueError:
        print("Ошибка: Необходимо ввести число")
        clear()
        start()
    if n_choice == "1":
        print(f"Метод Лагранжа: {method_langrange(x, table)}")
    elif n_choice == "2":
        print(f"Метод Ньютона с разделенными разностями: {method_newton(x, table)}")
    elif n_choice == "3":
        print(f"Метод Гаусса: {method_gauss(x, table)}")
    elif n_choice == "4":
        print(f"Метод Стирлинга: {method_stirling(x, table)}")
    elif n_choice == "5":
        print(f"Метод Бесселя: {method_bessel(x, table)}")
    elif n_choice == "6":
        print(f"Метод Лагранжа: {method_langrange(x, table)}")
        print(f"Метод Ньютона с разделенными разностями: {method_newton(x, table)}")
        if method_gauss(x, table) == -1:
            flag = True
        else:
            print(f"Метод Гаусса: {method_gauss(x, table)}")
        if method_stirling(x, table) == -1:
            flag = True
        else:
            print(f"Метод Стирлинга: {method_stirling(x, table)}")
        if method_bessel(x, table) == -1:
            flag = True
        else:
            print(f"Метод Бесселя: {method_bessel(x, table)}")

        save_plot(table)
        clear()

    else:
        print("Вы ввели некорректное значение, попробуйте еще раз")
        clear()
        start()
