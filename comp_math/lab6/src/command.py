#Глобальные переменные
from math_module import euler_method
from math_module import runge_kutta_method
from math_module import milne_method
from math_module import compare_methods
from math_module import exact_solutions
import matplotlib.pyplot as plt

interval = None
equation = None
equations = [("y' = 2x - y"), ("y' = y/x"), ("y' = e^(-x) + y^2"), ("y' = y(1-y)"), ("y' = sin(x)*y")]
h = None
epsilon = None
start_points = None

#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /input_epsilon - ввод точности")
    print("7. /input_h - ввод шага")
    print("8. /input_interval - ввод интервала дифференицирования [x_0, x_n]")
    print("9. /input_start_points - ввод начальных условий y_0 = y(x_0)")
    print("10. /choice_equations - выбор функции")


def plot_solutions(results, exact_sol_func=None, equation_name=""):
    plt.figure(figsize=(10, 6))

    # Цвета для разных методов
    colors = {
        'Эйлер': 'red',
        'Рунге-Кутта': 'blue',
        'Милн': 'green'
    }

    for method in results:
        x = method['x']
        y = method['y']
        plt.plot(x, y,
                 linestyle='--',
                 marker='o',
                 markersize=4,
                 color=colors.get(method['name'], 'black'),
                 label=method['name'])

    if exact_sol_func:
        x_exact = method['x']
        y_exact = [exact_sol_func(xi, results[0]['x'][0], results[0]['y'][0])
                   for xi in x_exact]
        plt.plot(x_exact, y_exact,
                 color='black',
                 linewidth=2,
                 label='Точное решение')

    # Настройки графика
    plt.title(f"Решение уравнения: {equation_name}")
    plt.xlabel('x')
    plt.ylabel('y')
    plt.grid(True)
    plt.legend()

    # Автоматическая настройка масштаба
    plt.autoscale(enable=True, axis='both', tight=True)

    # Сохранение в файл
    plt.savefig('solution_plot.png', dpi=300)

def choice_equations():
    global equation
    for i in range(len(equations)):
        print(f"{i}. {equations[i]}")
    try:
        equation = int(input("Введите номер функции: "))
        if equation >= len(equations):
            print(f"Ошибка: номер equation должен быть в указаном диапазоне. Получено: {equation}")
            choice_equations()
        if equation < 0:
            print(f"Ошибка: номер equation должен быть неотрицательным. Получено: {equation}")
            choice_equations()
    except ValueError:
        print(f"Ошибка: номер equation должен быть целым числом. Получено: {equation}")
        choice_equations()


def input_h():
    global h
    try:
        h = float(input("Введите шаг: "))
        if h <= 0:
            print("Значение шага должно быть больше 0")
            input_h()
    except ValueError:
        print("Ошибка: введено не число")
        input_h()

def input_start_points():
    global start_points
    try:
        input_str = input("Введите начальные условия в формате 'x0 y0' (например, '0 1'): ")
        x0, y0 = map(float, input_str.strip().split())
        start_points = (x0, y0)
    except ValueError:
        print("Ошибка: введите два числа, разделенных пробелом. Попробуйте снова.\n")
        input_start_points()

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

def input_epsilon():
    global epsilon
    try:
        epsilon = float(input("Введите точность: "))
        if epsilon <= 0:
            print("Точность должна быть больше 0")
            input_epsilon()
    except ValueError:
        print("Ошибка: введено не число")
        input_epsilon()

def info():
    if interval is not None:
        print(f"Интервал дифференцирования: {interval}")
    if equation is not None:
        print(f"Выбрано уравнение: {equations[equation]}")
    if h is not None:
        print(f"Шаг h: {h}")
    if epsilon is not None:
        print(f"Точность: {epsilon}")
    if start_points is not None:
        print(f"Начальные условия (x_0, y_0): {start_points}")

def clear():
    global interval, equation, h, epsilon, start_points
    interval = None
    equation = None
    h = None
    epsilon = None
    start_points = None

def start():
    if equation is None:
        choice_equations()
    if start_points is None:
        input_start_points()
    if interval is None:
        input_interval()
    if h is None:
        input_h()
    if epsilon is None:
        input_epsilon()

    print("1. Метод Эйлера")
    print("2. Метод Рунге-Кутта 4-го порядка")
    print("3. Метод Милна")
    print("4. Все методы")
    choice_n = input("Выберете метод для решение ОДУ: ")

    if choice_n == "1":
        euler_method(start_points, interval, h, epsilon, equation)
        clear()
    elif choice_n == "2":
        runge_kutta_method(start_points, interval, h, epsilon, equation)
        clear()
    elif choice_n == "3":
        milne_method(start_points, interval, h, epsilon, equation)
        clear()
    elif choice_n == "4":
        compare_methods(start_points, interval, h, epsilon, equation)
        euler_res = euler_method(start_points, interval, h, epsilon, equation, flag=False)
        rk_res = runge_kutta_method(start_points, interval, h, epsilon, equation, flag=False)
        milne_res = milne_method(start_points, interval, h, epsilon, equation, flag=False) if exact_solutions[equation] else None
        results = [
            {'x': euler_res['x_values'], 'y': euler_res['y_values'], 'name': 'Эйлер'},
            {'x': rk_res['x_values'], 'y': rk_res['y_values'], 'name': 'Рунге-Кутта'},
        ]

        if milne_res:
            results.append({'x': milne_res['x'], 'y': milne_res['y'], 'name': 'Милн'})

        plot_solutions(
            results=results,
            exact_sol_func=exact_solutions[equation],
            equation_name=equations[equation]
        )
        clear()
    else:
        print("Нужно ввести цифру из диапазона от 1 до 3 для выбора метода")
        start()
