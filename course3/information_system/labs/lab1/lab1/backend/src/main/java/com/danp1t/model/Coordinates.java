package com.danp1t.model;

import com.danp1t.error.NotNullError;
import com.danp1t.error.ValueTooBigError;
import com.danp1t.error.ValueTooSmallError;
import com.danp1t.interfaces.NeedValidate;

public class Coordinates implements NeedValidate {

    private Long id;
    private Float x;
    private Double y;

    public Coordinates() {}

    public Coordinates(Float x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Long getId() {return id;}
    public Float getX() {return x;}
    public Double getY() {return y;}

    public void setId(Long id) {this.id = id;}
    public void setX(Float x) {this.x = x;}
    public void setY(Double y) {this.y = y;}

    @Override
    public void validate() {
        if (this.x == null) {
            throw new NotNullError("x");
        }

        if (this.x <= -59) {
            throw new ValueTooSmallError("x", -59);
        }
        else if (this.x >= Float.MAX_VALUE) {
            throw new ValueTooBigError("x", (long) Float.MAX_VALUE);
        }

        if (this.y <= -5) {
            throw new ValueTooSmallError("y", -5);
        }
        else if (this.y >= Double.MAX_VALUE) {
            throw new ValueTooBigError("y", (long) Double.MAX_VALUE);
        }
    }
}
