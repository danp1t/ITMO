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
    #print("Добро пожаловать в программу, которая решает СЛАУ с помошью метода Гаусса-Зейделя")
    print("Список команд доступен по команде /help")
    print()

def validate_task(task):
    if task == "/help" or task == "1": command.help()
    elif task == "/exit" or task == "2": exit(0)
    elif task == "/info" or task == "3": command.info()
    elif task == "/start" or task == "4": command.start()
    elif task == "/clear" or task == "5": command.clear()
    elif task == "/choice_system" or task == "6": command.choice_system()
    elif task == "/choice_equations" or task == "7": command.choice_equations()
    elif task == "/input_interval" or task == "8": command.input_interval()
    elif task == "/input_interval_file" or task == "9": command.input_interval_file()
    elif task == "/input_start_value" or task == "10": command.input_start_value()
    elif task == "/input_start_value_file" or task == "11": command.input_start_value_file()
    elif task == "/input_epsilon" or task == "12": command.input_epsilon()
    elif task == "/input_epsilon_file" or task == "13": command.input_epsilon_file()
    else: print("Команда не найдена")
