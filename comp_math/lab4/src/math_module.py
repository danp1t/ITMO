import numpy as np

def linial_approx(x, y):
    sum_x = 0
    sum_xx = 0
    sum_y = 0
    sum_xy = 0
    for i in range(len(x)):
        sum_x += x[i]
        sum_xx += (x[i]**2)
        sum_y += y[i]
        sum_xy += (x[i]*y[i])
    delta = len(x) * sum_xx - sum_x ** 2

    b = (sum_y * sum_xx - sum_x * sum_xy) / delta
    a = (len(x) * sum_xy - sum_x * sum_y) / delta

    return a, b

def bipolin_approx(x, y):
    sum_x = 0
    sum_xx = 0
    sum_xxx = 0
    sum_xxxx = 0
    sum_y = 0
    sum_xy = 0
    sum_xxy = 0
    for i in range(len(x)):
        sum_x += x[i]
        sum_xx += (x[i]**2)
        sum_xxx += (x[i]**3)
        sum_xxxx += (x[i]**4)
        sum_y += y[i]
        sum_xy += (x[i]*y[i])
        sum_xxy += (x[i]**2 * y[i])
    A = [
        [len(x), sum_x, sum_xx],
        [sum_x, sum_xx, sum_xxx],
        [sum_xx, sum_xxx, sum_xxxx]
    ]

    B = [sum_y, sum_xy, sum_xxy]
    c, b, a = np.linalg.solve(A, B)

    return a, b, c

def cubic_approx(x, y):
    sum_x = 0
    sum_xx = 0
    sum_xxx = 0
    sum_xxxx = 0
    sum_xxxxx = 0
    sum_xxxxxx = 0
    sum_y = 0
    sum_xy = 0
    sum_xxy = 0
    sum_xxxy = 0
    for i in range(len(x)):
        sum_x += x[i]
        sum_xx += (x[i]**2)
        sum_xxx += (x[i]**3)
        sum_xxxx += (x[i]**4)
        sum_xxxxx += (x[i]**5)
        sum_xxxxxx += (x[i]**6)
        sum_y += y[i]
        sum_xy += (x[i] * y[i])
        sum_xxy += (x[i]**2 * y[i])
        sum_xxxy += (x[i]**3 * y[i])

    A = [
        [len(x), sum_x, sum_xx, sum_xxx],
        [sum_x, sum_xx, sum_xxx, sum_xxxx],
        [sum_xx, sum_xxx, sum_xxxx, sum_xxxxx],
        [sum_xxx, sum_xxxx, sum_xxxxx, sum_xxxxxx]
    ]

    B = [sum_y, sum_xy, sum_xxy, sum_xxxy]

    d, c, b, a = np.linalg.solve(A, B)

    return a, b, c, d

def exp_approx(x, y):
    log_y = np.log(y)
    sum_x = 0
    sum_xx = 0
    sum_logy = 0
    sum_x_logy = 0
    for i in range(len(x)):
        sum_x += x[i]
        sum_xx += (x[i]**2)
        sum_logy += log_y[i]
        sum_x_logy += (x[i] * log_y[i])

    A = [
        [len(x), sum_x],
        [sum_x, sum_xx]
    ]
    B = [sum_logy, sum_x_logy]

    A_coeff, b = np.linalg.solve(A, B)
    a = np.exp(A_coeff)

    return b, a

def log_approx(x, y):
    log_x = np.log(x)

    sum_logx = 0
    sum_logx2 = 0
    sum_y = 0
    sum_logx_y = 0
    for i in range(len(x)):
        sum_logx += log_x[i]
        sum_logx2 += (log_x[i]**2)
        sum_y += y[i]
        sum_logx_y += (log_x[i]*y[i])

    A = [
        [len(x), sum_logx],
        [sum_logx, sum_logx2]
    ]
    B = [sum_y, sum_logx_y]

    a, b = np.linalg.solve(A, B)
    return b, a

def power_approx(x, y):
    log_x = np.log(x)
    log_y = np.log(y)

    sum_logx = 0
    sum_logx2 = 0
    sum_logy = 0
    sum_logx_logy = 0
    for i in range(len(x)):
        sum_logx += log_x[i]
        sum_logx2 += (log_x[i]**2)
        sum_logy += log_y[i]
        sum_logx_logy += (log_x[i] * log_y[i])

    # Матрица системы уравнений
    A = [
        [len(x), sum_logx],
        [sum_logx, sum_logx2]
    ]
    B = [sum_logy, sum_logx_logy]

    # Решение системы
    A_coeff, b = np.linalg.solve(A, B)
    a = np.exp(A_coeff)

    return b, a