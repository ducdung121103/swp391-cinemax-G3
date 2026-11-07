package com.cinema.modules.booking.dto;

import com.cinema.model.Cinema;
import com.cinema.model.FnBItem;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPageData {
    private Showtime showtime;
    private String showtimeText;
    private Cinema currentCinema;
    private List<Seat> seatMap;
    private Map<Long, BigDecimal> seatPrices;
    private int seatColumnCount;
    private long availableSeatCount;
    private List<FnBItem> fnbItems;
    private int studentDiscount;
    private int seniorDiscount;
    private String errorMessage;
}

