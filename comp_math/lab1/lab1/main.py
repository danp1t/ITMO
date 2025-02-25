import other_command
import command

other_command.info_about_lab()
other_command.welcome_message()
command.help()

while True:
    task = input("Введите команду: ")
    other_command.validate_task(task)
