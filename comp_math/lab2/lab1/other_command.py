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
    match task:
        case "/help": command.help()
        case "/info": command.info()
        case "/exit": exit(0)
        case "/start": command.start()
        case "/clear": command.clear()
        case _: print("Команда не найдена")