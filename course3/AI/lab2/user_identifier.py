class UserIdentifier:
    def __init__(self, knowledge_base):
        self.kb = knowledge_base
        self.identified_user = None
        self.uncertain_matches = []
        self.conversation_history = []

    def identify_user(self, user_data):
        self.conversation_history.append(user_data.copy())

        combined_data = self._combine_conversation_data()

        candidates = self.kb.get_all_people()

        if 'age' in combined_data and combined_data['age']:
            age_candidates = self.kb.find_people_by_age(combined_data['age'], tolerance=3)
            candidates = [c for c in candidates if c in [person for person, age in age_candidates]]

        if 'gender' in combined_data and combined_data['gender']:
            gender_candidates = self.kb.find_people_by_gender(combined_data['gender'])
            candidates = [c for c in candidates if c in gender_candidates]

        if 'has_brothers' in combined_data and combined_data['has_brothers'] is not None:
            sibling_candidates = self.kb.find_people_with_siblings(
                has_brothers=combined_data['has_brothers']
            )
            candidates = [c for c in candidates if c in sibling_candidates]

        if 'has_sisters' in combined_data and combined_data['has_sisters'] is not None:
            sibling_candidates = self.kb.find_people_with_siblings(
                has_sisters=combined_data['has_sisters']
            )
            candidates = [c for c in candidates if c in sibling_candidates]

        if 'parents' in combined_data and combined_data['parents']:
            parent_filtered = []
            for candidate in candidates:
                candidate_parents = self.kb.get_parents(candidate)
                if any(parent in candidate_parents for parent in combined_data['parents']):
                    parent_filtered.append(candidate)
            candidates = parent_filtered

        scored_candidates = []
        for candidate in candidates:
            score = self.calculate_match_score(candidate, combined_data)
            scored_candidates.append((candidate, score))

        scored_candidates.sort(key=lambda x: x[1], reverse=True)
        if not scored_candidates:
            return None, []

        if len(scored_candidates) == 1 or scored_candidates[0][1] > scored_candidates[1][1] + 5:
            self.identified_user = scored_candidates[0][0]
            return self.identified_user, []
        else:
            self.uncertain_matches = scored_candidates[:6]
            return None, self.uncertain_matches

    def _combine_conversation_data(self):
        combined = {}
        for data in self.conversation_history:
            for key in ['age', 'gender', 'has_brothers', 'has_sisters']:
                if key in data and data[key] is not None:
                    combined[key] = data[key]

            for key in ['parents', 'siblings']:
                if key in data and data[key]:
                    if key not in combined:
                        combined[key] = []
                    combined[key].extend(data[key])
                    combined[key] = list(set(combined[key]))

        return combined

    def calculate_match_score(self, candidate, user_data):
        score = 0

        if 'age' in user_data and user_data['age']:
            candidate_age = self.kb.get_current_age(candidate)
            if candidate_age is not None:
                age_diff = abs(candidate_age - user_data['age'])
                if age_diff == 0:
                    score += 20
                elif age_diff == 1:
                    score += 15
                elif age_diff == 2:
                    score += 10
                else:
                    score -= 2

        if 'gender' in user_data and user_data['gender']:
            candidate_gender = self.kb.get_gender(candidate)
            if candidate_gender == user_data['gender']:
                score += 20
            else:
                score -= 20

        if 'has_brothers' in user_data and user_data['has_brothers'] is not None:
            has_brothers = len(self.kb.find_brothers(candidate)) > 0
            if has_brothers == user_data['has_brothers']:
                score += 10
            else:
                score -= 5

        if 'has_sisters' in user_data and user_data['has_sisters'] is not None:
            has_sisters = len(self.kb.find_sisters(candidate)) > 0
            if has_sisters == user_data['has_sisters']:
                score += 10
            else:
                score -= 5

        if 'parents' in user_data and user_data['parents']:
            candidate_parents = self.kb.get_parents(candidate)
            match_count = 0
            for parent in user_data['parents']:
                if parent in candidate_parents:
                    match_count += 1
                    score += 10

            if match_count == len(user_data['parents']):
                score += 15

        if 'siblings' in user_data and user_data['siblings']:
            candidate_siblings = self.kb.get_siblings(candidate)
            for sibling in user_data['siblings']:
                if sibling in candidate_siblings:
                    score += 10

        return score

    def get_additional_questions(self, uncertain_matches):
        questions = []
        candidates = [match[0] for match in uncertain_matches]

        all_ages = [self.kb.get_current_age(c) for c in candidates]
        all_genders = [self.kb.get_gender(c) for c in candidates]

        combined_data = self._combine_conversation_data()

        if len(set(all_ages)) > 1 and ('age' not in combined_data or combined_data['age'] is None):
            questions.append("Уточните ваш точный возраст?")

        if len(set(all_genders)) > 1 and ('gender' not in combined_data or combined_data['gender'] is None):
            questions.append("Уточните ваш пол (мужской/женский)?")

        brothers_info = []
        sisters_info = []
        for candidate in candidates:
            brothers = self.kb.find_brothers(candidate)
            sisters = self.kb.find_sisters(candidate)
            brothers_info.append((candidate, len(brothers) > 0))
            sisters_info.append((candidate, len(sisters) > 0))

        if (len(set([has_brothers for _, has_brothers in brothers_info])) > 1 and
            'has_brothers' not in combined_data):
            questions.append("У вас есть братья?")

        if (len(set([has_sisters for _, has_sisters in sisters_info])) > 1 and
            'has_sisters' not in combined_data):
            questions.append("У вас есть сестры?")

        parents_sets = []
        for candidate in candidates:
            parents = self.kb.get_parents(candidate)
            parents_sets.append(set(parents))

        if len(set(tuple(sorted(ps)) for ps in parents_sets)) > 1:
            if 'parents' not in combined_data or not combined_data['parents']:
                questions.append("Назовите имена ваших родителей?")

        return questions

    def reset_conversation(self):
        self.conversation_history = []
        self.identified_user = None
        self.uncertain_matches = []
