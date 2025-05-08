package org.example;


import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("Count")
@SessionScoped
public class Count implements Serializable, CountMBean{
    private Integer allPoints = 0;
    private Integer insidePoints = 0;

    public Count(){

    }
    @Inject
    public Count(RegMBeans reg){
        this.reg = reg;
        reg.registerBean(this);
    }
    RegMBeans reg;

    @Override
    public Integer getAllPoints() {
        return allPoints;
    }

    @Override
    public void setAllPoints(Integer allPoints) {
        this.allPoints = allPoints;
    }

    @Override
    public Integer getInsidePoints() {
        return insidePoints;
    }

    @Override
    public void setInsidePoints(Integer insidePoints) {
        this.insidePoints = insidePoints;
    }

    @Override
    public boolean isMultipleOf15() {
        return allPoints % 15 == 0 && allPoints != 0;
    }


}
