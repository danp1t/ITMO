import command

#Информация о лабораторной работе
def info_about_lab():
    print("Лабораторная работа №1")
    print("Работа сделана Путинцевым Данилом, ИСУ: 409425")
    print("Вариант №12")
    print("Метод Гаусса-Зейделя")
    print()

#Приветственное сообщение
def welcome_message():
    print("Добро пожаловать в программу, которая решает СЛАУ с помошью метода Гаусса-Зейделя")
    print("Список команд доступен по команде /help")
    print()

def validate_task(task):
    if task == "/help": command.help()
    elif task == "/info": command.info()
    elif task == "/exit": exit(0)
    elif task == "/input_n": command.input_n()
    elif task == "/input_epsilon": command.input_epsilon()
    elif task == "/input_n_file": command.input_n_file()
    elif task == "/input_epsilon_file": command.input_epsilon_file()
    elif task == "/input_matrix": command.input_matrix()
    elif task == "/input_matrix_file": command.input_matrix_file()
    else: print("Команда не найдена")