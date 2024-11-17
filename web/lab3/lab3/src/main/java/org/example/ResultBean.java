package org.example;


import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("ResultBean")
@SessionScoped
public class ResultBean implements Serializable {
    private static final long serialVersionUID = 2L;
    private double x;
    private double y;
    private double r;
    private boolean status;

    public ResultBean(double x, double y, double r, boolean status) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.status = status;
    }
    public ResultBean() {

    }


    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getR() {
        return r;
    }
    public void setR(double r) {
        this.r = r;
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
}
