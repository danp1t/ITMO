from math_module import *

#Глобальные переменные
table = None

#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /input_table - ввод таблицы y = f(x) из консоли")
    print("7. /input_table_file - ввод таблицы y = f(x) из файла")
    print()

def input_table():
    global table
    x = input("Введите значения x: ")
    y = input("Введите значения y: ")
    try:
        x = list(map(float, x.split()))
        y = list(map(float, y.split()))

        if len(x) != len(y):
            print("Количество чисел должно совпадать")
            input_table()
        if 8 > len(x) or len(x) > 12:
            print("Таблица должна содержать от 8 до 12 точек")
            input_table()
        if len(set(x)) != len(x):
            print("Точки должны быть различными по x")
            input_table()
        table = [x, y]
    except ValueError:
        print("Обнаружена ошибка при вводе")
        input_table()

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
                if 8 > len(x) or len(x) > 12:
                    print("Таблица должна содержать от 8 до 12 точек")
                    input_table_file()
                if len(set(x)) != len(x):
                    print("Точки должны быть различными по x")
                    input_table_file()
                table = [x, y]
            except ValueError:
                print("Обнаружена ошибка при вводе")
                input_table_file()
    except (ValueError, IndexError):
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


def clear():
    global table
    table = None

def start():
    if table is None:
        input_table()
    print("Выберете тип функции для исследования: ")
    print("1. Линейная функция")
    print("2. Полиномиальная функция 2-й степени")
    print("3. Полиномиальная функция 3-й степени")
    print("4. Экспонециальная функция")
    print("5. Логарифмическая функция")
    print("6. Степенная функция")
    print("7. Все вышестоящие функции")

    number_type = input("Введите тип функции: ")
    if number_type == "1":
        a, b = linial_approx(table[0], table[1])
        print(f"f(x) = {a}x + {b}")
        print(f"x_i: {table[0]}")

    elif number_type == "2":
        a, b, c = bipolin_approx(table[0], table[1])
        print(a, b, c)
    elif number_type == "3": pass
    elif number_type == "4": pass
    elif number_type == "5": pass
    elif number_type == "6": pass
    elif number_type == "7": pass
    else:
        print("Тип не найден")
        start()
