package com.cinema.modules.catalog;

import com.cinema.modules.catalog.service.ShowtimeRepeatPlanner;
import com.cinema.modules.catalog.service.ShowtimeRepeatPlanner.Preview;
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner.Block;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShowtimeRepeatPlannerTest {

    @Test
    void keepsOnlyCheckedWeekdaysInsideOneWeekWhenUntilIsEmpty() {
        LocalDate thursday = LocalDate.of(2026, 11, 5);
        List<LocalDate> dates = ShowtimeRepeatPlanner.dates(
                thursday, null, EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));

        assertEquals(List.of(LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 7)), dates);
    }

    @Test
    void repeatsCheckedWeekdaysUntilTheEndDate() {
        LocalDate monday = LocalDate.of(2026, 11, 2);
        List<LocalDate> dates = ShowtimeRepeatPlanner.dates(
                monday, LocalDate.of(2026, 11, 15), EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        assertEquals(List.of(
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 4),
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 11)
        ), dates);
    }

    @Test
    void marksRoomConflictAndALaterFreeTime() {
        LocalDate date = LocalDate.of(2026, 11, 6);
        Block busy = new Block("Dune",
                LocalDateTime.of(2026, 11, 6, 10, 0),
                LocalDateTime.of(2026, 11, 6, 12, 15));

        List<Preview> rows = ShowtimeRepeatPlanner.preview(
                List.of(date),
                List.of(LocalTime.of(10, 0), LocalTime.of(14, 0)),
                60,
                Map.of(date, List.of(busy)));

        assertEquals(2, rows.size());
        assertFalse(rows.get(0).isFree());
        assertTrue(rows.get(0).getResult().startsWith("Trùng Dune"));
        assertTrue(rows.get(1).isFree());
        assertEquals("2026-11-06T14:00", rows.get(1).getValue());
    }

    @Test
    void marksASecondSelectedTimeThatOverlapsTheFirst() {
        LocalDate date = LocalDate.of(2026, 11, 6);
        List<Preview> rows = ShowtimeRepeatPlanner.preview(
                List.of(date),
                List.of(LocalTime.of(10, 0), LocalTime.of(11, 0)),
                90,
                Map.of());

        assertTrue(rows.get(0).isFree());
        assertFalse(rows.get(1).isFree());
        assertEquals("Trùng giờ đã chọn", rows.get(1).getResult());
    }

    @Test
    void allowsALaterTimeWhenTheEarlierOneIsAlreadyBusy() {
        LocalDate date = LocalDate.of(2026, 11, 6);
        Block busy = new Block("Bảo trì",
                LocalDateTime.of(2026, 11, 6, 9, 0),
                LocalDateTime.of(2026, 11, 6, 11, 0));

        List<Preview> rows = ShowtimeRepeatPlanner.preview(
                List.of(date),
                List.of(LocalTime.of(9, 30), LocalTime.of(11, 0)),
                60,
                Map.of(date, List.of(busy)));

        assertFalse(rows.get(0).isFree());
        assertTrue(rows.get(1).isFree());
    }
}
