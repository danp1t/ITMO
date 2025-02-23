#Глобальные данные
matrix = None
epsilon = None
n = None #размерность матрицы

#Команда для вывода списка команд с их описанием
def help():
    print("/help - вывести список команд с их описанием")
    print("/exit - выход из программы")
    print("/info - вывести информацию о введеденных данных")
    print("/start - запуск программы")
    print("/full_input_file - ввод всей информации (размерность, точность, матрица) из файла")
    print("/input_matrix_file - ввод матрицы из файла")
    print("/input_epsilon_file - ввод точности из файла")
    print("/input_n_file - ввод размера матрицы из файла")
    print("/input_matrix - ввод матрицы с клавиатуры")
    print("/input_epsilon - ввод точности с клавиатуры")
    print("/input_n - ввод размера матрицы с клавиатуры")
    print()

def info():
    if epsilon is not None:
        print("Точность: ", epsilon)
    elif n is not None:
        print("Размерность: ", n)
    elif matrix is not None:
        print("Матрица: ")
        for i in range(n):
            for j in range(n):
                print(matrix[i][j], end=" ")
            print()
    else:
        print("Данные не введены")
    print()



def full_input_file():
    pass
def input_matrix_file():
    pass
def input_epsilon_file():
    pass
def input_matrix():
    pass
def input_epsilon():
    global epsilon
    epsilon = float(input("Введите точность: "))
def input_n():
    global n
    n = int(input("Введите размерность матрицы: "))
    if n <= 0:
        print("Размерность должна быть больше 0")
        input_n()
    elif n > 20:
        print("Размерность должна быть меньше 20")
        input_n()
def start():
    pass


