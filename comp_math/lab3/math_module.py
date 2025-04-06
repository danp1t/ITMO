def left_rectangle_method(f, a, b, epsilon):
    n = 1
    prev_result = 0
    max_iter = 10000

    for _ in range(max_iter):
        h = (b - a) / n
        current_result = sum(f(a + i * h) * h for i in range(n))

        if n > 1:
            error = abs(current_result - prev_result) / 3
            if error < epsilon:
                return current_result, error

        prev_result = current_result
        n *= 2

    raise ValueError("Не удалось достичь заданной точности")


def right_rectangle_method(f, a, b, epsilon):
    n = 1
    prev_result = 0
    max_iter = 10000

    for _ in range(max_iter):
        h = (b - a) / n
        current_result = sum(f(a + (i + 1) * h) * h for i in range(n))

        if n > 1:
            error = abs(current_result - prev_result) / 3
            if error < epsilon:
                return current_result, error

        prev_result = current_result
        n *= 2

    raise ValueError("Не удалось достичь заданной точности")


def middle_rectangle_method(f, a, b, epsilon):
    n = 1
    prev_result = 0
    max_iter = 10000

    for _ in range(max_iter):
        h = (b - a) / n
        current_result = sum(f(a + i * h + h / 2) * h for i in range(n))

        if n > 1:
            error = abs(current_result - prev_result) / 3
            if error < epsilon:
                return current_result, error

        prev_result = current_result
        n *= 2

    raise ValueError("Не удалось достичь заданной точности")


def trapezoid_method(f, a, b, epsilon):
    n = 1
    prev_result = 0
    max_iter = 10000

    for _ in range(max_iter):
        h = (b - a) / n
        current_result = (f(a) + f(b)) * h / 2
        current_result += sum(f(a + i * h) * h for i in range(1, n))

        if n > 1:
            error = abs(current_result - prev_result) / 3
            if error < epsilon:
                return current_result, error

        prev_result = current_result
        n *= 2

    raise ValueError("Не удалось достичь заданной точности")


def simpson_method(f, a, b, epsilon):
    n = 2
    prev_result = 0
    max_iter = 10000

    for _ in range(max_iter):
        h = (b - a) / n
        current_result = f(a) + f(b)

        current_result += 4 * sum(f(a + i * h) for i in range(1, n, 2))

        current_result += 2 * sum(f(a + i * h) for i in range(2, n - 1, 2))
        current_result *= h / 3

        if n > 2:
            error = abs(current_result - prev_result) / 15
            if error < epsilon:
                return current_result, error

        prev_result = current_result
        n *= 2

    raise ValueError("Не удалось достичь заданной точности")