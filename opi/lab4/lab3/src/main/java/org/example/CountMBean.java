package org.example;

public interface CountMBean {
    Integer getAllPoints();
    void setAllPoints(Integer allPoints);
    Integer getInsidePoints();
    void setInsidePoints(Integer insidePoints);
    boolean isMultipleOf15();
}
