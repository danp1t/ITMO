import numpy as np
import re
from math_module import *

#Глобальные переменные
equations = [("2x^3 + 3.41x^2 - 1.943x + 2.12"), ("sin(x) + cos(x) - 0.6"), ("cos(x) - 0.34x - 0.21"), ("-3.2x^3 - 3.2x - 2"), ("-33x^3 + 21.23x^2 + 0.68")]
interval = None
epsilon = None
equation = None

#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /choice_equations - выбор уравнения")
    print("7. /input_interval - ввод интервала с клавиатуры")
    print("8. /input_epsilon - ввод погрешности с клавиатуры")
    print()

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

def info():
    if equation is not None:
        print(f"Выбрано уравнение: {equations[equation]}")
    if interval is not None:
        print(f"Интервал: {interval}")
    if epsilon is not None:
        print(f"Погрешность: {epsilon}")

def clear():
    global equation, epsilon, interval
    equation = None
    epsilon = None
    interval = None

def start():
    if equation is None:
        choice_equations()
    if interval is None:
        input_interval()
    if epsilon is None:
        input_epsilon()

    original_eq = equations[equation]
    parsed_eq = parse_equation(original_eq)
    f = lambda x: eval(parsed_eq, {'np': np, 'x': x})

    print("Выберете способ численного интегрирования")
    print("1. Метод левых прямоугольников")
    print("2. Метод правых прямоугольников")
    print("3. Метод средних прямоугольников")
    print("4. Метод трапеций")
    print("5. Метод Симпсона")
    number = input("Введите номер метода: ")

    a, b = interval
    try:
        if number == "1": result, error = left_rectangle_method(f, a, b, epsilon)
        elif number == "2": result, error = right_rectangle_method(f, a, b, epsilon)
        elif number == "3": result, error = middle_rectangle_method(f, a, b, epsilon)
        elif number == "4": result, error = trapezoid_method(f, a, b, epsilon)
        elif number == "5": result, error = simpson_method(f, a, b, epsilon)
        else:
            print("Нужно указать метод числом от 1 до 5")
            start()
        result = abs(result)
        print("\n" + "=" * 50)
        print(f"Уравнение: {original_eq}")
        print(f"Интервал: [{a}, {b}]")
        print(f"Точность: {epsilon}")
        print(f"Результат интегрирования: {result:.6f}")
        print(f"Оценка погрешности: {error:.6f}")
        print("=" * 50 + "\n")
    except ValueError as e:
        print(e)
