package com.cinema.modules.catalog.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gợi ý giờ bắt đầu còn trống trong một phòng, trong khung 08:00–23:30.
 * Khoảng bận đã gồm phút dọn phòng vì end_time của suất được lưu sẵn.
 */
public final class ShowtimeSlotPlanner {
    public static final LocalTime DAY_OPEN = LocalTime.of(8, 0);
    public static final LocalTime DAY_CLOSE = LocalTime.of(23, 30);
    private static final int MAX_SUGGESTIONS = 8;
    private static final DateTimeFormatter VALUE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private ShowtimeSlotPlanner() {
    }

    public static final class Block {
        private final String label;
        private final LocalDateTime start;
        private final LocalDateTime end;

        public Block(String label, LocalDateTime start, LocalDateTime end) {
            this.label = label == null ? "" : label.replace("<", "");
            this.start = start;
            this.end = end;
        }

        public String getLabel() {
            return label;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public String getStartValue() {
            return start.format(VALUE);
        }

        public String getEndValue() {
            return end.format(VALUE);
        }

        public String getClock() {
            return start.format(CLOCK) + "–" + end.format(CLOCK);
        }
    }

    public static final class StartOption {
        private final LocalDateTime start;

        public StartOption(LocalDateTime start) {
            this.start = start;
        }

        public String getValue() {
            return start.format(VALUE);
        }

        public String getLabel() {
            return start.format(CLOCK);
        }
    }

    public static List<StartOption> suggest(LocalDate date, int neededMinutes, List<Block> blocks) {
        List<StartOption> options = new ArrayList<>();
        if (date == null || neededMinutes <= 0) {
            return options;
        }
        LocalDateTime windowStart = date.atTime(DAY_OPEN);
        LocalDateTime windowEnd = date.atTime(DAY_CLOSE);
        List<LocalDateTime[]> busy = merge(windowStart, windowEnd, blocks);
        LocalDateTime cursor = windowStart;
        for (LocalDateTime[] interval : busy) {
            fill(cursor, interval[0], neededMinutes, options);
            if (interval[1].isAfter(cursor)) {
                cursor = snapUp(interval[1]);
            }
        }
        fill(cursor, windowEnd, neededMinutes, options);
        return options;
    }

    private static void fill(LocalDateTime from, LocalDateTime until, int neededMinutes, List<StartOption> options) {
        LocalDateTime cursor = snapUp(from);
        while (options.size() < MAX_SUGGESTIONS && !cursor.plusMinutes(neededMinutes).isAfter(until)) {
            options.add(new StartOption(cursor));
            cursor = snapUp(cursor.plusMinutes(neededMinutes));
        }
    }

    private static List<LocalDateTime[]> merge(LocalDateTime windowStart, LocalDateTime windowEnd, List<Block> blocks) {
        List<LocalDateTime[]> raw = new ArrayList<>();
        if (blocks != null) {
            for (Block block : blocks) {
                if (block == null || block.start == null || block.end == null || !block.end.isAfter(block.start)) {
                    continue;
                }
                LocalDateTime start = block.start.isBefore(windowStart) ? windowStart : block.start;
                LocalDateTime end = block.end.isAfter(windowEnd) ? windowEnd : block.end;
                if (end.isAfter(start)) {
                    raw.add(new LocalDateTime[]{start, end});
                }
            }
        }
        raw.sort(Comparator.comparing(interval -> interval[0]));
        List<LocalDateTime[]> merged = new ArrayList<>();
        for (LocalDateTime[] interval : raw) {
            if (merged.isEmpty() || interval[0].isAfter(merged.get(merged.size() - 1)[1])) {
                merged.add(interval);
            } else if (interval[1].isAfter(merged.get(merged.size() - 1)[1])) {
                merged.get(merged.size() - 1)[1] = interval[1];
            }
        }
        return merged;
    }

    static LocalDateTime snapUp(LocalDateTime time) {
        LocalDateTime trimmed = time.withSecond(0).withNano(0);
        int remainder = trimmed.getMinute() % 15;
        return remainder == 0 ? trimmed : trimmed.plusMinutes(15 - remainder);
    }
}
