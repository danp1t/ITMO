import numpy as np
import math

def create_divided_differences(table):
    x = table[0]
    y = table[1]
    n = len(x)

    diff_table = [y.copy()]

    for i in range(1, n):
        level = []
        for j in range(n - i):
            delta = (diff_table[i - 1][j + 1] - diff_table[i - 1][j]) / (x[j + i] - x[j])
            level.append(delta)
        diff_table.append(level)

    return diff_table

def create_difference_table(table):
    y_values = table[1]
    difference_table = [y_values.copy()]

    current_level = y_values
    while len(current_level) > 1:
        next_level = []
        for i in range(1, len(current_level)):
            next_level.append(current_level[i] - current_level[i - 1])
        difference_table.append(next_level)
        current_level = next_level

    return difference_table

def method_langrange(x, table):
    x_values = table[0]
    y_values = table[1]
    n = len(x_values)
    result = 0.0

    for i in range(n):
        term = y_values[i]
        for j in range(n):
            if j != i:
                term *= (x - x_values[j]) / (x_values[i] - x_values[j])
        result += term

    return result


def method_gauss(x, table):
    x_values = np.array(table[0])
    y_values = np.array(table[1])
    n = len(x_values)

    # Проверка равноотстоящих узлов
    h = x_values[1] - x_values[0]
    if not np.allclose(np.diff(x_values), h, atol=1e-6):
        raise ValueError("Узлы не равноотстоящие")

    # Определение центрального узла
    mid_idx = np.argmin(np.abs(x_values - x))
    mid_idx = max(1, min(mid_idx, n - 2))
    a = x_values[mid_idx]

    # Корректный расчет параметра t
    t = (x - a) / h

    # Автоматический выбор формулы
    use_first_formula = (t < 0.5 and t >= -0.5)

    # Таблица конечных разностей
    diff_table = create_difference_table(table)

    result = y_values[mid_idx]
    product = 1.0

    for i in range(1, len(diff_table)):
        # Корректный выбор индекса разности
        if use_first_formula:
            idx = mid_idx - (i // 2)
        else:
            idx = mid_idx - (i // 2) + (i % 2)

        # Проверка границ
        if idx < 0 or idx >= len(diff_table[i]):
            break

        delta = diff_table[i][idx]

        # Корректный расчет множителя
        term = 1.0
        for j in range(i):
            if use_first_formula:
                k = j - (i // 2)
            else:
                k = (i // 2 - 1) + j
            term *= (t - k)

        result += delta * term / math.factorial(i)

    return result

def method_newton(x, table):
    x_nodes = table[0]
    y_nodes = table[1]
    n = len(x_nodes)

    diff_table = create_divided_differences(table)

    result = diff_table[0][0]
    product = 1.0

    for i in range(1, n):
        product *= (x - x_nodes[i - 1])
        result += diff_table[i][0] * product

    return result