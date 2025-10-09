package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.error.StringTooLongError;
import com.danp1t.interfaces.NeedValidate;
import jakarta.persistence.*;

@Entity
@Table(name = "address")
public class Address implements NeedValidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street")
    private String street;

    @Column(name = "zip_code")
    private String zipCode;

    public Address() {}

    public Address(String street, String zipCode) {
        this.street = street;
        this.zipCode = zipCode;
    }

    public Long getId() {return id;}
    public String getStreet() {
        return street;
    }
    public String getZipCode() {
        return zipCode;
    }

    public void setId(Long id) {this.id = id;}
    public void setStreet(String street) {
        this.street = street;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void validate() {
        if (this.street == null) {
            throw new NotNullError("street");
        }
        if (this.street.length() >= 181) {
            throw new StringTooLongError("street", 181);
        }

        if (this.zipCode == null) {
            throw new NotNullError("zipCode");
        }
    }
}
