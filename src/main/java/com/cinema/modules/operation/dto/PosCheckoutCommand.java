package com.cinema.modules.operation.dto;

import com.cinema.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosCheckoutCommand {
    private User staff;
    private String requestedCinemaId;
    private String date;
    private String showtimeId;
    private String checkoutConfirmed;
    private String[] seatIds;
    private String ticketClass;
    private String paymentMethod;
    private String sessionId;
    private Map<String, String> fnbQuantities;
}
