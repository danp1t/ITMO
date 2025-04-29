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


def method_newton():
    pass