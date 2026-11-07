package com.cinema.modules.booking.dto;

import com.cinema.model.User;
import jakarta.servlet.http.HttpServletRequest;
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
public class BookingCheckoutCommand {
    private User user;
    private String showtimeId;
    private List<Long> seatIds;
    private String ticketClass;
    private Map<Long, Integer> fnbQuantities;
    private String voucherId;
    private String paymentMethod;
    private String sessionId;
    private HttpServletRequest request;
}

