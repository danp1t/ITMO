#Глобальные переменные
type_n = None # Тип решения (система или уравнения)
systems = [("sin(x - y) - xy = -1", "0.3x^2 + y^2 = 2"), ("sin(y) + 2x = 2", "y + cos(x - 1) = 0.7")] # Системы нелинейных уравнений
equations = [("2x^3 + 3.41x^2 - 1.943x + 2.12"), ("sin(x) + cos(x) - 0.4 = 0.2"), ("tg(x) - 2.34 = 21"), ("-3.2x^3 - 3.2x = 2"), ("-33x^3 + 21.23x^2 + 3 = 2.32")] #Нелинейные уравнения, доступные на выбор
system = None #Текущая система
equation = None #Текущее уравнение
epsilon = None #Погрешность
start_value = None #Начальное приближение к корню
interval = None #Интервал

#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /choice_system - выбор системы")
    print("7. /choice_equations - выбор уравнения")
    print("8. /input_interval - ввод интервала с клавиатуры")
    print("9. /input_interval_file - ввод интервала из файла")
    print("10. /input_start_value - ввод начального приближения с клавиатуры")
    print("11. /input_start_value_file - ввод начального приблежения из файла")
    print("12. /input_epsilon - ввод погрешности с клавиатуры")
    print("13. /input_epsilon_file - ввод погрешности из файла")
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

def input_interval_file():
    global interval
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()
            a, b = map(float, lines[0].split())
            if a >= b:
                print("Ошибка: нижняя граница должна быть меньше верхней.")
                input_interval_file()
            else:
                interval = (a, b)
    except (ValueError, IndexError):
        print("Ошибка: некорректный формат данных в файле.")
        input_interval_file()
    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_interval_file()


def input_start_value():
    global start_value
    try:
        start_value = float(input("Введите начальное приближение: "))
    except ValueError:
        print(f"Ошибка: Получено не число: {start_value}")
        return input_start_value()

def input_start_value_file():
    global start_value
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            content = file.read().strip()
            numbers = content.split()

            if len(numbers) != 1:
                print(f"Ошибка: в файле должно быть ровно одно число. Найдено: {len(numbers)}")
                return input_start_value_file()
            try:
                start_value = float(numbers[0])
            except ValueError:
                print(f"Ошибка: невозможно преобразовать '{numbers[0]}' в float")
                return input_start_value_file()

    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_start_value_file()

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

def input_epsilon_file():
    global epsilon
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            content = file.read().strip()
            numbers = content.split()

            if len(numbers) != 1:
                print(f"Ошибка: в файле должно быть ровно одно число. Найдено: {len(numbers)}")
                input_epsilon_file()
            try:
                epsilon = float(numbers[0])
            except ValueError:
                print(f"Ошибка: невозможно преобразовать '{numbers[0]}' в float")
                input_epsilon_file()

    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_epsilon_file()

def choice_system():
    global system
    print("Выберите систему нелинейных уравнений")
    for i in range(len(systems)):
        print(f"{i}. {systems[i]}")
    try:
        system = int(input("Введите номер системы: "))
        if system >= len(systems):
            print(f"Ошибка: system должно быть в указанном диапазоне. Получено: {system}")
            return choice_system()
        if system < 0:
            print(f"Ошибка: system должен быть неотрицательным. Получено: {system}")
            return choice_system()
    except ValueError:
        print(f"Ошибка: номер system должен быть целым числом. Получено: {system}")
        return choice_system()


def choice_equations():
    global equation
    for i in range(len(equations)):
        print(f"{i}. {equations[i]}")
    try:
        equation = int(input("Введите номер нелинейного уравнения: "))
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
    if system is not None:
        print(f"Выбрана система: {systems[system]}")
    if equation is not None:
        print(f"Выбрано уравнение: {equations[equation]}")
    if interval is not None:
        print(f"Интервал: {interval}")
    if epsilon is not None:
        print(f"Погреность: {epsilon}")
    if start_value is not None:
        print(f"Начальное приближение: {start_value}")

def start():
    if (equation is None and system is None) or (equation is not None and system is not None):
        print("Выберете, что будем решать")
        print("1. Нелинейное уравнение")
        print("2. Систему нелинейных уравнений")
        number = input()
        if number == 1:
            choice_equations()


        elif number == 2:
            choice_system()



        else:
            print("Нужно ввести число 1 или 2")
            start()
    elif equation is not None and system is None:
        print("Выбран вариант решения нелинейных уравнений")

    elif equation is None and system is not None:
        print("Выбран вариант решения системы нелинейных уравнений")

def clear():
    print("Сброс всех значений")
    system = None
    equation = None
    epsilon = None
    start_value = None
    interval = None
