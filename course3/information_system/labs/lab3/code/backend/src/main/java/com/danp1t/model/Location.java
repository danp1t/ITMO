package com.danp1t.model;

import com.danp1t.error.NotNullError;
import com.danp1t.error.StringTooLongError;
import com.danp1t.error.ValueTooBigError;
import com.danp1t.error.ValueTooSmallError;
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
        else if (this.x < -Double.MAX_VALUE) {
            throw new ValueTooSmallError("x", (int) -Double.MAX_VALUE);
        }
        else if (this.x > Double.MAX_VALUE) {
            throw new ValueTooBigError("x", (long) Double.MAX_VALUE);
        }

        if (this.y == null) {}
        else if (this.y < -Float.MAX_VALUE) {
            throw new ValueTooSmallError("y", (int) -Float.MAX_VALUE);
        }
        else if (this.y > Float.MAX_VALUE) {
            throw new ValueTooBigError("y", (long) Float.MAX_VALUE);
        }

        if (this.z == null) {
            throw new NotNullError("z");
        }
        else if (this.z < -Double.MAX_VALUE) {
            throw new ValueTooSmallError("z", (int) -Double.MAX_VALUE);
        }
        else if (this.z > Double.MAX_VALUE) {
            throw new ValueTooBigError("z", (long) Double.MAX_VALUE);
        }

        if (this.name == null) {
            throw new NotNullError("name");
        }
        else if (this.name.length() >= 256) {
            throw new StringTooLongError("name", 181);
        }
    }
}
