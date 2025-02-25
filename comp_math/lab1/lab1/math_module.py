def seidel(matrix, epsilon, max_iterations):
    if not is_diagonally_dominant(matrix):
        matrix = rearrange_matrix(matrix)
        if not is_diagonally_dominant(matrix):
            print("Невозможно достичь диагонального преобладания")

    a = [row[:-1] for row in matrix]
    b = [row[-1] for row in matrix]
    n = len(matrix)
    x = [0.0 for _ in range(n)]
    k = 1

    while k <= max_iterations:
        current_errors = []
        delta = 0
        for i in range(n):
            s = 0
            for j in range(n):
                if j != i:
                    s += a[i][j] * x[j]
            if a[i][i] == 0: a[i][i] = .000000001
            new_x = (b[i] - s) / a[i][i]
            d = abs(new_x - x[i])
            current_errors.append(d)

            if d > delta:
                delta = d
            x[i] = new_x

        current_errors.sort()

        if delta < epsilon:
            print(f"Решение найдено за {k} итераций.")
            return x
        k += 1

    return "Итерации расходятся"

def is_diagonally_dominant(matrix):
    n = len(matrix)
    for i in range(n):
        diag = abs(matrix[i][i])
        row_sum = sum(abs(matrix[i][j]) for j in range(n) if j != i)
        if diag <= row_sum:
            return False
    return True

def rearrange_matrix(matrix):
    n = len(matrix)

    new_matrix = [row.copy() for row in matrix]

    for col in range(n):
        max_row = col
        for row in range(col, n):
            if abs(new_matrix[row][col]) > abs(new_matrix[max_row][col]):
                max_row = row

        new_matrix[col], new_matrix[max_row] = new_matrix[max_row], new_matrix[col]

    return new_matrix
