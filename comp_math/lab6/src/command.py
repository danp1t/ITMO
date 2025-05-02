#Глобальные переменные
interval = None
equation = None
equations = [("y' = 2x - y"), ("y' = y/x"), ("y' = e^(-x) + y^2"), ("y' = y(1-y)"), ("y' = sin(x)*y")]
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

def clear():
    global interval, equation, h, epsilon
    interval = None
    equation = None
    h = None
    epsilon = None

def start():
    pass
