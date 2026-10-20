package com.cinema.modules.operation.service;

import com.cinema.model.User;
import com.cinema.modules.operation.dto.PosCheckoutCommand;
import com.cinema.modules.operation.dto.PosCheckoutResult;
import com.cinema.modules.operation.dto.PosCounterPageData;

import java.sql.SQLException;

public interface PosCounterService {
    PosCounterPageData loadPage(User staff, String requestedCinemaId, String requestedDate,
                                String requestedShowtimeId, String sessionId) throws SQLException;

    PosCheckoutResult checkout(PosCheckoutCommand command);
}
