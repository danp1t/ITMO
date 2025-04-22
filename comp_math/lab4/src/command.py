from math_module import *
import math
import matplotlib.pyplot as plt

#Глобальные переменные
table = None

#Команда для вывода списка команд с их описанием
def help():
    print("1. /help - вывести список команд с их описанием")
    print("2. /exit - выход из программы")
    print("3. /info - вывести информацию о введеденных данных")
    print("4. /start - запуск программы")
    print("5. /clear - очистка введенных данных")
    print("6. /input_table - ввод таблицы y = f(x) из консоли")
    print("7. /input_table_file - ввод таблицы y = f(x) из файла")
    print()


def save_to_file(filename, text):
    with open(filename, "w", encoding="utf-8") as f:
        f.write(text)


def format_table(x_data, y_data, y_pred, eps):
    header = "|    x   |    y   |  f(x)  |  eps   |\n"
    separator = "+--------+--------+--------+--------+\n"
    table = separator + header + separator

    for xi, yi, fxi, epsi in zip(x_data, y_data, y_pred, eps):
        row = f"| {xi:6.4f} | {yi:6.4f} | {fxi:6.4f} | {epsi:6.4f} |\n"
        table += row
    table += separator
    return table


def print_results(models, x_data, y_data, filename="results.txt"):
    output = []

    for model in models:
        output.append(f"\n{model['name']}")
        output.append("-" * 50)

        if model["valid"]:
            output.append(f"Коэффициенты: {model['coeffs_str']}")
            output.append(f"Среднеквадратичное отклонение (SSE): {model['sse']:.4f}")
            output.append(f"R² = {model['r_squared']:.4f} ({model['interpretation']})")
            output.append("\n" + format_table(x_data, y_data, model["y_pred"], model["eps"]))
        else:
            output.append("Модель не применима (ошибка в данных)")

        output.append("\n")

    final_output = "\n".join(output)
    print(final_output)
    save_to_file(filename, final_output)


def plot_all_models(x_data, y_data, models):
    plt.figure(figsize=(12, 8))
    colors = plt.cm.tab10(np.linspace(0, 1, len(models)))

    # Исходные данные
    plt.scatter(x_data, y_data, label='Данные', s=100, color='black')

    # Расширенный интервал
    x_min, x_max = np.min(x_data), np.max(x_data)
    x_pad = 0.1 * (x_max - x_min)
    x_ext = np.linspace(x_min - x_pad, x_max + x_pad, 500)

    # Построение всех моделей
    for model, color in zip(models, colors):
        if model['valid']:
            y_pred = model['predict_function'](x_ext)
            print(model['predict_function'](4))
            plt.plot(x_ext, y_pred,
                     label=f"{model['name']} (R²={model['r_squared']:.2f})",
                     color=color,
                     linewidth=2)

    plt.title('Сравнение всех аппроксимирующих моделей')
    plt.xlabel('x')
    plt.ylabel('y')
    plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
    plt.grid(True)
    plt.tight_layout()
    plt.savefig('all_models_comparison.png', dpi=300)

def input_table():
    global table
    x = input("Введите значения x: ")
    y = input("Введите значения y: ")
    try:
        x = list(map(float, x.split()))
        y = list(map(float, y.split()))

        if len(x) != len(y):
            print("Количество чисел должно совпадать")
            input_table()
        if 8 > len(x) or len(x) > 12:
            print("Таблица должна содержать от 8 до 12 точек")
            input_table()
        if len(set(x)) != len(x):
            print("Точки должны быть различными по x")
            input_table()
        table = [x, y]
    except ValueError:
        print("Обнаружена ошибка при вводе")
        input_table()

