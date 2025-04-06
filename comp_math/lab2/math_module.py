def bisection_method(func, a, b, epsilon):
    if func(a) * func(b) >= 0:
        raise ValueError("Функция должна иметь разные знаки на концах интервала")

    iterations = 0
    while (b - a) >= epsilon and (func(b) - func(a) >= epsilon):
        c = (a + b) / 2
        fc = func(c)
        iterations += 1

        if fc == 0:
            return c, fc, iterations
        elif func(a) * fc < 0:
            b = c
        else:
            a = c

    root = (a + b) / 2
    return root, func(root), iterations