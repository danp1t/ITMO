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
    pass

def input_table_file():
    pass

def info():
    if table is not None:
        #Вывод переделать
        print(f"Введена таблица: {table}")

def clear():
    global table
    table = None

def start():
    if table is None:
        input_table()
