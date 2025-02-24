def seidel(matrix, epsilon, max_iterations):
    a = [row[:-1] for row in matrix]
    b = [row[-1] for row in matrix]
    n = len(matrix)
    x = [0.0 for _ in range(n)]
    k = 1

    while k <= max_iterations:
        delta = 0
        for i in range(n):
            s = 0

            for j in range(n):
                if j != i:
                    s += a[i][j] * x[j]

            new_x = (b[i] - s) / a[i][i]

            d = abs(new_x - x[i])
            if d > delta:
                delta = d

            x[i] = new_x

        if delta < epsilon:
            print(f"Решение найдено за {k} итераций.")
            return x
        k += 1

    return "Итерации расходятся"


