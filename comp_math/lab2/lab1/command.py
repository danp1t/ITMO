import math_module

#Глобальные данные
number_equation = None
equation = None
number_system = None
system = None
interval = None
epsilon = None
approximation = None


#Команда для вывода списка команд с их описанием
def help():
    print("0. /help - вывести список команд с их описанием")
    print("1. /exit - выход из программы")
    print("2. /info - вывести информацию о введеденных данных")
    print("3. /start - запуск программы")
    print("4. /clear - очистка введенных данных")
    print("5. /choice_equation - выбор уравнения")
    print("6. /choice_system - выбор системы уравнений")

    print("7. /input_interval - ввод интервала")
    print("8. /input_epsilon - ввод погрешности")
    print("9. /input_approximation - ввод начального приближения")

    print("10. /input_file_interval - ввод интервала из файла")
    print("11. /input_file_epsilon - ввод погрешности из файла")
    print("12. /input_file_approximation - ввод начального приближения из файла")
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

def input_epsilon():
    global epsilon
    try:
        epsilon = float(input("Введите погрешность: "))
        if epsilon <= 0:
            print("Ошибка: погрешность должна быть положительным числом. Попробуйте снова.")
            input_epsilon()
    except ValueError:
        print("Ошибка: введите числовое значение. Попробуйте снова.")
        input_epsilon()

def input_approximation():
    global approximation
    try:
        approximation = float(input("Введите начальное приближение: "))
    except ValueError:
        print("Ошибка: введите числовое значение. Попробуйте снова.")
        input_approximation()

def input_file_interval():
    global interval
    filename = input("Введите путь к файлу: ")
    try:
        with open(filename, 'r') as file:
            lines = file.readlines()
            a, b = map(float, lines[0].split())
            if a >= b:
                print("Ошибка: нижняя граница должна быть меньше верхней.")
                input_file_interval()
            else:
                interval = (a, b)
    except (ValueError, IndexError):
        print("Ошибка: некорректный формат данных в файле.")
        input_file_interval()

def input_file_epsilon():
    global epsilon
    filename = input("Введите путь к файлу: ")
    try:
        with open(filename, 'r') as file:
            lines = file.readlines()
            epsilon = float(lines[1].strip())
            if epsilon <= 0:
                print("Ошибка: погрешность должна быть положительным числом.")
                input_file_epsilon()
    except (ValueError, IndexError):
        print("Ошибка: некорректный формат данных в файле.")
        input_file_epsilon()

def input_file_approximation():
    global approximation
    filename = input("Введите путь к файлу: ")
    try:
        with open(filename, 'r') as file:
            lines = file.readlines()
            approximation = float(lines[2].strip())
            return True
    except (ValueError, IndexError):
        print("Ошибка: некорректный формат данных в файле.")
        input_file_approximation()


def choice_equation():
    global equation, number_equation
    print("Доступные уравнения: ")
    print()
    print("1. x^2 - 4 = 0")
    print("2. sin(x) - 0.5 = 0")
    print("3. x^2 = 1")
    print("4. x^3 + 2x^2 - 3x = 0")
    print("5. x^5 + 3x = 1")
    try:
        number_equation = int(input("Введите номер уравнения: "))
        if number_equation not in range(1, 6):
            print("Некорректный ввод")
            return choice_equation()
        match number_equation:
            case 1:
                equation = "x^2 - 4 = 0"
            case 2:
                equation = "sin(x) - 0.5 = 0"
            case 3:
                equation = "x^2 = 1"
            case 4:
                equation = "x^3 + 2x^2 - 3x = 0"
            case 5:
                equation = "x^5 + 3x = 1"
    except ValueError:
        print("Некорректный ввод")
        return choice_equation()

def choice_system():
    global system, number_system
    print("Доступные системы уравнений: ")
    print()
    print("1. x^3 + y = 1, x - 3y = 3")
    print("2. x^2 + sin(y) = 3, 2x + 5y^2 = 2")
    print("3. x^2 + y^2 = 5, x^2 - y^2 = 1")
    try:
        number_system = int(input("Введите номер системы уравнений: "))
        if number_system not in range(1, 4):
            print("Некорректный ввод")
            return choice_system()
        match number_system:
            case 1:
                system = "x^3 + y = 1, x - 3y = 3"
            case 2:
                system = "x^2 + sin(y) = 3, 2x + 5y^2 = 2"
            case 3:
                system = "x^2 + y^2 = 5, x^2 - y^2 = 1"
    except ValueError:
        print("Некорректный ввод")
        choice_equation()



def info():
    if equation is not None:
        print(number_equation, ".", "Уравнение: ", equation)
    if system is not None:
        print(number_system, ".", "Система уравнений: ", system)
    if interval is not None:
        print("Интервал: ", interval)
    if epsilon is not None:
        print("Погрешность: ", epsilon)
    if approximation is not None:
        print("Начальное приближение: ", approximation)
    print()



def start():
    pass

def clear():
    pass