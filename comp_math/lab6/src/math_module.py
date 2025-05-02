#Необходимо реализовать методы 1, 3, 5
#1. Метод Эйлера
#3. Метод Рунге-Кутта 4-го порядка
#5. Мношаговый метод Милна

import math

# Словарь функций уравнений (глобальная переменная)
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
