class InputParser:
    def __init__(self):
        self.gender_keywords = {
            'мужской', 'мужчина', 'мальчик', 'парень',
            'женский', 'женщина', 'девочка', 'девушка'
        }

        self.family_keywords = {
            'брат', 'братья', 'сестра', 'сестры', 'братьев', 'сестер',
            'родители', 'мама', 'папа', 'отец', 'мать',
        }

        self.people_names = [
            'savely', 'nikita', 'danil', 'arseniy', 'daniil', 'alexander',
            'anton', 'evgeniy', 'denis', 'ivan', 'sergey', 'vasiliy',
            'nikolay', 'victor', 'alexander_grandfather', 'vyacheslav',
            'marina', 'galina', 'nadezhda', 'anna_junior', 'ksenia',
            'svetlana', 'olga', 'elena', 'natalia', 'olga_mother',
            'anna', 'elena_aunt', 'vera', 'valentina'
        ]

    def parse_input(self, user_input):
        user_data = {
            'age': None,
            'gender': None,
            'has_brothers': None,
            'has_sisters': None,
            'parents': [],
            'siblings': [],
            'raw_text': user_input
        }

        user_input_lower = user_input.lower()
        words = user_input_lower.split()

        user_data['age'] = self._extract_age(words, user_input_lower)

        user_data['gender'] = self._extract_gender(user_input_lower)

        brothers_info = self._extract_brothers_info(user_input_lower)
        sisters_info = self._extract_sisters_info(user_input_lower)

        if brothers_info['has_brothers'] is not None:
            user_data['has_brothers'] = brothers_info['has_brothers']
            user_data['siblings'].extend(brothers_info['names'])

        if sisters_info['has_sisters'] is not None:
            user_data['has_sisters'] = sisters_info['has_sisters']
            user_data['siblings'].extend(sisters_info['names'])

        user_data['parents'] = self._extract_parents_info(user_input_lower)

        user_data['siblings'] = list(set(user_data['siblings']))
        user_data['parents'] = list(set(user_data['parents']))

        return user_data

    def _extract_age(self, words, text):
        patterns = [
            lambda: next((int(words[i]) for i in range(len(words))
                         if i > 0 and words[i-1] == 'мне' and words[i].isdigit() and
                         i+1 < len(words) and words[i+1] in ['лет', 'года', 'год']), None),

            lambda: next((int(words[i]) for i in range(len(words))
                         if words[i].isdigit() and i+1 < len(words) and
                         words[i+1] in ['лет', 'года', 'год']), None),

            lambda: next((int(words[i]) for i in range(len(words))
                         if i > 0 and words[i-1] == 'возраст' and words[i].isdigit()), None),

            lambda: next((int(word) for word in words if word.isdigit() and
                         (any(age_word in text for age_word in ['лет', 'возраст', 'год']) or
                          any(word in text for word in ['мне', 'возраст']))), None)
        ]

        for pattern in patterns:
            age = pattern()
            if age is not None and 0 < age < 120:
                return age

        return None

    def _extract_gender(self, text):
        male_indicators = ['мужской', 'мужчина', 'мальчик', 'парень']
        female_indicators = ['женский', 'женщина', 'девочка', 'девушка']

        if any(indicator in text for indicator in male_indicators):
            return 'male'
        elif any(indicator in text for indicator in female_indicators):
            return 'female'

        return None

    def _extract_brothers_info(self, text):
        result = {'has_brothers': None, 'names': []}

        has_brothers_patterns = [
            'есть брат', 'есть братья', 'имею брата', 'имею братьев',
            'у меня брат', 'мои братья', 'брат есть', 'братья есть'
        ]

        no_brothers_patterns = [
            'нет брата', 'нет братьев', 'не имею брата', 'без братьев',
            'нету брата', 'братьев нет'
        ]

        if any(pattern in text for pattern in has_brothers_patterns):
            result['has_brothers'] = True
        elif any(pattern in text for pattern in no_brothers_patterns):
            result['has_brothers'] = False

        brother_keywords = ['брат', 'братья']
        words = text.split()

        for i, word in enumerate(words):
            if word in brother_keywords and i + 1 < len(words):
                next_word = words[i + 1]
                if next_word in self.people_names:
                    result['names'].append(next_word)

        return result

    def _extract_sisters_info(self, text):
        result = {'has_sisters': None, 'names': []}

        has_sisters_patterns = [
            'есть сестра', 'есть сестры', 'имею сестру', 'имею сестер',
            'у меня сестра', 'мои сестры', 'сестра есть', 'сестры есть'
        ]

        no_sisters_patterns = [
            'нет сестры', 'нет сестер', 'не имею сестры', 'без сестер',
            'нету сестры', 'сестер нет'
        ]

        if any(pattern in text for pattern in has_sisters_patterns):
            result['has_sisters'] = True
        elif any(pattern in text for pattern in no_sisters_patterns):
            result['has_sisters'] = False

        sister_keywords = ['сестра', 'сестры']
        words = text.split()

        for i, word in enumerate(words):
            if word in sister_keywords and i + 1 < len(words):
                next_word = words[i + 1]
                if next_word in self.people_names:
                    result['names'].append(next_word)

        return result

    def _extract_parents_info(self, text):
        parents = []

        parent_keywords = ['мама', 'папа', 'отец', 'мать', 'родитель']
        words = text.split()

        for i, word in enumerate(words):
            if word in parent_keywords and i + 1 < len(words):
                next_word = words[i + 1]
                if next_word in self.people_names:
                    parents.append(next_word)

        return parents

    def get_missing_info_questions(self, user_data):
        questions = []

        if user_data['age'] is None:
            questions.append("Сколько вам лет?")

        if user_data['gender'] is None:
            questions.append("Уточните ваш пол (мужской/женский)?")

        if user_data['has_brothers'] is None and user_data['has_sisters'] is None:
            questions.append("У вас есть братья или сестры?")
        elif user_data['has_brothers'] is None:
            questions.append("У вас есть братья?")
        elif user_data['has_sisters'] is None:
            questions.append("У вас есть сестры?")

        return questions
