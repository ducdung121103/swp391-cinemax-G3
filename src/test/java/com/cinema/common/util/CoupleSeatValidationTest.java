package com.cinema.common.util;

import com.cinema.model.Seat;
import com.cinema.model.SeatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for Sweetbox Couple Seat Validation (Nghiệp vụ chọn ghế đôi)")
class CoupleSeatValidationTest {

    private Seat createSeat(Long id, String row, int number, String code, Long typeId, String typeCode) {
        SeatType st = new SeatType();
        st.setId(typeId);
        st.setTypeCode(typeCode);
        st.setName("COUPLE".equalsIgnoreCase(typeCode) ? "Ghế Đôi Sweetbox" : "Ghế Tiêu Chuẩn");

        Seat seat = new Seat();
        seat.setId(id);
        seat.setSeatRow(row);
        seat.setSeatNumber(number);
        seat.setSeatCode(code);
        seat.setSeatTypeId(typeId);
        seat.setSeatType(st);
        return seat;
    }

    private Seat createCoupleSeat(Long id, String row, int number) {
        String code = row + String.format("%02d", number);
        return createSeat(id, row, number, code, 3L, "COUPLE");
    }

    private Seat createStandardSeat(Long id, String row, int number) {
        String code = row + String.format("%02d", number);
        return createSeat(id, row, number, code, 1L, "STANDARD");
    }

    @Test
    @DisplayName("Should pass when seat list is null or empty")
    void testValidateCoupleSeats_NullOrEmpty_ShouldPass() {
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(null));
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(Collections.emptyList()));
    }

    @Test
    @DisplayName("Should pass when only standard or VIP seats are selected")
    void testValidateCoupleSeats_OnlyStandardSeats_ShouldPass() {
        List<Seat> seats = List.of(
                createStandardSeat(1L, "A", 1),
                createStandardSeat(2L, "A", 2),
                createStandardSeat(3L, "A", 5)
        );
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(seats));
    }

    @Test
    @DisplayName("Should reject single couple seat (odd count = 1)")
    void testValidateCoupleSeats_SingleCoupleSeat_ShouldFail() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCoupleSeats(seats));
        assertTrue(ex.getMessage().contains("số lượng chẵn"));
        assertTrue(ex.getMessage().contains("1 ghế đôi"));
    }

    @Test
    @DisplayName("Should reject three couple seats (odd count = 3)")
    void testValidateCoupleSeats_ThreeCoupleSeats_ShouldFail() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(102L, "H", 2),
                createCoupleSeat(103L, "H", 3)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCoupleSeats(seats));
        assertTrue(ex.getMessage().contains("số lượng chẵn"));
        assertTrue(ex.getMessage().contains("3 ghế đôi"));
    }

    @Test
    @DisplayName("Should reject two couple seats in different rows (e.g. H01 and I01)")
    void testValidateCoupleSeats_TwoSeatsDifferentRows_ShouldFail() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(201L, "I", 1)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCoupleSeats(seats));
        assertTrue(ex.getMessage().contains("cùng một hàng"));
    }

    @Test
    @DisplayName("Should reject two couple seats in same row with 1-seat gap (e.g. H01 and H03 - cách nhau 1 ghế)")
    void testValidateCoupleSeats_TwoSeatsWithOneSeatGap_ShouldFail() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(103L, "H", 3)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCoupleSeats(seats));
        assertTrue(ex.getMessage().contains("liền kề"));
        assertTrue(ex.getMessage().contains("H01"));
        assertTrue(ex.getMessage().contains("H03"));
        assertTrue(ex.getMessage().contains("cách nhau 1 ghế"));
    }

    @Test
    @DisplayName("Should reject two couple seats in same row with multiple seats gap (e.g. H01 and H05)")
    void testValidateCoupleSeats_TwoSeatsWithMultipleGaps_ShouldFail() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(105L, "H", 5)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCoupleSeats(seats));
        assertTrue(ex.getMessage().contains("liền kề"));
        assertTrue(ex.getMessage().contains("cách nhau 3 ghế"));
    }

    @Test
    @DisplayName("Should accept two adjacent couple seats in same row (e.g. H01 and H02)")
    void testValidateCoupleSeats_TwoAdjacentCoupleSeats_ShouldPass() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(102L, "H", 2)
        );
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(seats));
    }

    @Test
    @DisplayName("Should accept four couple seats as two valid pairs in same row (e.g. H01-H02 and H05-H06)")
    void testValidateCoupleSeats_FourSeatsTwoPairsSameRow_ShouldPass() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(102L, "H", 2),
                createCoupleSeat(105L, "H", 5),
                createCoupleSeat(106L, "H", 6)
        );
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(seats));
    }

    @Test
    @DisplayName("Should accept four couple seats across two different rows with 1 pair each (e.g. H01-H02 and I03-I04)")
    void testValidateCoupleSeats_FourSeatsTwoRows_ShouldPass() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(102L, "H", 2),
                createCoupleSeat(203L, "I", 3),
                createCoupleSeat(204L, "I", 4)
        );
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(seats));
    }

    @Test
    @DisplayName("Should reject four couple seats where one pair has a gap (e.g. H01-H02 OK, but H04-H06 separated)")
    void testValidateCoupleSeats_FourSeatsWithBrokenPair_ShouldFail() {
        List<Seat> seats = List.of(
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(102L, "H", 2),
                createCoupleSeat(104L, "H", 4),
                createCoupleSeat(106L, "H", 6)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCoupleSeats(seats));
        assertTrue(ex.getMessage().contains("liền kề"));
        assertTrue(ex.getMessage().contains("H04"));
        assertTrue(ex.getMessage().contains("H06"));
    }

    @Test
    @DisplayName("Should accept combination of standard seat and valid couple pair (e.g. A01 standard + H01-H02 couple)")
    void testValidateCoupleSeats_MixedStandardAndValidCouple_ShouldPass() {
        List<Seat> seats = List.of(
                createStandardSeat(1L, "A", 1),
                createCoupleSeat(101L, "H", 1),
                createCoupleSeat(102L, "H", 2)
        );
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(seats));
    }

    @Test
    @DisplayName("Should reject combination of standard seat and single couple seat (e.g. A01 standard + H01 couple)")
    void testValidateCoupleSeats_MixedStandardAndSingleCouple_ShouldFail() {
        List<Seat> seats = List.of(
                createStandardSeat(1L, "A", 1),
                createCoupleSeat(101L, "H", 1)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateCoupleSeats(seats));
        assertTrue(ex.getMessage().contains("số lượng chẵn"));
    }

    @Test
    @DisplayName("Should test isCoupleSeat helper accurately")
    void testIsCoupleSeatHelper() {
        Seat couple1 = createCoupleSeat(1L, "H", 1);
        assertTrue(ValidationUtil.isCoupleSeat(couple1));

        Seat standard = createStandardSeat(2L, "A", 1);
        assertFalse(ValidationUtil.isCoupleSeat(standard));

        Seat seatTypeIdOnly = new Seat();
        seatTypeIdOnly.setSeatTypeId(3L);
        assertTrue(ValidationUtil.isCoupleSeat(seatTypeIdOnly));

        assertFalse(ValidationUtil.isCoupleSeat(null));
    }
}
