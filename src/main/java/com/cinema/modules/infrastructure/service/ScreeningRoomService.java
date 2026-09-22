package com.cinema.modules.infrastructure.service;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public Service Interface do TV 1 cung cấp cho các module khác.
 * Bất kỳ module nào (TV 3, TV 4) cần dữ liệu phòng/rạp đều BẮT BUỘC gọi qua Interface này.
 */
public interface ScreeningRoomService {
    List<Cinema> getAllCinemas();
    Cinema getCinemaById(Long cinemaId);
    List<ScreeningRoom> getRoomsByCinema(Long cinemaId);
    ScreeningRoom getRoomById(Long roomId);
    boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
    List<Seat> getRoomSeatMatrix(Long roomId);
}
