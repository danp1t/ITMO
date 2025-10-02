from knowledge_base import FamilyKnowledgeBase

class UserIdentifier:
    def __init__(self, knowledge_base):
        self.kb = knowledge_base
        self.identified_user = None
        self.uncertain_matches = []

    def identify_user(self, user_data):
        candidates = self.kb.get_all_people()

        if 'age' in user_data and user_data['age']:
            age_candidates = self.kb.find_people_by_age(user_data['age'], tolerance=3)
            candidates = [c for c in candidates if c in [person for person, age in age_candidates]]

        if 'gender' in user_data and user_data['gender']:
            gender_candidates = self.kb.find_people_by_gender(user_data['gender'])
            candidates = [c for c in candidates if c in gender_candidates]

        if 'has_brothers' in user_data and user_data['has_brothers'] is not None:
            sibling_candidates = self.kb.find_people_with_siblings(
                has_brothers=user_data['has_brothers'],
                has_sisters=user_data.get('has_sisters')
            )
            candidates = [c for c in candidates if c in sibling_candidates]

        scored_candidates = []
        for candidate in candidates:
            score = self.calculate_match_score(candidate, user_data)
            scored_candidates.append((candidate, score))

        scored_candidates.sort(key=lambda x: x[1], reverse=True)

        if not scored_candidates:
            return None, []

        if len(scored_candidates) == 1 or scored_candidates[0][1] > scored_candidates[1][1] + 2:
            self.identified_user = scored_candidates[0][0]
            return self.identified_user, []
        else:
            self.uncertain_matches = scored_candidates[:3]
            return None, self.uncertain_matches

    def calculate_match_score(self, candidate, user_data):
        score = 0

        if 'age' in user_data and user_data['age']:
            candidate_age = self.kb.get_current_age(candidate)
            if candidate_age is not None:
                age_diff = abs(candidate_age - user_data['age'])
                if age_diff == 0:
                    score += 10
                elif age_diff == 1:
                    score += 7
                elif age_diff == 2:
                    score += 3
                elif age_diff == 3:
                    score += 1

        if 'gender' in user_data and user_data['gender']:
            candidate_gender = self.kb.get_gender(candidate)
            if candidate_gender == user_data['gender']:
                score += 8

        if 'has_brothers' in user_data and user_data['has_brothers'] is not None:
            has_brothers = len(self.kb.find_brothers(candidate)) > 0
            if has_brothers == user_data['has_brothers']:
                score += 5
            else:
                score -= 3

        if 'has_sisters' in user_data and user_data['has_sisters'] is not None:
            has_sisters = len(self.kb.find_sisters(candidate)) > 0
            if has_sisters == user_data['has_sisters']:
                score += 5
            else:
                score -= 3

        if 'parents' in user_data and user_data['parents']:
            candidate_parents = self.kb.get_parents(candidate)
            for parent in user_data['parents']:
                if parent in candidate_parents:
                    score += 6

        if 'siblings' in user_data and user_data['siblings']:
            candidate_siblings = self.kb.get_siblings(candidate)
            for sibling in user_data['siblings']:
                if sibling in candidate_siblings:
                    score += 4

        return score

    def get_additional_questions(self, uncertain_matches):
        questions = []
        candidates = [match[0] for match in uncertain_matches]

        all_ages = [self.kb.get_current_age(c) for c in candidates]
        all_genders = [self.kb.get_gender(c) for c in candidates]

        if len(set(all_ages)) > 1:
            questions.append("Уточните ваш точный возраст?")

        if len(set(all_genders)) > 1:
            questions.append("Уточните ваш пол (мужской/женский)?")

        brothers_info = []
        sisters_info = []
        for candidate in candidates:
            brothers = self.kb.find_brothers(candidate)
            sisters = self.kb.find_sisters(candidate)
            brothers_info.append((candidate, len(brothers) > 0))
            sisters_info.append((candidate, len(sisters) > 0))

        if len(set([has_brothers for _, has_brothers in brothers_info])) > 1:
            questions.append("У вас есть братья?")

        if len(set([has_sisters for _, has_sisters in sisters_info])) > 1:
            questions.append("У вас есть сестры?")

        parents_sets = []
        for candidate in candidates:
            parents = self.kb.get_parents(candidate)
            parents_sets.append(set(parents))

        if len(set(tuple(sorted(ps)) for ps in parents_sets)) > 1:
            questions.append("Назовите имена ваших родителей?")

        return questions

    def clarify_with_additional_info(self, user_data, additional_info):
        updated_data = user_data.copy()

        if 'возраст' in additional_info.lower() or 'лет' in additional_info.lower():
            for word in additional_info.split():
                if word.isdigit():
                    updated_data['age'] = int(word)
                    break

        if 'мужской' in additional_info.lower() or 'мальчик' in additional_info.lower():
            updated_data['gender'] = 'male'
        elif 'женский' in additional_info.lower() or 'девочка' in additional_info.lower():
            updated_data['gender'] = 'female'

        if 'братья' in additional_info.lower() and 'нет' not in additional_info.lower():
            updated_data['has_brothers'] = True
        elif 'братья' in additional_info.lower() and 'нет' in additional_info.lower():
            updated_data['has_brothers'] = False

        if 'сестры' in additional_info.lower() and 'нет' not in additional_info.lower():
            updated_data['has_sisters'] = True
        elif 'сестры' in additional_info.lower() and 'нет' in additional_info.lower():
            updated_data['has_sisters'] = False

        return self.identify_user(updated_data)
