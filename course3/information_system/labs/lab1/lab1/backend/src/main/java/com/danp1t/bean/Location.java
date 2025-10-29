package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.interfaces.NeedValidate;

public class Location implements NeedValidate {

    private Long id;
    private Double x;
    private Float y;
    private Double z;
    private String name;

    public Location() {}

    public Location(Double x, Float y, Double z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    public Long getId() {return id;}
    public Double getX() {
        return x;
    }
    public Float getY() {
        return y;
    }
    public Double getZ() {
        return z;
    }
    public String getName() {
        return name;
    }

    public void setId(Long id) {this.id = id;    }
    public void setX(Double x) {
        this.x = x;
    }
    public void setY(Float y) {
        this.y = y;
    }
    public void setZ(Double z) {
        this.z = z;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void validate() {
        if (this.x == null) {
            throw new NotNullError("x");
        }
        if (this.z == null) {
            throw new NotNullError("z");
        }
        if (this.name == null) {
            throw new NotNullError("name");
        }
    }
}
