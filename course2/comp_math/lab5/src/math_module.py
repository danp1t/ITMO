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


def create_central_difference_table(table):
    n = len(table[0])
    diff_table = [list(table[1])]

    for i in range(1, n):
        current_diff = []
        for j in range(n - i):
            if i % 2 == 1:
                current_diff.append(diff_table[i - 1][j + 1] - diff_table[i - 1][j])
            else:
                current_diff.append((diff_table[i - 1][j + 1] - diff_table[i - 1][j - 1]) / 2)
        diff_table.append(current_diff)

    return diff_table


def create_bessel_difference_table(table):
    x, y = table[0], table[1]
    n = len(x)
    diff_table = [list(y)]

    for i in range(1, n):
        current_diff = []
        for j in range(n - i):
            current_diff.append(diff_table[i - 1][j + 1] - diff_table[i - 1][j])
        diff_table.append(current_diff)

    return diff_table


def method_bessel(x, table):
    x_values = np.array(table[0])
    y_values = np.array(table[1])
    n = len(x_values)

    if n < 2:
        return y_values[0] if n == 1 else 0.0

    h = x_values[1] - x_values[0]
    if not np.allclose(np.diff(x_values), h, atol=1e-6):
        return -1

    mid_idx = np.argmin(np.abs(x_values - x))
    mid_idx = min(max(mid_idx, 1), n - 2)

    a = x_values[mid_idx]
    t = (x - a) / h

    diff_table = create_bessel_difference_table(table)

    result = y_values[mid_idx]
    term = 1.0

    for i in range(1, len(diff_table)):
        idx = mid_idx - (i // 2)
        if idx < 0 or idx + i >= len(diff_table[i]):
            break

        delta = diff_table[i][idx]
        term *= (t - (-1) ** i * (i // 2)) / math.factorial(i)
        result += delta * term

    return result

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


def method_stirling(x, table):
    x_values = np.array(table[0])
    y_values = np.array(table[1])
    n = len(x_values)

    if n < 2:
        return y_values[0] if n == 1 else 0.0

    h = x_values[1] - x_values[0]
    if not np.allclose(np.diff(x_values), h, atol=1e-6):
        return -1

    mid_idx = np.argmin(np.abs(x_values - x))
    mid_idx = min(max(mid_idx, 1), n - 2)

    a = x_values[mid_idx]
    t = (x - a) / h

    diff_table = create_central_difference_table(table)

    result = y_values[mid_idx]
    term = t
    factorial = 1

    max_steps = min(4, len(diff_table) - 1)
    for i in range(1, max_steps + 1):
        delta = 0.0
        order = (i + 1) // 2

        lower_bound = mid_idx - order
        upper_bound = mid_idx - order + 1

        if i % 2 == 1:  #
            if lower_bound >= 0 and lower_bound < len(diff_table[i]):
                delta = diff_table[i][lower_bound]
        else:
            if (lower_bound >= 0 and upper_bound < len(diff_table[i])):
                delta = (diff_table[i][lower_bound] + diff_table[i][upper_bound]) / 2

        if delta == 0.0:
            break

        result += delta * term / math.factorial(i)
        term *= (t ** 2 - order ** 2) / (2 * order + 1)

    return result


def method_gauss(x, table):
    x_values = np.array(table[0])
    y_values = np.array(table[1])
    n = len(x_values)

    if n < 2:
        return y_values[0] if n == 1 else 0.0

    h = x_values[1] - x_values[0]
    if not np.allclose(np.diff(x_values), h, atol=1e-6):
        raise ValueError("Узлы не равноотстоящие")

    mid_idx = np.argmin(np.abs(x_values - x))

    max_steps = min(mid_idx, n - mid_idx - 1, len(y_values) - 1)
    if max_steps < 1:
        return y_values[mid_idx]

    a = x_values[mid_idx]
    t = (x - a) / h
    use_first_formula = (t <= 0)

    diff_table = create_difference_table(table)

    result = y_values[mid_idx]
    term = 1.0

    for i in range(1, max_steps + 1):
        if use_first_formula:
            idx = mid_idx - (i // 2)
        else:
            idx = mid_idx - ((i - 1) // 2)

        if idx < 0 or idx + i >= len(diff_table[i]):
            break

        delta = diff_table[i][idx]

        term *= (t + (-1) ** use_first_formula * (i // 2))
        if i % 2 == 0:
            term *= (t - (-1) ** use_first_formula * (i // 2))

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