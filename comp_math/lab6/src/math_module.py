#Необходимо реализовать методы 1, 3, 5
#1. Метод Эйлера
#3. Метод Рунге-Кутта 4-го порядка
#5. Мношаговый метод Милна

import math

equations_funcs = [
    lambda x, y: 2 * x - y,
    lambda x, y: y / x if x != 0 else float('inf'),
    lambda x, y: math.exp(-x) + y ** 2,
    lambda x, y: y * (1 - y),
    lambda x, y: math.sin(x) * y
]

def euler_method(start_points, interval, h, epsilon, equation):
    x0, y0 = start_points
    xn = interval[1]
    f = equations_funcs[equation]

    def calculate(f, x0, y0, xn, step):
        x = [x0]
        y = [y0]
        while x[-1] < xn:
            current_x = x[-1]
            current_y = y[-1]
            delta = min(step, xn - current_x)
            y_next = current_y + delta * f(current_x, current_y)
            x.append(current_x + delta)
            y.append(y_next)
        return x, y

    x_h, y_h = calculate(f, x0, y0, xn, h)
    x_h2, y_h2 = calculate(f, x0, y0, xn, h / 2)
    max_error = 0.0
    for i in range(len(y_h)):
        idx = 2 * i
        if idx < len(y_h2):
            max_error = max(max_error, abs(y_h[i] - y_h2[idx]))

    # Вывод результатов
    print("\n" + "=" * 40)
    print(f"{'Метод Эйлера':^40}")
    print("=" * 40)
    print(f"{'x':<10}{'y (h)':<15}{'y (h/2)':<15}")
    for i in range(len(y_h)):
        h2_val = y_h2[2 * i] if 2 * i < len(y_h2) else "N/A"
        print(f"{x_h[i]:<10.3f}{y_h[i]:<15.6f}{str(h2_val):<15}")

    print(f"\nМаксимальная погрешность: {max_error:.6f}")
    print(f"Целевая точность: {epsilon:.6f}")

    if max_error > epsilon:
        print("\nТребуемая точность не достигнута!")
        print(f"Рекомендуемый шаг: {h / 2:.4f}")
    else:
        print("\nТочность в пределах допустимого!")

    return {
        "x_values": x_h,
        "y_values": y_h,
        "x_half_step": x_h2,
        "y_half_step": y_h2,
        "max_error": max_error,
        "is_precision_achieved": max_error <= epsilon
    }


def runge_kutta_method(start_points, interval, h, epsilon, equation):
    x0, y0 = start_points
    xn = interval[1]
    f = equations_funcs[equation]

    def calculate(f, x0, y0, xn, step):
        x = [x0]
        y = [y0]
        while x[-1] < xn:
            current_x = x[-1]
            current_y = y[-1]

            k1 = step * f(current_x, current_y)
            k2 = step * f(current_x + step / 2, current_y + k1 / 2)
            k3 = step * f(current_x + step / 2, current_y + k2 / 2)
            k4 = step * f(current_x + step, current_y + k3)

            y_next = current_y + (k1 + 2 * k2 + 2 * k3 + k4) / 6

            delta = min(step, xn - current_x)
            x.append(current_x + delta)
            y.append(y_next)
        return x, y

    x_h, y_h = calculate(f, x0, y0, xn, h)
    x_h2, y_h2 = calculate(f, x0, y0, xn, h / 2)

    max_error = 0.0
    for i in range(len(y_h)):
        idx = 2 * i
        if idx < len(y_h2):
            max_error = max(max_error, abs(y_h[i] - y_h2[idx]))

    print("\n" + "=" * 45)
    print(f"{'Метод Рунге-Кутта 4-го порядка':^45}")
    print("=" * 45)
    print(f"{'x':<10}{'y (h)':<18}{'y (h/2)':<18}")
    for i in range(len(y_h)):
        h2_val = y_h2[2 * i] if 2 * i < len(y_h2) else "N/A"
        print(f"{x_h[i]:<10.3f}{y_h[i]:<18.6f}{str(h2_val):<18}")

    return {
        "x_values": x_h,
        "y_values": y_h,
        "x_half_step": x_h2,
        "y_half_step": y_h2,
        "max_error": max_error,
        "is_precision_achieved": max_error <= epsilon
    }


exact_solutions = [
    lambda x, x0, y0: 2 * (x - 1) + (y0 - 2 * (x0 - 1)) * math.exp(-(x - x0)),
    lambda x, x0, y0: y0 * (x / x0),
    None,
    lambda x, x0, y0: y0 / (y0 + (1 - y0) * math.exp(-(x - x0))),
    lambda x, x0, y0: y0 * math.exp(math.cos(x0) - math.cos(x))
]


def milne_method(start_points, interval, h, epsilon, equation):
    x0, y0 = start_points
    xn = interval[1]
    f = equations_funcs[equation]
    exact_sol = exact_solutions[equation]

    if exact_sol is None:
        print("Ошибка: Для выбранного уравнения нет точного решения!")
        return None

    rk_result = runge_kutta_method(start_points, interval, h, epsilon, equation)
    x_rk = rk_result["x_values"]
    y_rk = rk_result["y_values"]

    if len(x_rk) < 4:
        print("Слишком мало точек! Уменьшите шаг h.")
        return None

    x = x_rk[:4]
    y = y_rk[:4]
    f_vals = [f(x[i], y[i]) for i in range(4)]

    n = 4
    while x[-1] < xn:
        x_p = x0 + n * h
        y_p = y[n - 4] + (4 * h / 3) * (2 * f_vals[n - 3] - f_vals[n - 2] + 2 * f_vals[n - 1])
        f_p = f(x_p, y_p)

        y_c = y[n - 2] + (h / 3) * (f_vals[n - 2] + 4 * f_vals[n - 1] + f_p)
        f_c = f(x_p, y_c)

        x.append(x_p)
        y.append(y_c)
        f_vals.append(f_c)
        n += 1

    exact_y = [exact_sol(xi, x0, y0) for xi in x]
    errors = [abs(y[i] - exact_y[i]) for i in range(len(y))]
    max_error = max(errors)

    print("\n" + "=" * 60)
    print(f"{'Метод Милна':^60}")
    print("=" * 60)
    print(f"{'x':<10}{'y (числ)':<15}{'y (точн)':<15}{'Погрешность':<15}")
    for i in range(len(x)):
        print(f"{x[i]:<10.3f}{y[i]:<15.6f}{exact_y[i]:<15.6f}{errors[i]:<15.6f}")

    print(f"\nМаксимальная погрешность: {max_error:.6f}")
    print(f"Целевая точность: {epsilon:.6f}")