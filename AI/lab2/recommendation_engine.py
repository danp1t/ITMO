from knowledge_base import FamilyKnowledgeBase

class RecommendationEngine:
    def __init__(self, knowledge_base):
        self.kb = knowledge_base

    def generate_personalized_recommendations(self, person_name):
        if not self.kb.person_exists(person_name):
            return ["Человек не найден в базе знаний"]

        recommendations = []
        current_age = self.kb.get_current_age(person_name)

        siblings = self.kb.get_siblings(person_name)
        if siblings:
            recommendations.append(f"Исследуйте информацию о ваших братьях и сестрах: {', '.join(siblings)}")

        cousins = self.kb.find_cousins(person_name)
        if cousins:
            recommendations.append(f"Узнайте о двоюродных братьях и сестрах, например: {', '.join(cousins[:2])}")

        aunts_uncles = self.kb.find_aunts_uncles(person_name)
        if aunts_uncles:
            recommendations.append(f"Изучите информацию о тетях и дядях: {', '.join(aunts_uncles[:2])}")

        grandparents = self.kb.find_grandparents(person_name)
        if grandparents:
            recommendations.append(f"Откройте историю ваших бабушек и дедушек: {', '.join(grandparents)}")

        spouse, marriage_year = self.kb.get_married_info(person_name)
        if spouse:
            recommendations.append(f"Изучите подробности вашего брака с {spouse}")
        else:
            if current_age and current_age > 18:
                married_siblings = []
                for sibling in siblings:
                    s_spouse, s_year = self.kb.get_married_info(sibling)
                    if s_spouse:
                        married_siblings.append(sibling)

                if married_siblings:
                    recommendations.append(f"Ваши братья/сестры {', '.join(married_siblings)} уже в браке - узнайте об их семьях")

        return recommendations

    def get_family_insights(self, person_name):
        insights = []

        family_members = [person_name]
        family_members.extend(self.kb.get_parents(person_name))
        family_members.extend(self.kb.get_siblings(person_name))

        ages = []
        for member in family_members:
            age = self.kb.get_current_age(member)
            if age:
                ages.append(age)

        if ages:
            avg_age = sum(ages) / len(ages)
            insights.append(f"Средний возраст ваших ближайших родственников: {avg_age:.1f} лет")

        married_count = 0
        for member in family_members:
            spouse, year = self.kb.get_married_info(member)
            if spouse:
                married_count += 1

        if married_count > 0:
            marriage_rate = married_count / len(family_members)
            if marriage_rate > 0.7:
                insights.append("В вашей семье сильные традиции брака")
            elif marriage_rate < 0.3:
                insights.append("В вашей семье индивидуалистический подход к отношениям")

        return insights
