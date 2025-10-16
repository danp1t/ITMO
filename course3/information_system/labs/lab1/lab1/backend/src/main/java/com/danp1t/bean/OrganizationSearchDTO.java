package com.danp1t.bean;

public class OrganizationSearchDTO {
    private Integer id;
    private String name;
    private Float annualTurnover;
    private Long employeesCount;
    private Integer rating;
    private OrganizationType type;
    private Long coordinates;
    private Long officialAddress;
    private Long postalAddress;

    // Конструкторы
    public OrganizationSearchDTO() {}

    public OrganizationSearchDTO(Integer id, String name, Float annualTurnover, Long employeesCount,
                                 Integer rating, OrganizationType type, Long coordinates,
                                 Long officialAddress, Long postalAddress) {
        this.id = id;
        this.name = name;
        this.annualTurnover = annualTurnover;
        this.employeesCount = employeesCount;
        this.rating = rating;
        this.type = type;
        this.coordinates = coordinates;
        this.officialAddress = officialAddress;
        this.postalAddress = postalAddress;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Float getAnnualTurnover() { return annualTurnover; }
    public void setAnnualTurnover(Float annualTurnover) { this.annualTurnover = annualTurnover; }

    public Long getEmployeesCount() { return employeesCount; }
    public void setEmployeesCount(Long employeesCount) { this.employeesCount = employeesCount; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public OrganizationType getType() { return type; }
    public void setType(OrganizationType type) { this.type = type; }

    public Long getCoordinates() { return coordinates; }
    public void setCoordinates(Long coordinates) { this.coordinates = coordinates; }

    public Long getOfficialAddress() { return officialAddress; }
    public void setOfficialAddress(Long officialAddress) { this.officialAddress = officialAddress; }

    public Long getPostalAddress() { return postalAddress; }
    public void setPostalAddress(Long postalAddress) { this.postalAddress = postalAddress; }
}