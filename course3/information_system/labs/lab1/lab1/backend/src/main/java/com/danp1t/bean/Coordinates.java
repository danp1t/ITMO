package com.danp1t.bean;

import com.danp1t.error.NotNullError;
import com.danp1t.error.ValueTooSmallError;
import com.danp1t.interfaces.NeedValidate;
import jakarta.persistence.*;

@Entity
@Table(name = "coordinates")
public class Coordinates implements NeedValidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "x_coordinate")
    private Float x;

    @Column(name = "y_coordinate")
    private double y;

    public Coordinates() {}

    public Coordinates(Float x, double y) {
        this.x = x;
        this.y = y;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
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