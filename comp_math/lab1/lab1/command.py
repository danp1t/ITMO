import math_module

#Глобальные данные
matrix = None
epsilon = None
n = None  #размерность матрицы


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
            for j in range(n + 1):
                print(matrix[i][j], end=" ")
            print()
    print()


def input_full_file():
    global n, epsilon, matrix
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            lines = [line.strip() for line in file if line.strip()]

            if len(lines) < 2:
                print("Ошибка: файл должен содержать как минимум две строки (n и epsilon)")
                return input_full_file()

            n_str = lines[0]
            try:
                n_float = float(n_str)
            except ValueError:
                print(f"Ошибка: n должно быть числом. Получено: {n_str}")
                return input_full_file()

            if not n_float.is_integer():
                print(f"Ошибка: n должно быть целым числом. Получено: {n_str}")
                return input_full_file()

            n = int(n_float)
            if n <= 0 or n > 20:
                print(f"Ошибка: n должно быть от 1 до 20. Получено: {n}")
                return input_full_file()

            epsilon_str = lines[1]
            try:
                epsilon = float(epsilon_str)
            except ValueError:
                print(f"Ошибка: epsilon должно быть числом. Получено: {epsilon_str}")
                return input_full_file()

            matrix_lines = lines[2:]
            if len(matrix_lines) != n:
                print(f"Ошибка: для матрицы ожидается {n} строк, получено {len(matrix_lines)}")
                return input_full_file()

            matrix = []
            for i, line in enumerate(matrix_lines, 1):
                elements = line.split()
                if len(elements) != n + 1:
                    print(f"Ошибка в строке матрицы {i}: ожидается {n + 1} элементов, получено {len(elements)}")
                    return input_full_file()
                try:
                    row = list(map(float, elements))
                    matrix.append(row)
                except ValueError:
                    print(f"Ошибка в строке матрицы {i}: нечисловой элемент")
                    return input_full_file()

    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        return input_full_file()
    except PermissionError:
        print(f"Ошибка: нет прав для чтения файла '{file_path}'")
        return input_full_file()
    except Exception as e:
        print(f"Неизвестная ошибка: {str(e)}")
        return input_full_file()


def input_matrix_file():
    global matrix
    file_path = input("Введите путь к файлу: ")
    if n is None:
        print("Ошибка: сначала введите размерность матрицы")
        input_n()
    matrix = []
    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()

            non_empty_lines = [line.strip() for line in lines if line.strip()]

            if len(non_empty_lines) != n:
                print(f"Ошибка: файл должен содержать ровно {n} строк. Найдено: {len(non_empty_lines)}")
                input_matrix_file()

            for i, line in enumerate(non_empty_lines, 1):
                elements = line.split()

                if len(elements) != n + 1:
                    print(f"Ошибка в строке {i}: должно быть {n + 1} элементов. Найдено: {len(elements)}")
                    input_matrix_file()

                try:
                    row = list(map(float, elements))
                    matrix.append(row)
                except ValueError:
                    print(f"Ошибка в строке {i}: нечисловой элемент")
                    input_matrix_file()


    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_matrix_file()
    except PermissionError:
        print(f"Ошибка: нет прав для чтения файла '{file_path}'")
        input_matrix_file()
    except Exception as e:
        print(f"Неизвестная ошибка: {str(e)}")
        input_matrix_file()


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
    global n, matrix
    if n is None:
        print("Ошибка: сначала введите размерность матрицы")
        input_n()
    matrix = []
    print(f"\nВведите матрицу {n}x{n} построчно. Числа разделяйте пробелами:")

    for i in range(n):
        while True:
            row_input = input(f"Строка {i + 1}: ").strip()
            elements = row_input.split()

            if len(elements) != n + 1:
                print(f"Ошибка: нужно ввести ровно {n + 1} чисел")
                continue

            try:
                row = list(map(float, elements))
                matrix.append(row)
                break
            except ValueError:
                print("Ошибка: все элементы должны быть числами")


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
    if matrix is None:
        print("Необходимо ввести матрицу")
        input_matrix()
    if epsilon is None:
        print("Необходимо ввести точность")
        input_epsilon()
    print("Ответ: ", end="")
    print(math_module.seidel(matrix, epsilon, 10))
