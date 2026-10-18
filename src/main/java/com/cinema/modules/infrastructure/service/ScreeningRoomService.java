package com.cinema.modules.infrastructure.service;

import com.cinema.model.Cinema;
import com.cinema.model.MaintenanceSchedule;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Public Service Interface do TV 1 (Infrastructure) cung cấp cho toàn bộ hệ thống.
 * BẢO TOÀN 100% 7 HÀM HỢP ĐỒNG GỐC để đảm bảo code của TV 3, TV 4 không bị ảnh hưởng!
 */
public interface ScreeningRoomService {
    // =========================================================================
    // 7 PHƯƠNG THỨC HỢP ĐỒNG CŨ (BẤT KHẢ XÂM PHẠM)
    // =========================================================================
    List<Cinema> getAllCinemas();
    Cinema getCinemaById(Long cinemaId);
    List<ScreeningRoom> getRoomsByCinema(Long cinemaId);
    ScreeningRoom getRoomById(Long roomId);
    boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
    List<Seat> getRoomSeatMatrix(Long roomId);
    Seat getSeatById(Long seatId);

    // =========================================================================
    // CÁC PHƯƠNG THỨC MỚI PHỤC VỤ ITERATION 1 CỦA DŨNG
    // =========================================================================

    // --- 1. Branch Management (CRUD Cụm rạp) ---
    List<Cinema> getAllCinemasForAdmin(String keyword, String city, Boolean isActive);
    List<String> getAllCities();
    Long createCinema(Cinema cinema) throws Exception;
    boolean updateCinema(Cinema cinema) throws Exception;
    boolean toggleCinemaStatus(Long id, boolean isActive);
    boolean deleteCinema(Long id);
    int getTotalCinemasCount();

    // --- 2. Screening Room Management (CRUD Phòng chiếu) ---
    List<ScreeningRoom> getAllRoomsWithCinema(Long cinemaId, String roomType, String status);
    Long createRoom(ScreeningRoom room);
    boolean updateRoom(ScreeningRoom room);
    boolean updateRoomStatus(Long roomId, String status);
    boolean deleteRoom(Long roomId);
    int getTotalRoomsCount();
    int getTotalSeatsCount();

    // --- 3. Seat Layout Designer (Sơ đồ ghế & Loại ghế) ---
    List<SeatType> getAllSeatTypes();
    boolean updateSeatTypeSurcharge(Long seatTypeId, BigDecimal surcharge, String colorHex);
    boolean regenerateRoomSeatMatrix(Long roomId, int totalRows, int totalColumns, int stdRows, int vipRows);
    boolean updateBatchSeats(Long roomId, List<Long> seatIds, Long seatTypeId, String status, Boolean isActive);
    boolean updateRowSeatType(Long roomId, String seatRow, Long seatTypeId);

    // =========================================================================
    // CÁC PHƯƠNG THỨC MỚI PHỤC VỤ ITERATION 2 (SEAT MAINTENANCE & SCHEDULES)
    // =========================================================================
    boolean toggleSeatsMaintenance(Long roomId, List<Long> seatIds, boolean underMaintenance);
    boolean restoreAllMaintenanceSeatsInRoom(Long roomId);
    List<MaintenanceSchedule> getMaintenanceSchedulesByRoom(Long roomId);
    Long createMaintenanceSchedule(MaintenanceSchedule schedule, List<Long> seatIds);
    boolean resolveMaintenanceSchedule(Long scheduleId, Long roomId, boolean restoreSeats);
}
