from knowledge_base import FamilyKnowledgeBase
from user_identifier import UserIdentifier
from input_parser import InputParser
from recommendation_engine import RecommendationEngine

class UserInterface:
    def __init__(self):
        self.kb = FamilyKnowledgeBase()
        self.identifier = UserIdentifier(self.kb)
        self.parser = InputParser()
        self.engine = RecommendationEngine(self.kb)
        self.current_user = None

    def display_welcome(self):
        print("=" * 60)
        print("БАЗА ЗНАНИЙ БОЛЬШОГО БРАТА")
        print("=" * 60)
        print("\nРасскажите о себе, и я попробую угадать, кто вы из базы знаний!")
        print("Пример: 'Мне 13 лет, у меня есть брат и сестра'")
        print("Введите 'выход' для завершения\n")

    def start_identification_mode(self):
        print("\n" + "=" * 50)
        print("Режим поиска человека -_- | -_- | -_- ")
        print("Большой брат всё-таки следил за тобой...")
        print("=" * 50)

        self.identifier.reset_conversation()
        identification_attempts = 0

        while identification_attempts < 5:
            if identification_attempts == 0:
                user_input = input("\nРасскажите о себе: ").strip()
            else:
                user_input = input("\nЧто еще вы можете рассказать? ").strip()

            new_data = self.parser.parse_input(user_input)

            identified_user, uncertain_matches = self.identifier.identify_user(new_data)

            if identified_user:
                self.current_user = identified_user
                print(f"\nБольшой брат нашел тебя! Ты - {identified_user.capitalize()}")
                self.show_user_profile(identified_user)
                self.provide_recommendations(identified_user)
                break
            elif uncertain_matches:
                print(f"\nХм... Я, конечно, не уверенm но возможно это ты:")
                for i, (match, score) in enumerate(uncertain_matches[:6], 1):
                    age = self.kb.get_current_age(match)
                    gender = "мужчина" if self.kb.get_gender(match) == 'male' else "женщина"
                    percentage = min(100, max(0, int((score / 80) * 100)))
                    print(f"{i}. {match} ({gender}, {age} лет) - совпадение: {percentage}%")

                questions = self.identifier.get_additional_questions(uncertain_matches)
                if questions:
                    print("\nПожалуйста, уточните:")
                    for question in questions[:2]:
                        print(f"- {question}")
                else:
                    print("\nПопробуйте предоставить больше информации о себе.")
            else:
                combined_data = self.identifier._combine_conversation_data()
                missing_questions = self.parser.get_missing_info_questions(combined_data)
                if missing_questions:
                    print("\nЧтобы я мог вас идентифицировать, ответьте на вопросы:")
                    for question in missing_questions[:3]:
                        print(f"- {question}")
                else:
                    print("\nНе удалось найти вас в базе знаний. Большой брат, плохо следил за тобой. -_-")

            identification_attempts += 1

        if not self.current_user:
            print("\nК сожалению, не удалось идентифицировать вас в системе.")

    def show_user_profile(self, person_name):
        report = self.kb.generate_family_report(person_name)
        print(f"\n{report}")

    def provide_recommendations(self, person_name):
        print("\n" + "=" * 40)
        print("    -_- ВОТ ЧТО БОЛЬШОЙ БРАТ НАШЕЛ О ТВОЕЙ СЕМЬЕ... -_-")
        print("=" * 40)

        recommendations = self.engine.generate_personalized_recommendations(person_name)
        for i, recommendation in enumerate(recommendations, 1):
            print(f"{i}. {recommendation}")

        insights = self.engine.get_family_insights(person_name)
        if insights:
            print("\nИнтересные факты о вашей семье:")
            for insight in insights:
                print(f"- {insight}")

        self.offer_further_actions(person_name)

    def offer_further_actions(self, person_name):
        while True:
            print("\nЧто вы хотите сделать дальше?")
            print("1. Исследовать информацию о конкретном родственнике")
            print("2. Узнать о браках в семье")
            print("3. Изучить семейную историю")
            print("4. Начать новую идентификацию")
            print("5. Вернуться в главное меню")

            choice = input("Ваш выбор (1-5): ").strip()

            if choice == '1':
                self.explore_relatives(person_name)
            elif choice == '2':
                self.explore_marriages(person_name)
            elif choice == '3':
                self.explore_family_history(person_name)
            elif choice == '4':
                self.start_identification_mode()
                break
            elif choice == '5':
                break
            else:
                print("Неизвестный выбор")

    def explore_relatives(self, person_name):
        print("\nВыберите тип родственников для исследования:")
        print("1. Родители")
        print("2. Братья и сестры")
        print("3. Дети")
        print("4. Двоюродные")
        print("5. Тети и дяди")
        print("6. Бабушки и дедушки")

        choice = input("Ваш выбор (1-6): ").strip()

        relation_methods = {
            '1': ('родители', self.kb.get_parents),
            '2': ('братья и сестры', self.kb.get_siblings),
            '3': ('дети', self.kb.get_children),
            '4': ('двоюродные', self.kb.find_cousins),
            '5': ('тети и дяди', self.kb.find_aunts_uncles),
            '6': ('бабушки и дедушки', self.kb.find_grandparents)
        }

        if choice in relation_methods:
            name, method = relation_methods[choice]
            relatives = method(person_name)

            if relatives:
                print(f"\nВаши {name}:")
                for relative in relatives:
                    age = self.kb.get_current_age(relative)
                    gender = "мужчина" if self.kb.get_gender(relative) == 'male' else "женщина"
                    print(f"- {relative} ({gender}, {age} лет)")

                selected = input(f"\nВведите имя для подробной информации (или 'назад'): ").strip()
                if selected in relatives:
                    report = self.kb.generate_family_report(selected)
                    print(f"\n{report}")
            else:
                print(f"{name.capitalize()} не найдены")
        else:
            print("Неизвестный выбор")

    def explore_marriages(self, person_name):
        print("\nБраки в вашей семье:")

        spouse, year = self.kb.get_married_info(person_name)
        if spouse:
            print(f"- Вы в браке с {spouse.capitalize()} с {year} года")
        else:
            print("- Вы не в браке")

        siblings = self.kb.get_siblings(person_name)
        married_siblings = []
        for sibling in siblings:
            s_spouse, s_year = self.kb.get_married_info(sibling)
            if s_spouse:
                married_siblings.append((sibling, s_spouse, s_year))

        if married_siblings:
            print("\nБраки ваших братьев и сестер:")
            for sibling, spouse, year in married_siblings:
                print(f"- {sibling.capitalize()} в браке с {spouse.capitalize()} с {year} года")

    def explore_family_history(self, person_name):
        print("\nСемейная история:")

        grandparents = self.kb.find_grandparents(person_name)
        if grandparents:
            print("Ваши бабушки и дедушки:")
            for grandparent in grandparents:
                birth_year = self.kb.birthday_facts.get(grandparent, "неизвестен")
                death_year = self.kb.death_facts.get(grandparent, "")
                death_info = f", умер(ла) в {death_year}" if death_year else ""
                print(f"- {grandparent.capitalize()} (род. {birth_year}{death_info})")

        all_relatives = [person_name]
        all_relatives.extend(self.kb.get_parents(person_name))
        all_relatives.extend(self.kb.get_siblings(person_name))
        all_relatives.extend(self.kb.find_grandparents(person_name))

        oldest = None
        oldest_age = 0
        current_year = 2025

        for relative in set(all_relatives):
            birth_year = self.kb.birthday_facts.get(relative)
            if birth_year:
                age = current_year - birth_year
                if age > oldest_age:
                    oldest_age = age
                    oldest = relative

        if oldest and oldest != person_name:
            print(f"\nСамый старший родственник в вашей ближайшей семье:")
            print(f"- {oldest.capitalize()} ({oldest_age} лет)")


    def run(self):
        self.display_welcome()

        while True:
            self.start_identification_mode()
