package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.error.StringNotEmptyError;
import com.danp1t.error.ValueTooSmallError;
import com.danp1t.interfaces.NeedValidate;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "organization")
public class Organization implements NeedValidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //Уникальное. Генерируется автоматически

    @Column(nullable = false)
    private String name;

    @ManyToOne
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false)
    private java.time.LocalDate creationDate;

    @ManyToOne
    @JoinColumn(name = "official_address_id", nullable = false)
    private Location officialAddress;

    @Column(name = "annual_turnover", nullable = false)
    private float annualTurnover;

    @Column(name = "employees_count", nullable = false)
    private long employeesCount;

    @Column(nullable = false)
    private int rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationType type;

    @ManyToOne
    @JoinColumn(name = "postal_address_id", nullable = false)
    private Address postalAddress;

    public Organization() {
        this.creationDate = LocalDate.now();
    }

    public Organization(String name, Coordinates coordinates, Location officialAddress,
                        float annualTurnover, long employeesCount, int rating,
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
    public float getAnnualTurnover() {
        return annualTurnover;
    }
    public long getEmployeesCount() {
        return employeesCount;
    }
    public int getRating() {
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
    public void setAnnualTurnover(float annualTurnover) {
        this.annualTurnover = annualTurnover;
    }
    public void setEmployeesCount(long employeesCount) {
        this.employeesCount = employeesCount;
    }
    public void setRating(int rating) {
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
