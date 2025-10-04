package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.error.StringNotEmptyError;
import com.danp1t.error.ValueTooSmallError;
import com.danp1t.interfaces.NeedValidate;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Organization implements NeedValidate {
    private Integer id; //Уникальное. Генерируется автоматически
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDate creationDate; //Генерируется автоматически
    private Address officialAddress;
    private float annualTurnover;
    private long employeesCount;
    private int rating;
    private OrganizationType type;
    private Address postalAddress;

    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }
    public java.time.LocalDate getCreationDate() {
        return creationDate;
    }
    public Address getOfficialAddress() {
        return officialAddress;
    }
    public float getAnnualTurnover() {
        return annualTurnover;
    }
    public long getEmployeesCount() {
        return employeesCount;
    }
    public int getRating() {
        return rating;
    }
    public Address getPostalAddress() {
        return postalAddress;
    }


    public void setId(Integer id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    public void setCreationDate(java.time.LocalDate creationDate) {
        this.creationDate = creationDate;
    }
    public void setOfficialAddress(Address officialAddress) {
        this.officialAddress = officialAddress;
    }
    public void setAnnualTurnover(float annualTurnover) {
        this.annualTurnover = annualTurnover;
    }
    public void setEmployeesCount(long employeesCount) {
        this.employeesCount = employeesCount;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public void setPostalAddress(Address postalAddress) {
        this.postalAddress = postalAddress;
    }

    public void validate() {
        if (this.id == null) {
            throw new NotNullError("id");
        }
        if (this.id <= 0) {
            throw new ValueTooSmallError("id", 0);
        }

        if (this.name == null) {
            throw new NotNullError("name");
        }
        if (this.name.isEmpty()) {
            throw new StringNotEmptyError("name");
        }

        if (this.coordinates == null) {
            throw new NotNullError("coordinates");
        }

        if (this.creationDate == null) {
            throw new NotNullError("creationDate");
        }

        if (this.officialAddress == null) {
            throw new NotNullError("officialAddress");
        }

        if (this.annualTurnover <= 0) {
            throw new ValueTooSmallError("annualTurnover", 0);
        }

        if (this.employeesCount <= 0) {
            throw new ValueTooSmallError("employeesCount", 0);
        }

        if (this.rating <= 0) {
            throw new ValueTooSmallError("rating", 0);
        }

        if (this.type == null) {
            throw new NotNullError("type");
        }

        if (this.postalAddress == null) {
            throw new NotNullError("postalAddress");
        }
    }
}
