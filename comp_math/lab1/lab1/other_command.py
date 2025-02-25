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
    match task:
        case "/help": command.help()
        case "/info" :command.info()
        case "/exit": exit(0)
        case "/input_n": command.input_n()
        case "/input_epsilon": command.input_epsilon()
        case "/input_n_file": command.input_n_file()
        case "/input_epsilon_file": command.input_epsilon_file()
        case "/input_matrix": command.input_matrix()
        case "/input_matrix_file": command.input_matrix_file()
        case "/input_full_file": command.input_full_file()
        case "/start": command.start()
        case "/clear": command.clear()
        case _: print("Команда не найдена")