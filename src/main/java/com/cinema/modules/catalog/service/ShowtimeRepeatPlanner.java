package com.cinema.modules.catalog.service;

import com.cinema.modules.catalog.service.ShowtimeSlotPlanner.Block;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tính các ngày lặp và bảng xem trước khi tạo nhiều suất một lần.
 * Không ghi database. Servlet vẫn gọi kiểm tra phòng lúc bấm lưu.
 */
public final class ShowtimeRepeatPlanner {
    public static final int MAX_SPAN_DAYS = 28;
    public static final int MAX_TIMES = 8;
    public static final int MAX_ROWS = 48;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter VALUE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private ShowtimeRepeatPlanner() {
    }

    public static List<LocalDate> dates(LocalDate start, LocalDate until, Set<DayOfWeek> days) {
        LocalDate from = start == null ? LocalDate.now() : start;
        Set<DayOfWeek> selected = days == null || days.isEmpty()
                ? EnumSet.of(from.getDayOfWeek())
                : EnumSet.copyOf(days);
        LocalDate end = until == null ? from.plusDays(6) : until;
        if (end.isBefore(from)) {
            end = from;
        }
        LocalDate cap = from.plusDays(MAX_SPAN_DAYS - 1L);
        if (end.isAfter(cap)) {
            end = cap;
        }
        List<LocalDate> out = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(end); day = day.plusDays(1)) {
            if (selected.contains(day.getDayOfWeek())) {
                out.add(day);
            }
        }
        return out;
    }

    public static String summary(List<LocalDate> dates) {
        String tail = " Không chọn ngày lặp đến thì hệ thống lấy 7 ngày kể từ ngày bắt đầu. Xa nhất là 4 tuần.";
        if (dates == null || dates.isEmpty()) {
            return "Không có ngày nào khớp các thứ đã chọn." + tail;
        }
        StringBuilder text = new StringBuilder("Các ngày: ");
        for (int i = 0; i < dates.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(dates.get(i).format(DATE));
        }
        text.append('.').append(tail);
        return text.toString();
    }

    public static List<WeekDay> week(Set<DayOfWeek> selected) {
        String[] labels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        Set<DayOfWeek> picked = selected == null ? Set.of() : selected;
        List<WeekDay> days = new ArrayList<>();
        for (int index = 0; index < labels.length; index++) {
            DayOfWeek day = DayOfWeek.of(index + 1);
            days.add(new WeekDay(index + 1, labels[index], picked.contains(day)));
        }
        return days;
    }

    public static List<ChosenTime> chosen(List<LocalTime> times, int neededMinutes) {
        List<ChosenTime> chosen = new ArrayList<>();
        if (times == null) {
            return chosen;
        }
        for (LocalTime time : times) {
            chosen.add(new ChosenTime(time.format(CLOCK), time.plusMinutes(neededMinutes).format(CLOCK)));
        }
        return chosen;
    }

    public static List<Preview> preview(List<LocalDate> dates, List<LocalTime> times, int neededMinutes,
                                        Map<LocalDate, List<Block>> blocksByDate) {
        List<Preview> rows = new ArrayList<>();
        if (dates == null || times == null || neededMinutes <= 0) {
            return rows;
        }
        for (LocalDate date : dates) {
            if (rows.size() >= MAX_ROWS) {
                break;
            }
            List<Block> blocks = blocksByDate == null ? List.of() : blocksByDate.getOrDefault(date, List.of());
            List<LocalDateTime[]> accepted = new ArrayList<>();
            for (LocalTime time : times) {
                if (rows.size() >= MAX_ROWS) {
                    break;
                }
                LocalDateTime start = date.atTime(time);
                LocalDateTime end = start.plusMinutes(neededMinutes);
                LocalDateTime open = date.atTime(ShowtimeSlotPlanner.DAY_OPEN);
                LocalDateTime close = date.atTime(ShowtimeSlotPlanner.DAY_CLOSE);
                if (start.isBefore(open) || end.isAfter(close)) {
                    rows.add(row(start, end, "Ngoài khung 08:00–23:30", false));
                    continue;
                }
                if (overlapsAny(start, end, accepted)) {
                    rows.add(row(start, end, "Trùng giờ đã chọn", false));
                    continue;
                }
                Block hit = firstOverlap(start, end, blocks);
                if (hit != null) {
                    String label = hit.getLabel() == null || hit.getLabel().isBlank() ? "suất khác" : hit.getLabel();
                    rows.add(row(start, end, "Trùng " + label + " · " + hit.getClock(), false));
                    continue;
                }
                accepted.add(new LocalDateTime[]{start, end});
                rows.add(row(start, end, "Trống, sẽ tạo", true));
            }
        }
        return rows;
    }

    private static Preview row(LocalDateTime start, LocalDateTime end, String result, boolean free) {
        return new Preview(start.format(VALUE), start.format(DATE), start.format(CLOCK), end.format(CLOCK), result, free);
    }

    private static boolean overlapsAny(LocalDateTime start, LocalDateTime end, List<LocalDateTime[]> accepted) {
        for (LocalDateTime[] interval : accepted) {
            if (start.isBefore(interval[1]) && end.isAfter(interval[0])) {
                return true;
            }
        }
        return false;
    }

    private static Block firstOverlap(LocalDateTime start, LocalDateTime end, List<Block> blocks) {
        for (Block block : blocks) {
            if (block.getStart() != null && block.getEnd() != null
                    && start.isBefore(block.getEnd()) && end.isAfter(block.getStart())) {
                return block;
            }
        }
        return null;
    }

    public static final class WeekDay {
        private final int value;
        private final String label;
        private final boolean selected;

        public WeekDay(int value, String label, boolean selected) {
            this.value = value;
            this.label = label;
            this.selected = selected;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public boolean isSelected() {
            return selected;
        }
    }

    public static final class ChosenTime {
        private final String clock;
        private final String endClock;

        public ChosenTime(String clock, String endClock) {
            this.clock = clock;
            this.endClock = endClock;
        }

        public String getClock() {
            return clock;
        }

        public String getEndClock() {
            return endClock;
        }
    }

    public static final class Preview {
        private final String value;
        private final String dateLabel;
        private final String startLabel;
        private final String endLabel;
        private final String result;
        private final boolean free;

        public Preview(String value, String dateLabel, String startLabel, String endLabel, String result, boolean free) {
            this.value = value;
            this.dateLabel = dateLabel;
            this.startLabel = startLabel;
            this.endLabel = endLabel;
            this.result = result;
            this.free = free;
        }

        public String getValue() {
            return value;
        }

        public String getDateLabel() {
            return dateLabel;
        }

        public String getStartLabel() {
            return startLabel;
        }

        public String getEndLabel() {
            return endLabel;
        }

        public String getResult() {
            return result;
        }

        public boolean isFree() {
            return free;
        }
    }
}
