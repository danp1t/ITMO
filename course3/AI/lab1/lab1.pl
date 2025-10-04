male_fact(savely).
male_fact(nikita).
male_fact(danil).
male_fact(arseniy).
male_fact(daniil).
male_fact(alexander).
male_fact(anton).
male_fact(evgeniy).
male_fact(denis).
male_fact(ivan).
male_fact(sergey).
male_fact(vasiliy).
male_fact(nikolay).
male_fact(victor).
male_fact(alexander_grandfather).
male_fact(vyacheslav).

female_fact(marina).
female_fact(galina).
female_fact(nadezhda).
female_fact(anna_junior).
female_fact(ksenia).
female_fact(svetlana).
female_fact(olga).
female_fact(elena).
female_fact(natalia).
female_fact(olga_mother).
female_fact(anna).
female_fact(elena_aunt).
female_fact(vera).
female_fact(valentina).

parent_fact(olga, savely).
parent_fact(anton, savely).
parent_fact(evgeniy, anna_junior).
parent_fact(elena, anna_junior).
parent_fact(natalia, nikita).
parent_fact(denis, danil).
parent_fact(olga_mother, danil).
parent_fact(denis, ksenia).
parent_fact(olga_mother, ksenia).
parent_fact(anna, svetlana).
parent_fact(ivan, svetlana).
parent_fact(anna, arseniy).
parent_fact(ivan, arseniy).
parent_fact(elena_aunt, daniil).
parent_fact(elena_aunt, alexander).
parent_fact(sergey, anton).
parent_fact(vera, anton).
parent_fact(sergey, evgeniy).
parent_fact(vera, evgeniy).
parent_fact(valentina, natalia).
parent_fact(vasiliy, natalia).
parent_fact(valentina, denis).
parent_fact(vasiliy, denis).
parent_fact(victor, olga_mother).
parent_fact(victor, anna).
parent_fact(marina, olga_mother).
parent_fact(marina, anna).
parent_fact(galina, elena_aunt).
parent_fact(alexander_grandfather, elena_aunt).
parent_fact(nadezhda, vera).
parent_fact(nadezhda, valentina).
parent_fact(vyacheslav, vera).
parent_fact(vyacheslav, valentina).

sibling_fact(danil, ksenia).
sibling_fact(svetlana, arseniy).
sibling_fact(daniil, alexander).
sibling_fact(anton, evgeniy).
sibling_fact(natalia, denis).
sibling_fact(olga_mother, anna).
sibling_fact(vera, valentina).
sibling_fact(nikolay, victor).
sibling_fact(marina, galina).

birthday_fact(savely, 2015).
birthday_fact(anna_junior, 2013).
birthday_fact(nikita, 2012).
birthday_fact(danil, 2005).
birthday_fact(ksenia, 2010).
birthday_fact(svetlana, 2010).
birthday_fact(arseniy, 2014).
birthday_fact(daniil, 2017).
birthday_fact(alexander, 2006).
birthday_fact(olga, 1985).
birthday_fact(anton, 1980).
birthday_fact(evgeniy, 1987).
birthday_fact(elena, 1990).
birthday_fact(natalia, 1983).
birthday_fact(denis, 1985).
birthday_fact(olga_mother, 1986).
birthday_fact(anna, 1988).
birthday_fact(ivan, 1980).
birthday_fact(elena_aunt, 1983).
birthday_fact(sergey, 1958).
birthday_fact(vera, 1958).
birthday_fact(valentina, 1956).
birthday_fact(vasiliy, 1961).
birthday_fact(nikolay, 1963).
birthday_fact(victor, 1963).
birthday_fact(marina, 1968).
birthday_fact(galina, 1965).
birthday_fact(alexander_grandfather, 1964).
birthday_fact(nadezhda, 1936).
birthday_fact(vyacheslav, 1933).

death_fact(vyacheslav, 2000).
death_fact(anton, 2019).

married_fact(olga, anton, 2011).
married_fact(evgeniy, elena, 2021).
married_fact(denis, olga_mother, 2005).
married_fact(anna, ivan, 2009).
married_fact(sergey, vera, 1978).
married_fact(valentina, vasiliy, 1981).
married_fact(victor, marina, 1985).
married_fact(galina, alexander_grandfather, 1984).
married_fact(nadezhda, vyacheslav, 1956).

%1. Who is sibling?
sibling(X, Y) :- sibling_fact(X, Y) ; sibling_fact(Y, X).

%2. Who, when married?
married(X, Y, Z) :- married_fact(X, Y, Z), !.
married(X, Y, Z) :- married_fact(Y, X, Z).

