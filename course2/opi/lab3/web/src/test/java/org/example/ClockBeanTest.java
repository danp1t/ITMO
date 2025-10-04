package org.example;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDateTime;

public class ClockBeanTest {
    
    @Test
    public void testGetCurrentTimeFormat() {
        ClockBean clock = new ClockBean();
        
        
        LocalDateTime testTime = LocalDateTime.of(2025, 3, 31, 20, 30, 45);
        
        try {
            var field = ClockBean.class.getDeclaredField("currentTime");
            field.setAccessible(true);
            field.set(clock, testTime);
        } catch (Exception e) {
            fail("Failed to set time via reflection");
        }
        
        assertEquals("31/03/2025 20:30:45", clock.getCurrentTime());
    }
}