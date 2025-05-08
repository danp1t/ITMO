package org.example;

import jakarta.inject.Named;

import java.io.Serializable;

@Named("IntervalBean")
public class IntervalBean implements Serializable {
    private Double srInteval;

    public Double getSrInteval() {
        return srInteval;
    }

    public void setSrInteval(Double srInteval) {
        this.srInteval = srInteval;
    }
}
