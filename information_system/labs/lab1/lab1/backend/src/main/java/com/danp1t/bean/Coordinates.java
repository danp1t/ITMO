package com.danp1t.bean;

public class Coordinates {
    private Float x; //Не null. Значение поля должно быть > -59
    private double y; // > -5

    public Float getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public void setX(Float x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    public void validate() {
        if (this.x == null) {

        }
    }
}
