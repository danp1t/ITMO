package com.example;

import java.io.Serializable;

public class ResultBean implements Serializable {
    private double x;
    private double y;
    private double r;
    private boolean isInArea;

    public ResultBean(double x, double y, double r, boolean isInArea) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.isInArea = isInArea;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getR() {
        return r;
    }

    public boolean isInArea() {
        return isInArea;
    }
}
