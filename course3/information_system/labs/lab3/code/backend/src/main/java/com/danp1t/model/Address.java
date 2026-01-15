package com.danp1t.model;

import com.danp1t.error.NotNullError;
import com.danp1t.error.StringTooLongError;
import com.danp1t.interfaces.NeedValidate;

public class Address implements NeedValidate {

    private Long id;
    private String street;
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
        else if (this.street.length() >= 181) {
            throw new StringTooLongError("street", 181);
        }

        if (this.zipCode == null) {
            throw new NotNullError("zipCode");
        }
    }
}