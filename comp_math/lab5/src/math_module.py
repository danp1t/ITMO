import numpy as np
import math

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


def method_gauss(x, table, diff_table):
    x_values = np.array(table[0])
    y_values = np.array(table[1])
    n = len(x_values)
    h = x_values[1] - x_values[0]

    mid_idx = np.argmin(np.abs(x_values - x))
    if mid_idx == 0: mid_idx = 1
    if mid_idx == n - 1: mid_idx = n - 2
    a = x_values[mid_idx]

    t = (x - a) / h
    use_first_formula = (x < a)

    result = y_values[mid_idx]
    product = 1.0

    for i in range(1, len(diff_table)):
        if use_first_formula:
            idx = mid_idx - (i // 2)
        else:
            idx = mid_idx - (i // 2) + (i - 1) % 2

        if idx < 0 or idx >= len(diff_table[i]):
            break

        delta = diff_table[i][idx]

        term_product = 1.0
        for j in range(i):
            if use_first_formula:
                k = (-(i // 2) + j)
            else:
                k = (i // 2 - 1 + j)
            term_product *= (t - k)

        result += delta * term_product / math.factorial(i)

    return result


def method_newton():
    pass