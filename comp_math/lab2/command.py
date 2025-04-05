#Глобальные переменные
type_n = None # Тип решения (система или уравнения)
systems = [("sin(x - y) - xy = -1", "0.3x^2 + y^2 = 2"), ("sin(y) + 2x = 2", "y + cos(x - 1) = 0.7")] # Системы нелинейных уравнений
equations = [("2x^3 + 3.41x^2 - 1.943x + 2.12"), ("sin(x) + cos(x) - 0.4 = 0.2"), ("tg(x) - 2.34 = 21"), ("-3.2x^3 - 3.2x = 2"), ("-33x^3 + 21.23x^2 + 3 = 2.32")] #Нелинейные уравнения, доступные на выбор
system = None #Текущая система
equation = None #Текущее уравнение

#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /choice_system - выбор системы")
    print("7. /choice_equations - выбор уравнения")
    print()


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

def start():
    pass

def clear():
    pass
