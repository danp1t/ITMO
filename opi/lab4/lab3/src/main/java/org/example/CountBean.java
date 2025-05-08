package org.example;


import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("CountBean")
@SessionScoped
public class CountBean implements Serializable {
    private Integer allPoints = 0;
    private Integer insidePoints = 0;

    public Integer getAllPoints() {
        return allPoints;
    }
    public void setAllPoints(Integer allPoints) {
        this.allPoints = allPoints;
    }
    public Integer getInsidePoints() {
        return insidePoints;
    }
    public void setInsidePoints(Integer insidePoints) {
        this.insidePoints = insidePoints;
    }


}
