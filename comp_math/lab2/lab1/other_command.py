import command

#Информация о лабораторной работе
def info_about_lab():
    print("Лабораторная работа №2")
    print("Работа сделана Путинцевым Данилом, ИСУ: 409425")
    print("Вариант №12")
    print("Численное решение нелинейных уравнений и систем")
    print()

#Приветственное сообщение
def welcome_message():
    print("Добро пожаловать в программу, которая решает нелинейные уравнения и системы")
    print("Нелинейные уравнения решает методами половинного деления, Ньютона и простой итерации")
    print("Системы решает методом Ньютона")
    print("Список команд доступен по команде /help")
    print()

def validate_task(task):
    if task == "/help" or task =="0": command.help()
    elif task == "/exit" or task == "1": exit(0)
    elif task == "/info" or task == "2": command.info()
    elif task == "/start" or task == "3": command.start()
    elif task == "/clear" or task == "4": command.clear()
    elif task == "/choice_equation" or task == "5": command.choice_equation()
    elif task == "/choice_system" or task == "6": command.choice_system()
    elif task == "/input_interval" or task == "7": command.input_interval()
    elif task == "/input_epsilon" or task == "8": command.input_epsilon()
    elif task == "/input_approximation" or task == "9": command.input_approximation()
    elif task == "/input_file_interval" or task == "10": command.input_file_interval()
    elif task == "/input_file_epsilon" or task == "11": command.input_file_epsilon()
    elif task == "/input_file_approximation" or task == "12": command.input_file_approximation()
    else: print("Команда не найдена")