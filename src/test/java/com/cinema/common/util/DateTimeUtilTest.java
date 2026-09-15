package com.cinema.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for DateTimeUtil")
class DateTimeUtilTest {

    @Test
    @DisplayName("Should format LocalDateTime to Vietnamese standard format dd/MM/yyyy HH:mm")
    void testFormatDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 10, 25, 19, 30);
        String formatted = DateTimeUtil.formatDateTime(dt);

        assertEquals("25/10/2024 19:30", formatted);
    }

    @Test
    @DisplayName("Should format LocalDate to dd/MM/yyyy")
    void testFormatDate() {
        LocalDate date = LocalDate.of(2024, 10, 25);
        String formatted = DateTimeUtil.formatDate(date);

        assertEquals("25/10/2024", formatted);
    }

    @Test
    @DisplayName("Should format time to HH:mm")
    void testFormatTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 10, 25, 9, 5);
        String formatted = DateTimeUtil.formatTime(dt);

        assertEquals("09:05", formatted);
    }

    @Test
    @DisplayName("Should parse standard DB datetime string yyyy-MM-dd HH:mm:ss")
    void testParseDbDateTime() {
        String dbText = "2024-10-25 19:30:00";
        LocalDateTime parsed = DateTimeUtil.parseDbDateTime(dbText);

        assertNotNull(parsed);
        assertEquals(2024, parsed.getYear());
        assertEquals(10, parsed.getMonthValue());
        assertEquals(25, parsed.getDayOfMonth());
        assertEquals(19, parsed.getHour());
        assertEquals(30, parsed.getMinute());
    }

    @Test
    @DisplayName("Should handle null and empty values gracefully")
    void testNullAndEmptyHandling() {
        assertEquals("", DateTimeUtil.formatDateTime(null));
        assertEquals("", DateTimeUtil.formatDate(null));
        assertEquals("", DateTimeUtil.formatTime(null));
        assertNull(DateTimeUtil.parseDbDateTime(null));
        assertNull(DateTimeUtil.parseDbDateTime("   "));
    }
}
