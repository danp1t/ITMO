import command
import other_command

# Сделать валидацию входных данных
# Сделать ввод данных из консоли
# Сделать ввод данных из файла
# Реализовать алгоритм: Метод Гаусса-Зейделя


other_command.info_about_lab()
# Старт программы
while True:
    other_command.welcome_message()
    command.help()
    task = input("Введите команду: ")
    try:
        command.commands[task]
    except:
        print("Неизвестная команда. Посмотреть список доступных команд можно с помощью /help")
        continue



