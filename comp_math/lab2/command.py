import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import re
from math_module import *

#Глобальные переменные
type_n = None # Тип решения (система или уравнения)
systems = [("sin(x - y) - x*y = -1", "0.3x^2 + y^2 = 2"), ("sin(y) + 2x = 2", "y + cos(x - 1) = 0.7")] # Системы нелинейных уравнений
equations = [("2x^3 + 3.41x^2 - 1.943x + 2.12"), ("sin(x) + cos(x) - 0.4 = 0.2"), ("tg(x) - 2.34 = 21"), ("-3.2x^3 - 3.2x = 2"), ("-33x^3 + 21.23x^2 + 3 = 2.32")] #Нелинейные уравнения, доступные на выбор
system = None #Текущая система
equation = None #Текущее уравнение
epsilon = None #Погрешность
start_value = None #Начальное приближение к корню
interval = None #Интервал
results = None

#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /choice_system - выбор системы")
    print("7. /choice_equations - выбор уравнения")
    print("8. /input_interval - ввод интервала с клавиатуры")
    print("9. /input_interval_file - ввод интервала из файла")
    print("10. /input_start_value - ввод начального приближения с клавиатуры")
    print("11. /input_start_value_file - ввод начального приблежения из файла")
    print("12. /input_epsilon - ввод погрешности с клавиатуры")
    print("13. /input_epsilon_file - ввод погрешности из файла")
    print()

def input_interval():
    global interval
    try:
        a = float(input("Введите нижнюю границу интервала: "))
        b = float(input("Введите верхнюю границу интервала: "))
        if a >= b:
            print("Ошибка: нижняя граница должна быть меньше верхней. Попробуйте снова.")
            input_interval()
        else:
            interval = (a, b)
    except ValueError:
        print("Ошибка: введите числовые значения. Попробуйте снова.")
        input_interval()

def input_interval_file():
    global interval
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()
            a, b = map(float, lines[0].split())
            if a >= b:
                print("Ошибка: нижняя граница должна быть меньше верхней.")
                input_interval_file()
            else:
                interval = (a, b)
    except (ValueError, IndexError):
        print("Ошибка: некорректный формат данных в файле.")
        input_interval_file()
    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_interval_file()


def parse_equation(eq_str):
    eq_str = eq_str.replace('sin', 'np.sin')
    eq_str = eq_str.replace('cos', 'np.cos')
    eq_str = eq_str.replace('tan', 'np.tan')
    eq_str = eq_str.replace('tg', 'np.tan')
    eq_str = eq_str.replace('sqrt', 'np.sqrt')
    eq_str = eq_str.replace('^', '**')

    eq_str = re.sub(r'(?<=[0-9)])(?=[a-zA-Z])', '*', eq_str)
    eq_str = re.sub(r'(?<=[a-zA-Z])(?=[0-9])', '*', eq_str)

    if '=' in eq_str:
        left, right = eq_str.split('=', 1)
        expr = f'({left.strip()}) - ({right.strip()})'
    else:
        expr = eq_str.strip()

    return expr


def plot_equation(equation_str, interval):
    expr = parse_equation(equation_str)
    a, b = interval
    x = np.linspace(a, b, 400)

    try:
        y = eval(expr, {'np': np, 'x': x})
    except Exception as e:
        print(f"Ошибка при вычислении уравнения: {e}")
        return

    plt.figure(figsize=(10, 6))
    plt.plot(x, y, label=f'f(x) = {equation_str}')
    plt.axhline(0, color='black', linewidth=0.5)
    plt.axvline(0, color='black', linewidth=0.5)
    plt.xlabel('x')
    plt.ylabel('f(x)')
    plt.title('График уравнения')
    plt.grid(True)
    plt.legend()

    plt.savefig('equation_plot.png')
    print("График сохранен как 'equation_plot.png'")

    if matplotlib.get_backend().lower() not in ['agg', 'pdf']:
        try:
            plt.show(block=True)
        except Exception as e:
            print(f"Ошибка отображения графика: {e}")
    else:
        plt.close()


def plot_system(system_str):
    eq1, eq2 = system_str
    expr1 = parse_equation(eq1)
    expr2 = parse_equation(eq2)

    x = np.linspace(-5, 5, 400)
    y = np.linspace(-5, 5, 400)
    X, Y = np.meshgrid(x, y)

    try:
        Z1 = eval(expr1, {'np': np, 'x': X, 'y': Y})
        Z2 = eval(expr2, {'np': np, 'x': X, 'y': Y})
    except Exception as e:
        print(f"Ошибка при вычислении системы: {e}")
        return

    plt.figure(figsize=(10, 6))

    cs1 = plt.contour(X, Y, Z1, levels=[0], colors='r', linewidths=2)
    cs2 = plt.contour(X, Y, Z2, levels=[0], colors='b', linewidths=2)

    from matplotlib.lines import Line2D
    legend_elements = [
        Line2D([0], [0], color='r', lw=2, label=eq1),
        Line2D([0], [0], color='b', lw=2, label=eq2)
    ]

    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('График системы уравнений')
    plt.legend(handles=legend_elements)
    plt.grid(True)

    plt.savefig('system_plot.png')
    print("График системы сохранен как 'system_plot.png'")



