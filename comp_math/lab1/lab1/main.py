import other_command
import command

# Сделать валидацию входных данных
# Сделать ввод данных из консоли
# Сделать ввод данных из файла
# Реализовать алгоритм: Метод Гаусса-Зейделя

other_command.info_about_lab()
other_command.welcome_message()
command.help()

# Старт программы
while True:
    task = input("Введите команду: ")
    other_command.validate_task(task)




