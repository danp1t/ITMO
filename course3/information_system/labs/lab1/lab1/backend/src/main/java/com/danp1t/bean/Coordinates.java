package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.error.ValueTooSmallError;
import com.danp1t.interfaces.NeedValidate;

public class Coordinates implements NeedValidate {
    private Long id;
    private Float x;
    private double y;

    public Long getId() {
        return id;
    }
    public Float getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public void setId(Long id) {this.id = id;}
    public void setX(Float x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    public void validate() {
        if (this.x == null) {
            throw new NotNullError("x");
        }

        if (this.x <= -59) {
            throw new ValueTooSmallError("x", -59);
        }

        if (this.y <= -5) {
            throw new ValueTooSmallError("y", -5);
        }
    }
}
