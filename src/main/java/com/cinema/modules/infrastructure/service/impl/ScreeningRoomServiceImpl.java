package com.cinema.modules.infrastructure.service.impl;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.infrastructure.dao.ScreeningRoomDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cài đặt nghiệp vụ Hạ tầng cụm rạp và phòng chiếu (TV 1).
 */
public class ScreeningRoomServiceImpl implements ScreeningRoomService {
    private final CinemaDAO cinemaDAO = new CinemaDAO();
    private final ScreeningRoomDAO screeningRoomDAO = new ScreeningRoomDAO();

    @Override
    public List<Cinema> getAllCinemas() {
        return cinemaDAO.findAll();
    }

    @Override
    public Cinema getCinemaById(Long cinemaId) {
        return cinemaDAO.findById(cinemaId);
    }

    @Override
    public List<ScreeningRoom> getRoomsByCinema(Long cinemaId) {
        return screeningRoomDAO.findByCinema(cinemaId);
    }

    @Override
    public ScreeningRoom getRoomById(Long roomId) {
        return screeningRoomDAO.findById(roomId);
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return screeningRoomDAO.isAvailable(roomId, startTime, endTime);
    }

    @Override
    public List<Seat> getRoomSeatMatrix(Long roomId) {
        return screeningRoomDAO.findSeatsByRoom(roomId);
    }

    @Override
    public Seat getSeatById(Long seatId) {
        return screeningRoomDAO.findSeatById(seatId);
    }
}
