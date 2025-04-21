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