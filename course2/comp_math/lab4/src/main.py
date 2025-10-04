import other_command
import command
import readline


class SimpleCompleter:
    def __init__(self, options):
        self.options = sorted(options)
        self.matches = []

    def complete(self, text, state):
        if state == 0:
            if text:
                self.matches = [s for s in self.options if s.startswith(text)]
            else:
                self.matches = self.options[:]
        return self.matches[state] if state < len(self.matches) else None


def input_loop():
    # Инициализация автодополнения
    completer = SimpleCompleter([
        '/help', '/exit', '/info', '/start',
        '/clear', '/input_table',
        '/input_table_file'
    ])

    readline.set_completer(completer.complete)
    readline.parse_and_bind('tab: complete')
    readline.set_completer_delims(' \t\n')

    while True:
        try:
            line = input('!("/exit" to quit) Введите команду: => ')
            print(f'Отправка: {line}')

            if line.strip() == '/exit':
                break

            other_command.validate_task(line)

        except EOFError:
            break
        except KeyboardInterrupt:
            print("\nВыход...")
            break


# Инициализация
other_command.info_about_lab()
other_command.welcome_message()
command.help()

# Запуск основного цикла
input_loop()