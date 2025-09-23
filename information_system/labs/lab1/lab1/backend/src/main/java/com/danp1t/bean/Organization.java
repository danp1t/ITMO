package com.danp1t.bean;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Organization {
    private Integer id; //Не null, значение поля > 0. Уникальное. Генерируется автоматически
    private String name; //Не null. Не пустая строка
    private Coordinates coordinates; //Не null
    private java.time.LocalDate creationDate; //Не null. Генерируется автоматически
    private Address officialAddress; //Поле не может быть null
    private float annualTurnover; //Значение поля должно быть больше 0
    private long employeesCount; //Значение поля должно быть больше 0
    private int rating; //Значение поля должно быть больше 0
    private OrganizationType type; //Поле может быть null
    private Address postalAddress; //Поле не может быть null
}