def input_start_value():
    global start_value
    try:
        start_value = float(input("Введите начальное приближение: "))
    except ValueError:
        print(f"Ошибка: Получено не число: {start_value}")
        return input_start_value()

def input_start_value_file():
    global start_value
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            content = file.read().strip()
            numbers = content.split()

            if len(numbers) != 1:
                print(f"Ошибка: в файле должно быть ровно одно число. Найдено: {len(numbers)}")
                return input_start_value_file()
            try:
                start_value = float(numbers[0])
            except ValueError:
                print(f"Ошибка: невозможно преобразовать '{numbers[0]}' в float")
                return input_start_value_file()

    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_start_value_file()

def input_system_start_values():
    print("Введите начальные приближения x0 и y0 через пробел:")
    while True:
        try:
            x0, y0 = map(float, input().split())
            return x0, y0
        except:
            print("Ошибка! Введите два числа через пробел.")

def input_epsilon():
    global epsilon
    try:
        epsilon = float(input("Введите точность: "))
        if epsilon <= 0:
            print("Точность должна быть больше 0")
            input_epsilon()
    except ValueError:
        print("Ошибка: введено не число")
        input_epsilon()

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

def choice_system():
    global system
    print("Выберите систему нелинейных уравнений")
    for i in range(len(systems)):
        print(f"{i}. {systems[i]}")
    try:
        system = int(input("Введите номер системы: "))
        if system >= len(systems):
            print(f"Ошибка: system должно быть в указанном диапазоне. Получено: {system}")
            return choice_system()
        if system < 0:
            print(f"Ошибка: system должен быть неотрицательным. Получено: {system}")
            return choice_system()
    except ValueError:
        print(f"Ошибка: номер system должен быть целым числом. Получено: {system}")
        return choice_system()


def choice_equations():
    global equation
    for i in range(len(equations)):
        print(f"{i}. {equations[i]}")
    try:
        equation = int(input("Введите номер нелинейного уравнения: "))
        if equation >= len(equations):
            print(f"Ошибка: номер equation должен быть в указаном диапазоне. Получено: {equation}")
            return choice_equations()
        if equation < 0:
            print(f"Ошибка: номер equation должен быть неотрицательным. Получено: {equation}")
            return choice_equations()
    except ValueError:
        print(f"Ошибка: номер equation должен быть целым числом. Получено: {equation}")
        return choice_equations()

def info():
    if system is not None:
        print(f"Выбрана система: {systems[system]}")
    if equation is not None:
        print(f"Выбрано уравнение: {equations[equation]}")
    if interval is not None:
        print(f"Интервал: {interval}")
    if epsilon is not None:
        print(f"Погреность: {epsilon}")
    if start_value is not None:
        print(f"Начальное приближение: {start_value}")

def save_results_to_file(filename, results):
    with open(filename, 'w') as f:
        for key, value in results.items():
            f.write(f"{key}: {value}\n")
    print(f"\nРезультаты сохранены в файл '{filename}'")

def print_results(results):
    print("\n════════ Результаты ════════")
    for key, value in results.items():
        print(f"{key}: {value}")
    print("════════════════════════════")

def get_output_choice():
    print("\nВыберите способ вывода результатов:")
    print("1. Вывести на экран")
    print("2. Сохранить в файл")
    print("3. Сделать и то, и другое")
    choice = input("Ваш выбор (1-3): ")
    return choice

def prepare_results(method_name, eq_str, root, f_val, iterations, additional=None):
    results = {
        "Метод": method_name,
        "Уравнение": eq_str,
        "Приближенный корень": f"{root:.8f}",
        "Значение функции в корне": f"{f_val:.2e}",
        "Количество итераций": iterations
    }
    if additional:
        results.update(additional)
    return results

