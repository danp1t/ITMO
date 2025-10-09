package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.interfaces.NeedValidate;
import jakarta.persistence.*;

@Entity
@Table(name = "location")
public class Location implements NeedValidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "x_location")
    private Double x;

    @Column(name = "y_location")
    private Float y;

    @Column(name = "z_location")
    private double z;

    @Column(name = "name_location")
    private String name;

    public Location() {}

    public Location(Double x, Float y, double z, String name) {
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
    public double getZ() {
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
    public void setZ(double z) {
        this.z = z;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void validate() {
        if (this.x == null) {
            throw new NotNullError("x");
        }
        if (this.y == null) {
            throw new NotNullError("y");
        }
        if (this.name == null) {
            throw new NotNullError("name");
        }
    }
}
