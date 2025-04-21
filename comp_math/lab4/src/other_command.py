import command

#Информация о лабораторной работе
def info_about_lab():
    print("Лабораторная работа №4")
    print("Работа сделана Путинцевым Данилом, ИСУ: 409425")
    print("Вариант №12")
    print("Аппроксимация функции методом наименьших квадратов")
    print()

#Приветственное сообщение
def welcome_message():
    #print("Добро пожаловать в программу, которая осуществляет численное интегрирование")
    print("Список команд доступен по команде /help")
    print()

def validate_task(task):
    if task == "/help" or task == "1": command.help()
    elif task == "/exit" or task == "2": exit(0)
    elif task == "/info" or task == "3": command.info()
    elif task == "/start" or task == "4": command.start()
    elif task == "/clear" or task == "5": command.clear()
    elif task == "/input_table" or task == "6": command.input_table()
    elif task == "/input_table_file" or task == "6": command.input_table_file()
    else: print("Команда не найдена")
