from user_interface import UserInterface

def main():
    try:
        ui = UserInterface()
        ui.run()
    except KeyboardInterrupt:
        print("\n\nПрограмма завершена пользователем")
    # except Exception as e:
    #     print(f"\nПроизошла ошибка: {e}")

if __name__ == "__main__":
    main()
