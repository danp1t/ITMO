package org.example;

public interface CountMBean {
    int getAllPoints();
    void setAllPoints(int allPoints);
    int getInsidePoints();
    void setInsidePoints(int insidePoints);
    boolean isMultipleOf15();
}
