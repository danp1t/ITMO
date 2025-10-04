package org.example;


import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import javax.management.JMException;
import java.io.Serializable;

@Named("Count")
@SessionScoped
public class Count implements Serializable, CountMBean{
    private int allPoints = 0;
    private int insidePoints = 0;

    public Count(){

    }
    @Inject
    public Count(RegMBeans reg) throws JMException {
        this.reg = reg;
        reg.registerBean(this);
    }
    RegMBeans reg;

    @Override
    public int getAllPoints() {
        return allPoints;
    }

    public void setAllPoints(int allPoints) {
        this.allPoints = allPoints;
    }

    @Override
    public int getInsidePoints() {
        return insidePoints;
    }

    public void setInsidePoints(int insidePoints) {
        this.insidePoints = insidePoints;
    }

    @Override
    public boolean isMultipleOf15() {
        return allPoints % 15 == 0 && allPoints != 0;
    }


}
