import numpy as np

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


def newton_method(f, x0, epsilon, max_iter=100):
    h = 1e-6

    def df(x):
        return (f(x + h) - f(x - h)) / (2 * h)

    x = x0
    history = []

    for iter_count in range(1, max_iter + 1):
        fx = f(x)
        dfx = df(x)

        history.append((x, fx, dfx))

        if abs(fx) < epsilon:
            return x, fx, iter_count, history
        if abs(dfx) < 1e-12:
            raise RuntimeError(f"Производная близка к нулю (df={dfx:.2e})")
        x_new = x - fx / dfx
        if abs(x_new - x) < epsilon:
            return x_new, f(x_new), iter_count, history
        x = x_new

    raise RuntimeError(f"Не сошлось за {max_iter} итераций. Последнее значение: {x:.6f}")


def simple_iteration_method(f, x0, epsilon, max_iter=100):
    h = 1e-6
    df_x0 = (f(x0 + h) - f(x0 - h)) / (2 * h)
    M = df_x0 if abs(df_x0) > 1e-8 else 1.0
    phi = lambda x: x - f(x) / M

    history = []
    x = x0
    for iter_count in range(1, max_iter + 1):
        x_new = phi(x)
        history.append((x, x_new, f(x)))

        if abs(x_new - x) < epsilon:
            return x_new, f(x_new), iter_count, history

        x = x_new

    raise RuntimeError(f"Не сошлось за {max_iter} итераций. Последнее значение: {x:.6f}")


def compute_jacobian(f1, f2, x, y, h=1e-6):
    df1_dx = (f1(x + h, y) - f1(x - h, y)) / (2 * h)
    df1_dy = (f1(x, y + h) - f1(x, y - h)) / (2 * h)

    df2_dx = (f2(x + h, y) - f2(x - h, y)) / (2 * h)
    df2_dy = (f2(x, y + h) - f2(x, y - h)) / (2 * h)

    return np.array([
        [df1_dx, df1_dy],
        [df2_dx, df2_dy]
    ])


def newton_system_solver(f1, f2, x0, y0, epsilon=1e-6, max_iter=100):
    x, y = x0, y0
    history = []

    for iterations in range(1, max_iter + 1):
        f_val = np.array([f1(x, y), f2(x, y)])
        J = compute_jacobian(f1, f2, x, y)

        if np.abs(np.linalg.det(J)) < 1e-12:
            raise RuntimeError("Якобиан вырожден. Решение невозможно.")

        delta = np.linalg.solve(J, -f_val)

        x_new = x + delta[0]
        y_new = y + delta[1]

        history.append({
            'iteration': iterations,
            'x': x,
            'y': y,
            'f1': f_val[0],
            'f2': f_val[1],
            'delta_x': delta[0],
            'delta_y': delta[1]
        })

        if np.linalg.norm(delta) < epsilon and np.linalg.norm(f_val) < epsilon:
            return (x_new, y_new), iterations, history

        x, y = x_new, y_new

    raise RuntimeError(f"Метод не сошелся за {max_iter} итераций.")