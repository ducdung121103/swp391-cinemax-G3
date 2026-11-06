package com.cinema.modules.catalog;

import com.cinema.modules.catalog.service.ShowtimeSlotPlanner;
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner.Block;
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner.StartOption;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShowtimeSlotPlannerTest {

    @Test
    void suggestsStartsThatFitAroundAnExistingShow() {
        LocalDate date = LocalDate.of(2026, 10, 16);
        Block busy = new Block("Dune",
                LocalDateTime.of(2026, 10, 16, 9, 30),
                LocalDateTime.of(2026, 10, 16, 12, 31));

        List<StartOption> options = ShowtimeSlotPlanner.suggest(date, 60, List.of(busy));

        assertEquals("08:00", options.get(0).getLabel());
        assertTrue(options.stream().noneMatch(option -> option.getLabel().equals("09:30")));
        assertTrue(options.stream().anyMatch(option -> option.getLabel().equals("12:45")));
    }
}
