package com.cinema.common.util;

import com.cinema.model.Seat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Tiá»‡n Ã­ch kiá»ƒm tra tÃ­nh há»£p lá»‡ cá»§a dá»¯ liá»‡u Ä‘áº§u vÃ o (Validation).
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0[3|5|7|8|9])[0-9]{8}$");
    // Máº­t kháº©u: Tá»‘i thiá»ƒu 8 kÃ½ tá»±, Ã­t nháº¥t 1 chá»¯ hoa, 1 chá»¯ thÆ°á»ng, 1 sá»‘, 1 kÃ½ tá»± Ä‘áº·c biá»‡t
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Kiểm tra xem một ghế có phải là ghế đôi Sweetbox (COUPLE) hay không.
     *
     * @param seat thực thể ghế
     * @return true nếu là ghế đôi Sweetbox
     */
    public static boolean isCoupleSeat(Seat seat) {
        if (seat == null) return false;
        if (seat.getSeatType() != null) {
            String typeCode = seat.getSeatType().getTypeCode();
            String name = seat.getSeatType().getName();
            if ("COUPLE".equalsIgnoreCase(typeCode)) return true;
            if (name != null && name.toLowerCase().contains("sweetbox")) return true;
            if (name != null && name.toLowerCase().contains("đôi")) return true;
        }
        return seat.getSeatTypeId() != null && seat.getSeatTypeId() == 3L;
    }

    /**
     * Nghiệp vụ kiểm tra tính hợp lệ khi chọn ghế đôi Sweetbox:
     * 1. Số lượng ghế đôi Sweetbox phải là số chẵn (ví dụ: 2, 4, 6... ghế; cấm chọn 1 hoặc 3 ghế).
     * 2. Các ghế đôi Sweetbox phải được đặt theo từng cặp liền kề nhau trong cùng một hàng
     *    (2 ghế nhưng cách nhau 1 hoặc nhiều ghế, hoặc 2 ghế ở 2 hàng khác nhau đều không được phép).
     *
     * @param seats danh sách ghế khách chọn
     * @throws IllegalArgumentException nếu vi phạm bất kỳ quy tắc nào
     */
    public static void validateCoupleSeats(List<Seat> seats) throws IllegalArgumentException {
        if (seats == null || seats.isEmpty()) {
            return;
        }

        // Lọc danh sách các ghế đôi Sweetbox
        List<Seat> coupleSeats = new ArrayList<>();
        for (Seat s : seats) {
            if (isCoupleSeat(s)) {
                coupleSeats.add(s);
            }
        }

        // Nếu không chọn ghế đôi nào (chỉ chọn Standard/VIP) thì hợp lệ
        if (coupleSeats.isEmpty()) {
            return;
        }

        // Quy tắc 1: Tổng số ghế đôi Sweetbox phải là số chẵn
        if (coupleSeats.size() % 2 != 0) {
            throw new IllegalArgumentException(
                "Ghế đôi Sweetbox phải được chọn theo số lượng chẵn (bạn đang chọn "
                + coupleSeats.size() + " ghế đôi; ví dụ: 2, 4, 6... ghế, không được chọn số lẻ như 1 hay 3 ghế)!"
            );
        }

        // Quy tắc 2: Phải đi theo cặp cùng hàng (không được ghép chéo hàng)
        Map<String, List<Seat>> byRow = new LinkedHashMap<>();
        for (Seat s : coupleSeats) {
            String row = s.getSeatRow() != null ? s.getSeatRow().trim().toUpperCase() : "";
            byRow.computeIfAbsent(row, k -> new ArrayList<>()).add(s);
        }

        for (Map.Entry<String, List<Seat>> entry : byRow.entrySet()) {
            String row = entry.getKey();
            List<Seat> rowSeats = entry.getValue();

            // Nếu số ghế đôi trong 1 hàng là số lẻ -> không thể ghép thành cặp nội bộ hàng đó
            if (rowSeats.size() % 2 != 0) {
                throw new IllegalArgumentException(
                    "Ghế đôi Sweetbox phải được chọn theo cặp trong cùng một hàng (hàng "
                    + row + " đang chọn " + rowSeats.size() + " ghế lẻ, không thể ghép cặp)!"
                );
            }

            // Quy tắc 3: Trong cùng một hàng, các ghế phải liền kề nhau theo từng cặp (không được cách quãng)
            rowSeats.sort((a, b) -> {
                int numA = a.getSeatNumber() != null ? a.getSeatNumber() : (a.getGridColIndex() != null ? a.getGridColIndex() : 0);
                int numB = b.getSeatNumber() != null ? b.getSeatNumber() : (b.getGridColIndex() != null ? b.getGridColIndex() : 0);
                return Integer.compare(numA, numB);
            });

            for (int i = 0; i < rowSeats.size(); i += 2) {
                Seat s1 = rowSeats.get(i);
                Seat s2 = rowSeats.get(i + 1);
                int num1 = s1.getSeatNumber() != null ? s1.getSeatNumber() : (s1.getGridColIndex() != null ? s1.getGridColIndex() : 0);
                int num2 = s2.getSeatNumber() != null ? s2.getSeatNumber() : (s2.getGridColIndex() != null ? s2.getGridColIndex() : 0);

                int diff = Math.abs(num2 - num1);
                if (diff != 1) {
                    int gap = diff - 1;
                    throw new IllegalArgumentException(
                        "Ghế đôi Sweetbox được chọn phải liền kề nhau, không được cách quãng! Cặp ghế "
                        + s1.getSeatCode() + " và " + s2.getSeatCode() + " đang cách nhau " + gap + " ghế, không hợp lệ."
                    );
                }
            }
        }
    }
}

