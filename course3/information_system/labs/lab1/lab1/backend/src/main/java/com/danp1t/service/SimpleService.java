package com.danp1t.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class SimpleService {

    private int callCount = 0;

    public String getTestMessage() {
        callCount++;
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        return String.format(
                "CDI Service is working! Call #%d at %s",
                callCount, timestamp
        );
    }
}