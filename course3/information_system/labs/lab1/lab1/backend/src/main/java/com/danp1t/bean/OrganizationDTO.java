package com.danp1t.bean;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrganizationDTO {

    @NotNull(message = "Name cannot be null")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Min(value = 0, message = "Annual turnover must be positive")
    private Long annualTurnover;

    @Min(value = 0, message = "Employees count must be positive")
    private Integer employeesCount;

    @Min(value = 0, message = "Rating must be positive")
    private Integer rating;

    @NotNull(message = "Type cannot be null")
    private OrganizationType type;

    // Конструкторы
    public OrganizationDTO() {}

    public OrganizationDTO(String name, Long annualTurnover, Integer employeesCount, int rating, OrganizationType type) {
        this.name = name;
        this.annualTurnover = annualTurnover;
        this.employeesCount = employeesCount;
        this.rating = rating;
        this.type = type;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getAnnualTurnover() { return annualTurnover; }
    public void setAnnualTurnover(Long annualTurnover) { this.annualTurnover = annualTurnover; }

    public Integer getEmployeesCount() { return employeesCount; }
    public void setEmployeesCount(Integer employeesCount) { this.employeesCount = employeesCount; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public OrganizationType getType() { return type; }
    public void setType(OrganizationType type) { this.type = type; }
}