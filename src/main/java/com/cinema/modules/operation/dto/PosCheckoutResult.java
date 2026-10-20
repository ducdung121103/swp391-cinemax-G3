package com.cinema.modules.operation.dto;

import com.cinema.model.Showtime;
import com.cinema.modules.booking.dto.BookingResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PosCheckoutResult {
    private final Long cinemaId;
    private final LocalDate date;
    private final Showtime showtime;
    private final BookingResult bookingResult;
}
