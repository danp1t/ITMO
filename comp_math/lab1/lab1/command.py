import os

#Команда для выхода из программы
def exit():
    os._exit(0)

#Команда для вывода списка команд с их описанием
def help():
    print("/help - вывести список команд с их описанием")
    print("/exit - выход из программы")
    print("/info - вывести информацию о введеденных данных")
    print("/start - запуск программы")
    print("/full_input_file - ввод всей информации (точности и матрицы) из файла")
    print("/input_matrix_file - ввод матрицы из файла")
    print("/input_epsilon_file - ввод точности из файла")
    print("/input_matrix - ввод матрицы с клавиатуры")
    print("/input_epsilon - ввод точности с клавиатуры")

def info():
    pass
def full_input_file():
    pass
def input_matrix_file():
    pass
def input_epsilon_file():
    pass
def input_matrix():
    pass
def input_epsilon():
    pass
def start():
    pass

#Список команд
commands = {
    "/help" : help(),
    "/exit" : exit(),
    "/info" : info(),
    "/start" : start(),
    "/full_input_file" : full_input_file(),
    "/input_matrix_file" : input_matrix_file(),
    "/input_epsilon_file" : input_epsilon_file(),
    "/input_matrix" : input_matrix(),
    "/input_epsilon" : input_epsilon()
}