%3. Who, when my son birthday? X - parent, Y - child, Z - year
son(X, Y, Z) :- parent_fact(X, Y), male_fact(Y), birthday_fact(Y, Z).

%4. Who, when my daughter birthday? X - parent, Y - child, Z - year
daughter(X, Y, Z) :- parent_fact(X, Y), female_fact(Y), birthday_fact(Y, Z).

%5. When did my parents marry? X - I am.
parents_married(X, Y, Z, Year) :- (parent_fact(Y, X) ,! ; parent_fact(Z, X)), married_fact(Y, Z, Year).

%6. When did my father born? X - I am.
father_born(X, Y, Z) :- parent_fact(Y, X), male_fact(Y), birthday_fact(Y, Z).

%7. When did my mother born? X - I am
mother_born(X, Y, Z) :- parent_fact(Y, X), female_fact(Y), birthday_fact(Y, Z).

%8. When did my sister born? X - I am
sister_born(X, Y, Z) :- sibling(X, Y), female_fact(Y), birthday_fact(Y, Z).

%9. When did my brother born?
brother_born(X, Y, Z) :- sibling(X, Y), male_fact(Y), birthday_fact(Y, Z).

%10. When did my sister marry?
sister_marry(X, Y, Z, Year) :- sibling(X, Y), female_fact(Y), married(Y, Z, Year).

%11. When did my brother marry?
brother_marry(X, Y, Z, Year) :- sibling(X, Y), male_fact(Y), married(Y, Z, Year).

%12. When did my cousins born?
cousins_born(X, Y, Z, A, Year) :- parent_fact(Y, X), sibling(Y, Z), parent_fact(Z, A), birthday_fact(A, Year).

%13. When did my aunt born?
aunt_born(X, Y, Z, Year) :- parent_fact(Y, X), sibling(Y, Z), female_fact(Z), birthday_fact(Z, Year).

%14. When did my cousins marry?
cousins_marry(X, Y, Z, A, B, Year) :- parent_fact(Y, X), sibling(Y, Z), parent_fact(Z, A), married(A, B, Year).

%15. When did my grandparents marry?
grandparents_marry(X, Y, Z, A, Year) :- parent_fact(Y, X), parent_fact(Z, Y), married(Z, A, Year).

%16. When did my grandparents born?
grandparents_born(X, Y, Z, Year) :- parent_fact(Y, X), parent_fact(Z, Y), birthday_fact(Z, Year).

%17. When did my uncle born?
uncle_born(X, Y, Z, Year) :- parent_fact(Y, X), sibling(Y, Z), male_fact(Z), birthday_fact(Z, Year).

%18. When did my uncle marry?
uncle_marry(X, Y, Z, A, Year) :- parent_fact(Y, X), sibling(Y, Z), male_fact(Z), married(Z, A, Year).

%19. When did my uncle died?
uncle_died(X, Y, Z, Year) :- parent_fact(Y, X), sibling(Y, Z), male_fact(Z), death_fact(Z, Year).

%20. When did my grandparents died?
grandparents_died(X, Y, Z, Year) :- parent_fact(Y, X), parent_fact(Z, Y), death_fact(Z, Year).

%21. When did my great-grandparents marry?
great_grandparents_marry(X, Y, Z, A, B, Year) :- parent_fact(Y, X), parent_fact(Z, Y), parent_fact(A, Z), married(A, B, Year).

%22. When did my great-grandparents born?
great_grandparents_born(X, Y, Z, A, Year) :- parent_fact(Y, X), parent_fact(Z, Y), parent_fact(A, Z), birthday_fact(A, Year).

%23. When did my father died?
father_died(X, Y, Year) :- parent_fact(Y, X), male_fact(Y), death_fact(Y, Year).

%24. Who married in this year?
married_in_year(X, Y,Year) :- married_fact(X, Y, Year).

%25. Who born in this year?
born_in_year(X, Year) :- birthday_fact(X, Year).

%26. Who died in this year?
died_in_year(X, Year) :- death_fact(X, Year).

%27. When did my aunt marry?
aunt_marry(X, Y, Z, A, Year) :- parent_fact(Y, X), sibling(Y, Z), female_fact(Z), married(Z, A, Year).

%28. When did nephew born?
nephew_born(X, Y, Z, Year) :- sibling(X, Y), parent_fact(Y, Z), male_fact(Z), birthday_fact(Z, Year).

%29. When did niece born?
niece_born(X, Y, Z, Year) :- sibling(X, Y), parent_fact(Y, Z), female_fact(Z), birthday_fact(Z, Year).

%30. When did niece marry?
niece_marry(X, Y, Z, A, Year) :- sibling(X, Y), parent_fact(Y, Z), female_fact(Z), married(Z, A, Year).



