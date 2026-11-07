package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingQuoteCommand {
    private String showtimeId;
    private List<Long> seatIds;
    private String ticketClass;
    private Map<Long, Integer> fnbQuantities;
}

