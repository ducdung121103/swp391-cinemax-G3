package com.cinema.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for ValidationUtil")
class ValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"admin@cinema.com", "customer.vip@gmail.com", "user123@fpt.edu.vn", "test+tag@sub.domain.org"})
    @DisplayName("Should validate valid email addresses")
    void testValidEmail(String email) {
        assertTrue(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "missing@domain", "@missinguser.com", "spaces in@email.com", "", "   "})
    @DisplayName("Should reject invalid email addresses")
    void testInvalidEmail(String email) {
        assertFalse(ValidationUtil.isValidEmail(email));
    }

    @Test
    @DisplayName("Should reject null email")
    void testNullEmail() {
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0901234567", "0987654321", "0356789012", "0778899001", "0891234567"})
    @DisplayName("Should validate correct Vietnamese phone numbers")
    void testValidPhone(String phone) {
        assertTrue(ValidationUtil.isValidPhone(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0123456789", "090123456", "09012345678", "abcdefghij", "", "   "})
    @DisplayName("Should reject invalid phone numbers")
    void testInvalidPhone(String phone) {
        assertFalse(ValidationUtil.isValidPhone(phone));
    }

    @Test
    @DisplayName("Should test isNotEmpty helper method")
    void testIsNotEmpty() {
        assertTrue(ValidationUtil.isNotEmpty("Cinema"));
        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty("   "));
        assertFalse(ValidationUtil.isNotEmpty(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Pass@123", "Strong#Pass2026", "Admin$99!", "Cinema@123"})
    @DisplayName("Should validate strong passwords")
    void testValidPassword(String pwd) {
        assertTrue(ValidationUtil.isValidPassword(pwd));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "abcdefgh", "ABCDEFGH", "Abcdefgh", "Abcdefg1", "Pass@1", "", "   "})
    @DisplayName("Should reject weak passwords not meeting requirements")
    void testInvalidPassword(String pwd) {
        assertFalse(ValidationUtil.isValidPassword(pwd));
    }

    private Seat createSeat(long id, String row, int number, String code, long typeId, String typeCode) {
        Seat s = new Seat();
        s.setId(id);
        s.setSeatRow(row);
        s.setSeatNumber(number);
        s.setSeatCode(code);
        s.setSeatTypeId(typeId);
        SeatType st = new SeatType();
        st.setId(typeId);
        st.setTypeCode(typeCode);
        s.setSeatType(st);
        return s;
    }

    @Test
    @DisplayName("Should accept valid couple seat pairs adjacent in same row")
    void testValidCoupleSeatsAdjacent() {
        List<Seat> seats = List.of(
            createSeat(1L, "I", 1, "I01", 3L, "COUPLE"),
            createSeat(2L, "I", 2, "I02", 3L, "COUPLE")
        );
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(seats));
    }

    @Test
    @DisplayName("Should accept multiple valid couple pairs")
    void testMultipleValidCouplePairs() {
        List<Seat> seats = List.of(
            createSeat(1L, "I", 1, "I01", 3L, "COUPLE"),
            createSeat(2L, "I", 2, "I02", 3L, "COUPLE"),
            createSeat(5L, "J", 5, "J05", 3L, "COUPLE"),
            createSeat(6L, "J", 6, "J06", 3L, "COUPLE")
        );
        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(seats));
    }

    @Test
    @DisplayName("Should reject odd number of couple seats (1 or 3)")
    void testRejectOddCoupleSeats() {
        List<Seat> oneSeat = List.of(createSeat(1L, "I", 1, "I01", 3L, "COUPLE"));
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateCoupleSeats(oneSeat));
        assertNotNull(ex1.getMessage());
        assertTrue(ex1.getMessage().contains("Sweetbox"));

        List<Seat> threeSeats = List.of(
            createSeat(1L, "I", 1, "I01", 3L, "COUPLE"),
            createSeat(2L, "I", 2, "I02", 3L, "COUPLE"),
            createSeat(3L, "I", 3, "I03", 3L, "COUPLE")
        );
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateCoupleSeats(threeSeats));
        assertNotNull(ex3.getMessage());
        assertTrue(ex3.getMessage().contains("Sweetbox"));
    }

    @Test
    @DisplayName("Should reject couple seats separated by a gap in the same row")
    void testRejectSeparatedCoupleSeatsInSameRow() {
        List<Seat> seats = List.of(
            createSeat(1L, "I", 1, "I01", 3L, "COUPLE"),
            createSeat(3L, "I", 3, "I03", 3L, "COUPLE")
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateCoupleSeats(seats));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Sweetbox"));
    }

    @Test
    @DisplayName("Should reject couple seats in different rows")
    void testRejectCoupleSeatsInDifferentRows() {
        List<Seat> seats = List.of(
            createSeat(1L, "I", 1, "I01", 3L, "COUPLE"),
            createSeat(2L, "J", 1, "J01", 3L, "COUPLE")
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateCoupleSeats(seats));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Sweetbox"));
    }
}
