package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

@Named
@ApplicationScoped
public class ClockBean {
    private LocalDateTime currentTime;

    private Timer timer;

    @PostConstruct
    public void init() {
        currentTime = LocalDateTime.now();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCurrentTime();
            }
        }, 0, 9000); // Обновляем каждую 9 секунд
    }

    public void updateCurrentTime() {
        currentTime = LocalDateTime.now();
    }

    public String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return currentTime.format(formatter);
    }

    @PreDestroy
    public void cleanup() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
