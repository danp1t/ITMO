package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.error.StringNotEmptyError;
import com.danp1t.error.ValueTooSmallError;
import com.danp1t.interfaces.NeedValidate;

import java.time.LocalDate;

public class Organization implements NeedValidate {
    private Integer id;
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDate creationDate;
    private Location officialAddress;
    private Float annualTurnover;
    private Long employeesCount;
    private Integer rating;
    private OrganizationType type;
    private Address postalAddress;

    public Organization() {
        this.creationDate = LocalDate.now();
    }

    public Organization(String name, Coordinates coordinates, Location officialAddress,
                        Float annualTurnover, Long employeesCount, Integer rating,
                        OrganizationType type, Address postalAddress) {
        this();
        this.name = name;
        this.coordinates = coordinates;
        this.officialAddress = officialAddress;
        this.annualTurnover = annualTurnover;
        this.employeesCount = employeesCount;
        this.rating = rating;
        this.type = type;
        this.postalAddress = postalAddress;
    }

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
    public Location getOfficialAddress() {
        return officialAddress;
    }
    public Float getAnnualTurnover() {
        return annualTurnover;
    }
    public Long getEmployeesCount() {
        return employeesCount;
    }
    public Integer getRating() {
        return rating;
    }
    public OrganizationType getType(){return type;}
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
    public void setOfficialAddress(Location officialAddress) {
        this.officialAddress = officialAddress;
    }
    public void setAnnualTurnover(Float annualTurnover) {
        this.annualTurnover = annualTurnover;
    }
    public void setEmployeesCount(Long employeesCount) {
        this.employeesCount = employeesCount;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public void setType(OrganizationType type){this.type = type;}
    public void setPostalAddress(Address postalAddress) {
        this.postalAddress = postalAddress;
    }

    public void validate() {
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
