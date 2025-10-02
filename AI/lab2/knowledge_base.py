class FamilyKnowledgeBase:
    def __init__(self):
        self.male_facts = set()
        self.female_facts = set()
        self.parent_facts = []
        self.sibling_facts = []
        self.birthday_facts = {}
        self.death_facts = {}
        self.married_facts = []

        self.load_knowledge_base()

    def load_knowledge_base(self):
        males = ['savely', 'nikita', 'danil', 'arseniy', 'daniil', 'alexander',
                'anton', 'evgeniy', 'denis', 'ivan', 'sergey', 'vasiliy',
                'nikolay', 'victor', 'alexander_grandfather', 'vyacheslav']
        self.male_facts.update(males)

        females = ['marina', 'galina', 'nadezhda', 'anna_junior', 'ksenia',
                  'svetlana', 'olga', 'elena', 'natalia', 'olga_mother',
                  'anna', 'elena_aunt', 'vera', 'valentina']
        self.female_facts.update(females)

        self.parent_facts = [
            ('olga', 'savely'), ('anton', 'savely'),
            ('evgeniy', 'anna_junior'), ('elena', 'anna_junior'),
            ('natalia', 'nikita'),
            ('denis', 'danil'), ('olga_mother', 'danil'),
            ('denis', 'ksenia'), ('olga_mother', 'ksenia'),
            ('anna', 'svetlana'), ('ivan', 'svetlana'),
            ('anna', 'arseniy'), ('ivan', 'arseniy'),
            ('elena_aunt', 'daniil'),
            ('elena_aunt', 'alexander'),
            ('sergey', 'anton'), ('vera', 'anton'),
            ('sergey', 'evgeniy'), ('vera', 'evgeniy'),
            ('valentina', 'natalia'), ('vasiliy', 'natalia'),
            ('valentina', 'denis'), ('vasiliy', 'denis'),
            ('victor', 'olga_mother'), ('marina', 'olga_mother'),
            ('victor', 'anna'), ('marina', 'anna'),
            ('galina', 'elena_aunt'), ('alexander_grandfather', 'elena_aunt'),
            ('nadezhda', 'vera'), ('vyacheslav', 'vera'),
            ('nadezhda', 'valentina'), ('vyacheslav', 'valentina')
        ]

        self.sibling_facts = [
            ('danil', 'ksenia'), ('svetlana', 'arseniy'), ('daniil', 'alexander'),
            ('anton', 'evgeniy'), ('natalia', 'denis'), ('olga_mother', 'anna'),
            ('vera', 'valentina'), ('nikolay', 'victor'), ('marina', 'galina')
        ]

        self.birthday_facts = {
            'savely': 2015, 'anna_junior': 2013, 'nikita': 2012, 'danil': 2005,
            'ksenia': 2010, 'svetlana': 2010, 'arseniy': 2014, 'daniil': 2017,
            'alexander': 2006, 'olga': 1985, 'anton': 1980, 'evgeniy': 1987,
            'elena': 1990, 'natalia': 1983, 'denis': 1985, 'olga_mother': 1986,
            'anna': 1988, 'ivan': 1980, 'elena_aunt': 1983, 'sergey': 1958,
            'vera': 1958, 'valentina': 1956, 'vasiliy': 1961, 'nikolay': 1963,
            'victor': 1963, 'marina': 1968, 'galina': 1965, 'alexander_grandfather': 1964,
            'nadezhda': 1936, 'vyacheslav': 1933
        }

        self.death_facts = {
            'vyacheslav': 2000, 'anton': 2019
        }

        self.married_facts = [
            ('olga', 'anton', 2011), ('evgeniy', 'elena', 2021),
            ('denis', 'olga_mother', 2005), ('anna', 'ivan', 2009),
            ('sergey', 'vera', 1978), ('valentina', 'vasiliy', 1981),
            ('victor', 'marina', 1985), ('galina', 'alexander_grandfather', 1984),
            ('nadezhda', 'vyacheslav', 1956)
        ]

    def person_exists(self, name):
        return (name in self.male_facts or name in self.female_facts)

    def get_gender(self, name):
        if name in self.male_facts:
            return 'male'
        elif name in self.female_facts:
            return 'female'
        return None

    def get_current_age(self, name, current_year=2025):
        if name in self.birthday_facts:
            birth_year = self.birthday_facts[name]
            return current_year - birth_year
        return None

    def get_parents(self, child):
        parents = []
        for parent, child_name in self.parent_facts:
            if child_name == child:
                parents.append(parent)
        return parents

    def get_children(self, parent):
        children = []
        for parent_name, child in self.parent_facts:
            if parent_name == parent:
                children.append(child)
        return children

    def get_siblings(self, person):
        siblings = []
        for sib1, sib2 in self.sibling_facts:
            if sib1 == person:
                siblings.append(sib2)
            elif sib2 == person:
                siblings.append(sib1)
        return siblings

    def find_brothers(self, person):
        brothers = []
        siblings = self.get_siblings(person)
        for sibling in siblings:
            if self.get_gender(sibling) == 'male':
                brothers.append(sibling)
        return brothers

    def find_sisters(self, person):
        sisters = []
        siblings = self.get_siblings(person)
        for sibling in siblings:
            if self.get_gender(sibling) == 'female':
                sisters.append(sibling)
        return sisters

    def find_cousins(self, person):
        cousins = []
        parents = self.get_parents(person)
        for parent in parents:
            parent_siblings = self.get_siblings(parent)
            for parent_sibling in parent_siblings:
                cousins.extend(self.get_children(parent_sibling))
        return list(set(cousins))

    def find_aunts_uncles(self, person):
        aunts_uncles = []
        parents = self.get_parents(person)
        for parent in parents:
            aunts_uncles.extend(self.get_siblings(parent))
        return list(set(aunts_uncles))

    def find_grandparents(self, person):
        grandparents = []
        parents = self.get_parents(person)
        for parent in parents:
            grandparents.extend(self.get_parents(parent))
        return list(set(grandparents))

    def get_married_info(self, person):
        for spouse1, spouse2, year in self.married_facts:
            if spouse1 == person:
                return spouse2, year
            elif spouse2 == person:
                return spouse1, year
        return None, None

    def find_people_by_age(self, target_age, tolerance=3):
        matches = []
        current_year = 2025

        for person in self.get_all_people():
            age = self.get_current_age(person, current_year)
            if age is not None and abs(age - target_age) <= tolerance:
                matches.append((person, age))

        return matches

    def find_people_by_gender(self, gender):
        if gender == 'male':
            return list(self.male_facts)
        elif gender == 'female':
            return list(self.female_facts)
        return []

    def find_people_with_siblings(self, has_brothers=None, has_sisters=None):
        matches = []

        for person in self.get_all_people():
            siblings = self.get_siblings(person)
            brothers = self.find_brothers(person)
            sisters = self.find_sisters(person)

            match = True
            if has_brothers is not None:
                if has_brothers and not brothers:
                    match = False
                elif not has_brothers and brothers:
                    match = False

            if has_sisters is not None:
                if has_sisters and not sisters:
                    match = False
                elif not has_sisters and sisters:
                    match = False

            if match:
                matches.append(person)

        return matches

    def get_all_people(self):
        return list(self.male_facts) + list(self.female_facts)

    def generate_family_report(self, person):
        if not self.person_exists(person):
            return f"Человек с именем {person} не найден в базе знаний"

        report = []
        report.append(f"Инфорация найденная о {person}:")
        report.append("=" * 40)

        gender = "мужчина" if self.get_gender(person) == 'male' else "женщина"
        birth_year = self.birthday_facts.get(person, "неизвестен")
        current_age = self.get_current_age(person)
        report.append(f"Пол: {gender}")
        report.append(f"Год рождения: {birth_year}")
        if current_age:
            report.append(f"Текущий возраст: {current_age} лет")

        parents = self.get_parents(person)
        if parents:
            report.append(f"Родители: {', '.join(parents)}")
        else:
            report.append("Родители: информация отсутствует")

        siblings = self.get_siblings(person)
        if siblings:
            report.append(f"Братья и сестры: {', '.join(siblings)}")
        else:
            report.append("Братья и сестры: нет")

        children = self.get_children(person)
        if children:
            report.append(f"Дети: {', '.join(children)}")
        else:
            report.append("Дети: нет")

        spouse, marriage_year = self.get_married_info(person)
        if spouse:
            report.append(f"В браке с: {spouse} (с {marriage_year} года)")
        else:
            report.append("Семейное положение: не в браке")

        return "\n".join(report)
