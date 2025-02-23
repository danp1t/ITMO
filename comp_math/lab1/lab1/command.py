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
    if n is not None:
        print("Размерность: ", n)
    if matrix is not None:
        print("Матрица: ")
        for i in range(n):
            for j in range(n):
                print(matrix[i][j], end=" ")
            print()
    print()



def full_input_file():
    pass
def input_matrix_file():
    pass

def input_n_file():
    global n
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            content = file.read().strip()
            numbers = content.split()

            if len(numbers) != 1:
                print(f"Ошибка: в файле должно быть ровно одно число. Найдено: {len(numbers)}")
                input_n_file()

            try:
                if float(numbers[0]) == int(numbers[0]):
                    n = int(numbers[0])
                    if n <= 0:
                        print("Размерность должна быть больше 0")
                        input_n_file()
                    elif n > 20:
                        print("Размерность должна быть меньше 20")
                        input_n_file()
                else:
                    print("Ошибка: введено не целое число")
            except ValueError:
                print(f"Ошибка: невозможно преобразовать '{numbers[0]}' в int")
                input_n_file()

    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_n_file()

def input_epsilon_file():
    global epsilon
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            content = file.read().strip()
            numbers = content.split()

            if len(numbers) != 1:
                print(f"Ошибка: в файле должно быть ровно одно число. Найдено: {len(numbers)}")
                input_epsilon_file()

            try:
                epsilon = float(numbers[0])
            except ValueError:
                print(f"Ошибка: невозможно преобразовать '{numbers[0]}' в float")
                input_epsilon_file()

    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_epsilon_file()


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


