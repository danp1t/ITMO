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