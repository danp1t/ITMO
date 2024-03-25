#Разбиение промежутка [1, e] на n равномерных частей
#Создам массив из массивов вида [[start_1, end_1], [start_2, end_2], ..., [start_n, end_n],
# где start - значение начала части, а end - значения конца части
# Мы знаем, что end_1=start_2, end_2=start_3, ..., end_n-1=start_n, поэтому их будем вычислять только один раз

#Импортируем библиотеку для работы с математикой
import math

# Количество частей разбиения
n = int(input("Введите количество равномерных частей разбиения: "))
array = []

start = start_i = 1
end = math.e
for i in range(1, n + 1):
    part = []
    part.append(start_i)
    end_i = start_i + ((math.e - 1) / n)
    if i == n:
        part.append(end)
    else:
        part.append(end_i)
    start_i = end_i
    array.append(part)


