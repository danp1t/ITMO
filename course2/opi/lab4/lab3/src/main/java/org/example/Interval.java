package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

@Named("Interval")
@ApplicationScoped
public class Interval implements Serializable, IntervalMBean {
    private String averageInterval;
    private Instant lastClickTime;
    private Long duration = 0L;
    private Long totalIntervalMillis = 0L;

    public Interval(){

    }

    @Inject
    public Interval(RegMBeans reg){
        this.reg = reg;
        reg.registerBean(this);
    }
    RegMBeans reg;

    @Inject
    Count count;

    @Override
    public String getAverageInterval() {
        return String.format("%.2f сек", calcAverageInterval() / 1000);
    }

    public void averageInterval(String averageInterval) {
        this.averageInterval = averageInterval;
    }

    public Instant getLastClickTime() {
        return lastClickTime;
    }
    public void setLastClickTime(Instant lastClickTime) {
        this.lastClickTime = lastClickTime;
    }

    @Override
    public void registerClick() {
        Instant now = Instant.now();
        if (lastClickTime != null) {
            long interval = Duration.between(lastClickTime, now).toMillis();
            totalIntervalMillis += interval;
        }
        lastClickTime = now;
    }

    @Override
    public double calcAverageInterval() {
        if (count.getAllPoints() < 2) return 0;
        return (double) totalIntervalMillis / (count.getAllPoints() - 1);
    }
}
