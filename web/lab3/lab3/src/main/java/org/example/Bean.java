package org.example;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Named
@RequestScoped
public class Bean {
    // Ваш код здесь

    private LocalDateTime currentTime;

    public Bean() {
        updateCurrentTime();
    }

    public void updateCurrentTime() {
        currentTime = LocalDateTime.now();
    }

    public String getFormattedCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentTime.format(formatter);
    }
}