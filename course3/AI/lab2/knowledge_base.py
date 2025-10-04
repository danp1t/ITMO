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
        males = ['савелий', 'никита', 'данил', 'арсений', 'даниил', 'саша',
            'антон', 'евгений', 'денис', 'иван', 'сергей', 'василий',
            'николай', 'виктор', 'александр', 'вячеслав']
        self.male_facts.update(males)

        females = ['марина', 'галина', 'надежда', 'аня', 'ксения',
            'светлана', 'ольга', 'лена', 'наталья', 'оля',
            'анна', 'елена', 'вера', 'валентина']
        self.female_facts.update(females)

        self.parent_facts = [
            ('ольга', 'савелий'), ('антон', 'савелий'),
            ('евгений', 'аня'), ('лена', 'аня'),
            ('наталья', 'никита'),
            ('денис', 'данил'), ('оля', 'данил'),
            ('денис', 'ксения'), ('оля', 'ксения'),
            ('анна', 'светлана'), ('иван', 'светлана'),
            ('анна', 'арсений'), ('иван', 'арсений'),
            ('елена', 'даниил'),
            ('елена', 'саша'),
            ('сергей', 'антон'), ('вера', 'антон'),
            ('сергей', 'евгений'), ('вера', 'евгений'),
            ('валентина', 'наталья'), ('василий', 'наталья'),
            ('валентина', 'денис'), ('василий', 'денис'),
            ('виктор', 'оля'), ('марина', 'оля'),
            ('виктор', 'анна'), ('марина', 'анна'),
            ('галина', 'елена'), ('александр', 'елена'),
            ('надежда', 'вера'), ('вячеслав', 'вера'),
            ('надежда', 'валентина'), ('вячеслав', 'валентина')
        ]

        self.sibling_facts = [
            ('данил', 'ксения'), ('светлана', 'арсений'), ('даниил', 'саша'),
            ('антон', 'евгений'), ('наталья', 'денис'), ('оля', 'анна'),
            ('вера', 'валентина'), ('николай', 'виктор'), ('марина', 'галина')
        ]

        self.birthday_facts = {
            'савелий': 2015, 'аня': 2013, 'никита': 2012, 'данил': 2005,
            'ксения': 2010, 'светлана': 2010, 'арсений': 2014, 'даниил': 2017,
            'саша': 2006, 'ольга': 1985, 'антон': 1980, 'евгений': 1987,
            'лена': 1990, 'наталья': 1983, 'денис': 1985, 'оля': 1986,
            'анна': 1988, 'иван': 1980, 'елена': 1983, 'сергей': 1958,
            'вера': 1958, 'валентина': 1956, 'василий': 1961, 'николай': 1963,
            'виктор': 1963, 'марина': 1968, 'галина': 1965, 'александр': 1964,
            'надежда': 1936, 'вячеслав': 1933
        }

        self.death_facts = {
            'вячеслав': 2000, 'антон': 2019
        }

        self.married_facts = [
            ('ольга', 'антон', 2011), ('евгений', 'лена', 2021),
            ('денис', 'оля', 2005), ('анна', 'иван', 2009),
            ('сергей', 'вера', 1978), ('валентина', 'василий', 1981),
            ('виктор', 'марина', 1985), ('галина', 'александр', 1984),
            ('надежда', 'вячеслав', 1956)
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
            return f"Человек с именем {person.capitalize()} не найден в базе знаний"

        report = []
        report.append(f"Инфорация найденная о {person.capitalize()}:")
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
            report.append(f"Родители: {', '.join(parent.capitalize() for parent in parents)}")
        else:
            report.append("Родители: информация отсутствует")

        siblings = self.get_siblings(person)
        if siblings:
            report.append(f"Братья и сестры: {', '.join(sibling.capitalize() for sibling in siblings)}")
        else:
            report.append("Братья и сестры: нет")

        children = self.get_children(person)
        if children:
            report.append(f"Дети: {', '.join(child.capitalize() for child in children)}")
        else:
            report.append("Дети: нет")

        spouse, marriage_year = self.get_married_info(person)
        if spouse:
            report.append(f"В браке с: {spouse.capitalize()} (с {marriage_year} года)")
        else:
            report.append("Семейное положение: не в браке")

        return "\n".join(report)
