package com.cinema.modules.infrastructure.service.impl;

import com.cinema.model.Cinema;
import com.cinema.model.MaintenanceSchedule;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.infrastructure.dao.ScreeningRoomDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cài đặt nghiệp vụ Hạ tầng cụm rạp, phòng chiếu và sơ đồ ghế (TV 1 - Dũng).
 */
public class ScreeningRoomServiceImpl implements ScreeningRoomService {
    private final CinemaDAO cinemaDAO;
    private final ScreeningRoomDAO screeningRoomDAO;

    public ScreeningRoomServiceImpl() {
        this.cinemaDAO = new CinemaDAO();
        this.screeningRoomDAO = new ScreeningRoomDAO();
    }

    public ScreeningRoomServiceImpl(CinemaDAO cinemaDAO, ScreeningRoomDAO screeningRoomDAO) {
        this.cinemaDAO = cinemaDAO;
        this.screeningRoomDAO = screeningRoomDAO;
    }

    // =========================================================================
    // 7 PHƯƠNG THỨC HỢP ĐỒNG GỐC (BẤT KHẢ XÂM PHẠM)
    // =========================================================================

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

    // =========================================================================
    // TRIỂN KHAI CÁC PHƯƠNG THỨC MỚI PHỤC VỤ ITERATION 1
    // =========================================================================

    // --- 1. Branch Management ---
    @Override
    public List<Cinema> getAllCinemasForAdmin(String keyword, String city, Boolean isActive) {
        return cinemaDAO.findAllForAdmin(keyword, city, isActive);
    }

    @Override
    public List<String> getAllCities() {
        return cinemaDAO.findAllCities();
    }

    @Override
    public Long createCinema(Cinema cinema) throws Exception {
        if (cinema == null || cinema.getCinemaCode() == null || cinema.getCinemaCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã cụm rạp không được để trống!");
        }
        if (cinema.getName() == null || cinema.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên cụm rạp không được để trống!");
        }
        return cinemaDAO.insert(cinema);
    }

    @Override
    public boolean updateCinema(Cinema cinema) throws Exception {
        if (cinema == null || cinema.getId() == null) {
            throw new IllegalArgumentException("ID cụm rạp không hợp lệ!");
        }
        return cinemaDAO.update(cinema);
    }

    @Override
    public boolean toggleCinemaStatus(Long id, boolean isActive) {
        return cinemaDAO.toggleActiveStatus(id, isActive);
    }

    @Override
    public boolean deleteCinema(Long id) {
        return cinemaDAO.softDelete(id);
    }

    @Override
    public int getTotalCinemasCount() {
        return cinemaDAO.countTotalCinemas();
    }

    // --- 2. Screening Room Management ---
    @Override
    public List<ScreeningRoom> getAllRoomsWithCinema(Long cinemaId, String roomType, String status) {
        return screeningRoomDAO.findAllRoomsWithCinema(cinemaId, roomType, status);
    }

    @Override
    public Long createRoom(ScreeningRoom room) {
        if (room == null || room.getCinemaId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn cụm rạp chi nhánh!");
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng chiếu không được để trống!");
        }
        return screeningRoomDAO.insertRoom(room);
    }

    @Override
    public boolean updateRoom(ScreeningRoom room) {
        if (room == null || room.getId() == null) {
            throw new IllegalArgumentException("ID phòng chiếu không hợp lệ!");
        }
        return screeningRoomDAO.updateRoom(room);
    }

    @Override
    public boolean updateRoomStatus(Long roomId, String status) {
        return screeningRoomDAO.updateRoomStatus(roomId, status);
    }

    @Override
    public boolean deleteRoom(Long roomId) {
        return screeningRoomDAO.softDeleteRoom(roomId);
    }

    @Override
    public int getTotalRoomsCount() {
        return screeningRoomDAO.countTotalRooms();
    }

    @Override
    public int getTotalSeatsCount() {
        return screeningRoomDAO.countTotalSeats();
    }

    // --- 3. Seat Layout Designer ---
    @Override
    public List<SeatType> getAllSeatTypes() {
        return screeningRoomDAO.findAllSeatTypes();
    }

    @Override
    public boolean updateSeatTypeSurcharge(Long seatTypeId, BigDecimal surcharge, String colorHex) {
        return screeningRoomDAO.updateSeatTypeSurcharge(seatTypeId, surcharge, colorHex);
    }

    @Override
    public boolean regenerateRoomSeatMatrix(Long roomId, int totalRows, int totalColumns, int stdRows, int vipRows) {
        if (totalRows <= 0 || totalRows > 26 || totalColumns <= 0 || totalColumns > 30) {
            throw new IllegalArgumentException("Kích thước ma trận không hợp lệ (Hàng từ 1 đến 26, Cột từ 1 đến 30)!");
        }
        return screeningRoomDAO.regenerateRoomSeatMatrix(roomId, totalRows, totalColumns, stdRows, vipRows);
    }

    @Override
    public boolean updateBatchSeats(Long roomId, List<Long> seatIds, Long seatTypeId, String status, Boolean isActive) {
        return screeningRoomDAO.updateBatchSeats(roomId, seatIds, seatTypeId, status, isActive);
    }

    @Override
    public boolean updateRowSeatType(Long roomId, String seatRow, Long seatTypeId) {
        return screeningRoomDAO.updateRowSeatType(roomId, seatRow, seatTypeId);
    }

    // =========================================================================
    // ITERATION 2: SEAT MAINTENANCE & MAINTENANCE SCHEDULES
    // =========================================================================

    @Override
    public boolean toggleSeatsMaintenance(Long roomId, List<Long> seatIds, boolean underMaintenance) {
        return screeningRoomDAO.toggleSeatsMaintenance(roomId, seatIds, underMaintenance);
    }

    @Override
    public boolean restoreAllMaintenanceSeatsInRoom(Long roomId) {
        return screeningRoomDAO.restoreAllMaintenanceSeatsInRoom(roomId);
    }

    @Override
    public List<MaintenanceSchedule> getMaintenanceSchedulesByRoom(Long roomId) {
        return screeningRoomDAO.findMaintenanceSchedulesByRoom(roomId);
    }

    @Override
    public Long createMaintenanceSchedule(MaintenanceSchedule schedule, List<Long> seatIds) {
        return screeningRoomDAO.createMaintenanceSchedule(schedule, seatIds);
    }

    @Override
    public boolean resolveMaintenanceSchedule(Long scheduleId, Long roomId, boolean restoreSeats) {
        return screeningRoomDAO.resolveMaintenanceSchedule(scheduleId, roomId, restoreSeats);
    }
}