def start():
    global results
    print("Выберете, что будем решать")
    print("1. Нелинейное уравнение")
    print("2. Систему нелинейных уравнений")
    number = input()
    if number == "1":
        choice_equations()
        if interval is None:
            print("Для построения графика введите интервал.")
            input_interval()
        eq = equations[equation]
        plot_equation(eq, interval)

        print("Выберете способ решения нелинейного уравнения")
        print("1. Метод половинного деления")
        print("2. Метод Ньютона")
        print("3. Метод простой итерации")
        variant = input()

        eq_str = equations[equation]
        parsed_expr = parse_equation(eq_str)

        try:
            f = lambda x: eval(parsed_expr, {'np': np, 'x': x})
        except:
            print("Ошибка в преобразовании уравнения")
            return

        if variant == "1":
            if interval is None:
                print("Требуется ввод интервала.")
                input_interval()
            if epsilon is None:
                print("Требуется ввод точности.")
                input_epsilon()


            try:
                a, b = interval
                f = lambda x: eval(parsed_expr, {'np': np, 'x': x})

                verified_interval = verify_interval(f, (a, b))
                print(f"Корень существует на интервале: {verified_interval}")

                if (b - a) > 1.0:
                    print("Рекомендуется сузить интервал для повышения точности")
                root, f_val, iterations = bisection_method(f, a, b, epsilon)
                results = prepare_results(
                    "Метод половинного деления",
                    eq_str, root, f_val, iterations,
                    {"Интервал": f"[{a:.4f}, {b:.4f}]"}
                )
            except ValueError as e:
                print(f"\nОшибка: {e}")

        elif variant == "2":
            if interval is None:
                print("Требуется ввод интервала.")
                input_interval()
            if epsilon is None:
                print("Требуется ввод точности.")
                input_epsilon()
            start_value = find_optimal_newton_start(f, interval, n_samples=100)
            try:
                check_newton_conditions(f, start_value, epsilon, interval)
                verify_interval(f, interval)

                root, f_val, iters, history = newton_method(f, start_value, epsilon)
                results = prepare_results(
                    "Метод Ньютона",
                    eq_str, root, f_val, iters,
                    {
                        "Начальное приближение": f"{start_value:.4f}",
                        "Погрешность": epsilon
                    }
                )
            except RuntimeError as e:
                print(f"Метод Ньютона не сошелся: {e}")
            except ValueError as e:
                print(e)
        elif variant == "3":
            if interval is None:
                print("Требуется ввод интервала.")
                input_interval()
            if epsilon is None:
                print("Требуется ввод точности.")
                input_epsilon()
            start_value = find_best_start_point(f, interval)
            try:
                a, b = interval
                M = (f(start_value + 1e-6) - f(start_value - 1e-6)) / 2e-6
                phi = lambda x: x - f(x) / M
                check_iteration_conditions(phi, a, b)

                root, f_val, iters, history = simple_iteration_method(
                    f, start_value, epsilon
                )

                results = prepare_results(
                    "Метод простой итерации",
                    eq_str, root, f_val, iters,
                    {
                        "Начальное приближение": f"{start_value:.4f}",
                        "Параметр M": f"{M:.4f}"
                    }
                )

            except RuntimeError as e:
                print(f"\nОшибка: {e}")
            except ValueError as e:
                print(f"Ошибка сходимости: {e}")
        else:
            print("Нужно ввести номер способа решения")
            start()

        if results is not None:
            output_choice = get_output_choice()

            if output_choice == "1":
                print_results(results)
            elif output_choice == "2":
                filename = input("Введите имя файла для сохранения (например: results.txt): ")
                save_results_to_file(filename, results)
            elif output_choice == "3":
                print_results(results)
                filename = input("Введите имя файла для сохранения (например: results.txt): ")
                save_results_to_file(filename, results)
            else:
                print("Неверный выбор, результаты будут выведены на экран")
                print_results(results)

    elif number == "2":
        choice_system()
        system_eq = systems[system]
        plot_system(system_eq)
        eq1, eq2 = system_eq
        try:
            parsed_eq1 = parse_equation(eq1)
            parsed_eq2 = parse_equation(eq2)
            f1 = lambda x, y: eval(parsed_eq1, {'np': np, 'x': x, 'y': y})
            f2 = lambda x, y: eval(parsed_eq2, {'np': np, 'x': x, 'y': y})
        except Exception as e:
            print(f"Ошибка преобразования уравнений: {str(e)}")
            return
        system_start_values = input_system_start_values()
        if epsilon is None:
            input_epsilon()
        try:
            (root_x, root_y), iterations, history = newton_system_solver(f1, f2, system_start_values[0], system_start_values[1], epsilon)
            print(f"Найденное решение:")
            print(f"x = {root_x:.8f}")
            print(f"y = {root_y:.8f}")
            print(f"\nЗначения функций в решении:")
            print(f"f1(x,y) = {f1(root_x, root_y):.2e}")
            print(f"f2(x,y) = {f2(root_x, root_y):.2e}")
            print(f"\nОбщее количество итераций: {iterations}")

            print("\nИстория итераций:")
            print("Итер |      x      |      y      |    Δx    |    Δy    |   f1(x,y)   |   f2(x,y)")
            print("-----|-------------|-------------|----------|----------|-------------|----------")
            for step in history:
                print(f"{step['iteration']:4d} | {step['x']:11.6f} | {step['y']:11.6f} | "
                      f"{step['delta_x']:8.2e} | {step['delta_y']:8.2e} | "
                      f"{step['f1']:11.2e} | {step['f2']:10.2e}")
        except RuntimeError as e:
            print(f"Ошибка решения: {str(e)}")
        except Exception as e:
            print(f"Неожиданная ошибка: {str(e)}")

    else:
        print("Нужно ввести число 1 или 2")
        start()
    clear()

def clear():
    global system, equation, epsilon, start_value, interval
    print("Сброс всех значений")
    system = None
    equation = None
    epsilon = None
    start_value = None
    interval = None
