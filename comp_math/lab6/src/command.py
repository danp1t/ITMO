import re
from math_module import *
import matplotlib.pyplot as plt


#Глобальные переменные
interval = None
equation = None
equations = [("2x^3 + 3.41x^2 - 1.943x + 2.12 = 0"), ("sin(x) + cos(x) - 0.6 = 0"), ("cos(x) - 0.34x - 0.21 = 0"), ("-3.2x^3 - 3.2x - 2 = 0"), ("-33x^3 + 21.23x^2 + 0.68 = 0")]
h = None
epsilon = None

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

def choice_equations():
    pass

def input_h():
    pass

def input_start_points():
    pass

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
    pass

def input_h():
    pass

def info():
    pass

def clear():
    pass

def start():
    pass
