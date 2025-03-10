import math_module

#Глобальные данные
equation = None
system = None


#Команда для вывода списка команд с их описанием
def help():
    print("0. /help - вывести список команд с их описанием")
    print("1. /exit - выход из программы")
    print("2. /info - вывести информацию о введеденных данных")
    print("3. /start - запуск программы")
    print("4. /clear - очистка введенных данных")
    print("5. /choice_equation - выбор уравнения")
    print("6. /choice_system - выбор системы уравнений")
    print()

def choice_equation():
    global equation
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
    global system
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
        print("Уравнение: ", equation)
    if system is not None:
        print("Система уравнений: ", system)



def start():
    pass

def clear():
    pass