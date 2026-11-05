package com.cinema.modules.operation.dto;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosCounterPageData {
    private LocalDate today;
    private LocalDate selectedDate;
    private Long cinemaId;
    private Long selectedShowtimeId;
    private List<Cinema> cinemas;
    private List<Showtime> showtimes;
    private List<FnBItem> fnbItems;
    private Showtime selectedShowtime;
    private List<Seat> seatMap;
    private Map<Long, BigDecimal> seatPrices;
    private long availableSeatCount;
    private int seatColumnCount;
    private BigDecimal studentDiscount;
    private BigDecimal seniorDiscount;
    private String setupMessage;
    private String errorMessage;
}