def input_table_file():
    global table
    file_path = input("Введите путь к файлу: ")
    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()
            x = lines[0]
            y = lines[1]
            try:
                x = list(map(float, x.split()))
                y = list(map(float, y.split()))

                if len(x) != len(y):
                    print("Количество чисел должно совпадать")
                    input_table_file()
                if 8 > len(x) or len(x) > 12:
                    print("Таблица должна содержать от 8 до 12 точек")
                    input_table_file()
                if len(set(x)) != len(x):
                    print("Точки должны быть различными по x")
                    input_table_file()
                table = [x, y]
            except ValueError:
                print("Обнаружена ошибка при вводе")
                input_table_file()
    except (ValueError, IndexError):
        print("Ошибка: некорректный формат данных в файле.")
        input_table_file()
    except FileNotFoundError:
        print(f"Ошибка: файл '{file_path}' не найден")
        input_table_file()

def info():
    if table is not None:
        print(f"Введена таблица: ")
        print(f"Значения x: {table[0]}")
        print(f"Значения y: {table[1]}")


def clear():
    global table
    table = None

def start():
    if table is None:
        input_table()
    models = []
    print("Выберете тип функции для исследования: ")
    print("1. Линейная функция")
    print("2. Полиномиальная функция 2-й степени")
    print("3. Полиномиальная функция 3-й степени")
    print("4. Экспоненциальная функция")
    print("5. Логарифмическая функция")
    print("6. Степенная функция")
    print("7. Исследовать все типы функции")

    x = np.array(table[0], dtype=float)
    y = np.array(table[1], dtype=float)

    number_type = input("Введите тип функции: ")
    if number_type == "1":
        a, b = linial_approx(x, y)
        y_lin =  a * x + b
        sse_lin = np.sum((y - y_lin) ** 2)
        models.append({
            "name": "1. Линейная функция: y = a + b·x",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_lin,
            'predict_function': lambda x: a * x + b,
            "y_pred": y_lin,
            "eps": y - y_lin,
            "valid": True
        })
        r = corr_pirson(x, y)
        print(f"Коэффициент корреляции Пирсона: {r}")

    elif number_type == "2":
        a, b, c = bipolin_approx(x, y)
        y_bi = a*x**2 + b*x + c
        sse_bi = np.sum((y - y_bi) ** 2)
        models.append({
            "name": "2. Полиномиальная функция 2-й степени: y = a·x^2 + b·x + c",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}, c = {c:.4f}",
            "sse": sse_bi,
            "y_pred": y_bi,
            "eps": y - y_bi,
            "valid": True
        })
    elif number_type == "3":
        a, b, c, d = cubic_approx(x, y)
        y_cub = a * x ** 3 + b * x**2 + c*x + d
        sse_cub = np.sum((y - y_cub) ** 2)
        models.append({
            "name": "3. Полиномиальная функция 3-й степени: y = a·x^3 + b·x^2 + c·x + d",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}, c = {c:.4f}, d = {d:.4f}",
            "sse": sse_cub,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })
    elif number_type == "4":
        flag = False
        for i in table[1]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по Y должны быть положительными")
            start()
            clear()
        b, a = exp_approx(x, y)
        y_cub = a*math.e**(b*x)
        sse_cub = np.sum((y - y_cub) ** 2)
        models.append({
            "name": "4. Экспоненциальная функция: y = a·e^(bx)",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_cub,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })
    elif number_type == "5":
        flag = False
        for i in table[0]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по X должны быть положительными")
            start()
            clear()
        a, b = log_approx(x, y)
        y_cub = a * np.log(x) + b
        sse_cub = np.sum((y - y_cub) ** 2)
        models.append({
            "name": "5. Логарифмическая функция: y = a·log(x) + b",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_cub,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })
    elif number_type == "6":
        flag = False
        for i in table[0]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по X должны быть положительными")
            start()
        for i in table[1]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по Y должны быть положительными")
            start()
            clear()
        a, b = power_approx(x, y)
        y_cub = a * (x**b)
        sse_cub = np.sum((y - y_cub) ** 2)
        models.append({
            "name": "6. Степенная функция: y = a * x^b",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_cub,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })

    elif number_type == "7":
        a, b = linial_approx(x, y)
        y_lin = a * x + b
        sse_lin = np.sum((y - y_lin) ** 2)

        sum_1 = np.sum((y - np.mean(y_lin)) ** 2)
        r_2 = 1 - (sse_lin / sum_1) if sum_1 != 0 else 0

        if r_2 >= 0.9:
            interpretation = "отлично объясняет данные (R² ≥ 0.9)"
        elif r_2 >= 0.7:
            interpretation = "хорошо объясняет данные (0.7 ≤ R² < 0.9)"
        elif r_2 >= 0.5:
            interpretation = "удовлетворительно объясняет данные (0.5 ≤ R² < 0.7)"
        else:
            interpretation = "плохо объясняет данные (R² < 0.5)"
        models.append({
            "name": "1. Линейная функция: y = a + b·x",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_lin,
            "r_squared": r_2,
            'predict_function': lambda x: a * x + b,
            "interpretation": interpretation,
            "y_pred": y_lin,
            "eps": y - y_lin,
            "valid": True
        })
        r = corr_pirson(x, y)
        print(f"Коэффициент корреляции Пирсона: {r}")

        a, b, c = bipolin_approx(x, y)
        y_bi = a * x ** 2 + b * x + c
        sse_bi = np.sum((y - y_bi) ** 2)

        sum_1 = np.sum((y - np.mean(y_bi)) ** 2)
        r_2 = 1 - (sse_bi / sum_1) if sum_1 != 0 else 0

        if r_2 >= 0.9:
            interpretation = "отлично объясняет данные (R² ≥ 0.9)"
        elif r_2 >= 0.7:
            interpretation = "хорошо объясняет данные (0.7 ≤ R² < 0.9)"
        elif r_2 >= 0.5:
            interpretation = "удовлетворительно объясняет данные (0.5 ≤ R² < 0.7)"
        else:
            interpretation = "плохо объясняет данные (R² < 0.5)"

        models.append({
            "name": "2. Полиномиальная функция 2-й степени: y = a·x^2 + b·x + c",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}, c = {c:.4f}",
            "sse": sse_bi,
            "r_squared": r_2,
            'predict_function': lambda x: a * x ** 2 + b * x + c,
            "interpretation": interpretation,
            "y_pred": y_bi,
            "eps": y - y_bi,
            "valid": True
        })



        a, b, c, d = cubic_approx(x, y)
        y_cub = a * x ** 3 + b * x ** 2 + c * x + d
        sse_cub = np.sum((y - y_cub) ** 2)

        sum_1 = np.sum((y - np.mean(y_cub)) ** 2)
        r_2 = 1 - (sse_cub / sum_1) if sum_1 != 0 else 0

        if r_2 >= 0.9:
            interpretation = "отлично объясняет данные (R² ≥ 0.9)"
        elif r_2 >= 0.7:
            interpretation = "хорошо объясняет данные (0.7 ≤ R² < 0.9)"
        elif r_2 >= 0.5:
            interpretation = "удовлетворительно объясняет данные (0.5 ≤ R² < 0.7)"
        else:
            interpretation = "плохо объясняет данные (R² < 0.5)"
        models.append({
            "name": "3. Полиномиальная функция 3-й степени: y = a·x^3 + b·x^2 + c·x + d",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}, c = {c:.4f}, d = {d:.4f}",
            "sse": sse_cub,
            "r_squared": r_2,
            'predict_function': lambda x: a * x ** 3 + b * x ** 2 + c * x + d,
            "interpretation": interpretation,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })

        flag = False
        for i in table[1]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по Y должны быть положительными")
            start()
            clear()
        b, a = exp_approx(x, y)
        y_cub = a * math.e ** (b * x)
        sse_cub = np.sum((y - y_cub) ** 2)
        sum_1 = np.sum((y - np.mean(y_cub)) ** 2)
        r_2 = 1 - (sse_cub / sum_1) if sum_1 != 0 else 0

        if r_2 >= 0.9:
            interpretation = "отлично объясняет данные (R² ≥ 0.9)"
        elif r_2 >= 0.7:
            interpretation = "хорошо объясняет данные (0.7 ≤ R² < 0.9)"
        elif r_2 >= 0.5:
            interpretation = "удовлетворительно объясняет данные (0.5 ≤ R² < 0.7)"
        else:
            interpretation = "плохо объясняет данные (R² < 0.5)"
        print(f"{a} * e^{b}x")
        models.append({
            "name": "4. Экспоненциальная функция: y = a·e^(bx)",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_cub,
            "r_squared": r_2,
            'predict_function': lambda x: a * np.exp(b*x),
            "interpretation": interpretation,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })

        flag = False
        for i in table[0]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по X должны быть положительными")
            start()
            clear()
        a, b = log_approx(x, y)
        y_cub = a * np.log(x) + b
        sse_cub = np.sum((y - y_cub) ** 2)
        sum_1 = np.sum((y - np.mean(y_cub)) ** 2)
        r_2 = 1 - (sse_cub / sum_1) if sum_1 != 0 else 0

        if r_2 >= 0.9:
            interpretation = "отлично объясняет данные (R² ≥ 0.9)"
        elif r_2 >= 0.7:
            interpretation = "хорошо объясняет данные (0.7 ≤ R² < 0.9)"
        elif r_2 >= 0.5:
            interpretation = "удовлетворительно объясняет данные (0.5 ≤ R² < 0.7)"
        else:
            interpretation = "плохо объясняет данные (R² < 0.5)"
        models.append({
            "name": "5. Логарифмическая функция: y = a·log(x) + b",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_cub,
            'predict_function': lambda x: a * np.log(x) + b,
            "r_squared": r_2,
            "interpretation": interpretation,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })

        flag = False
        for i in table[0]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по X должны быть положительными")
            start()
        for i in table[1]:
            if i < 0:
                flag = True
        if flag:
            print("Аппроксимация невозможна. Значения по Y должны быть положительными")
            start()
            clear()
        a, b = power_approx(x, y)
        y_cub = a * (x ** b)
        sse_cub = np.sum((y - y_cub) ** 2)
        sum_1 = np.sum((y - np.mean(y_cub)) ** 2)
        r_2 = 1 - (sse_cub / sum_1) if sum_1 != 0 else 0

        if r_2 >= 0.9:
            interpretation = "отлично объясняет данные (R² ≥ 0.9)"
        elif r_2 >= 0.7:
            interpretation = "хорошо объясняет данные (0.7 ≤ R² < 0.9)"
        elif r_2 >= 0.5:
            interpretation = "удовлетворительно объясняет данные (0.5 ≤ R² < 0.7)"
        else:
            interpretation = "плохо объясняет данные (R² < 0.5)"
        models.append({
            "name": "6. Степенная функция: y = a * x^b",
            "coeffs_str": f"a = {a:.4f}, b = {b:.4f}",
            "sse": sse_cub,
            'predict_function': lambda x: a * (x ** b),
            "r_squared": r_2,
            "interpretation": interpretation,
            "y_pred": y_cub,
            "eps": y - y_cub,
            "valid": True
        })
        if models:
            best_model = max(models, key=lambda m: m["r_squared"])
            print("\nНаилучшая модель:")
            print(f"Тип: {best_model['name']}")
            print(f"Коэффициенты: {best_model['coeffs_str']}")
            print(f"R²: {best_model['r_squared']:.4f}")
            print(f"Интерпретация: {best_model['interpretation']}")

        else:
            print("Ни одна модель не может быть рассчитана")

        plot_all_models(x, y, models)



    else:
        print("Тип не найден")
        start()
        clear()
    print_results(models, x, y)
