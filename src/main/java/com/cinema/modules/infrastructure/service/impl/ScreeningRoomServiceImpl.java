package com.cinema.modules.infrastructure.service.impl;

import com.cinema.common.util.ValidationUtil;
import com.cinema.common.constant.RoleConstant;
import com.cinema.model.Cinema;
import com.cinema.model.User;
import com.cinema.model.MaintenanceSchedule;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.infrastructure.dao.ScreeningRoomDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    public boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime,
            Long excludeShowtimeId) {
        return screeningRoomDAO.isAvailable(roomId, startTime, endTime, excludeShowtimeId);
    }

    @Override
    public List<Seat> getRoomSeatMatrix(Long roomId) {
        return screeningRoomDAO.findSeatsByRoom(roomId);
    }

    @Override
    public Seat getSeatById(Long seatId) {
        return screeningRoomDAO.findSeatById(seatId);
    }

    @Override
    public List<Seat> getSeatsByIds(List<Long> seatIds) {
        return screeningRoomDAO.findSeatsByIds(seatIds);
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
    public List<Cinema> getAllCinemasForAdmin(String keyword, String city, Boolean isActive, int page, int pageSize) {
        return cinemaDAO.findAllForAdmin(keyword, city, isActive, page, pageSize);
    }

    @Override
    public int countCinemasForAdmin(String keyword, String city, Boolean isActive) {
        return cinemaDAO.countForAdmin(keyword, city, isActive);
    }

    @Override
    public List<String> getAllCities() {
        return cinemaDAO.findAllCities();
    }

    @Override
    public Long createCinema(Cinema cinema) throws Exception {
        if (cinema == null) {
            throw new IllegalArgumentException("Dữ liệu cụm rạp không hợp lệ!");
        }
        // 1. Mã cụm rạp
        if (cinema.getCinemaCode() == null || cinema.getCinemaCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã cụm rạp không được để trống!");
        }
        String code = cinema.getCinemaCode().trim().toUpperCase();
        if (!code.matches("^[A-Za-z0-9\\-_]{3,20}$")) {
            throw new IllegalArgumentException("Mã cụm rạp phải từ 3 đến 20 ký tự chữ và số (VD: C-HN01)!");
        }
        cinema.setCinemaCode(code);

        // 2. Tên cụm rạp: chuẩn hóa viết hoa chữ cái đầu (Title Case)
        if (cinema.getName() == null || cinema.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên cụm rạp không được để trống!");
        }
        cinema.setName(ValidationUtil.capitalizeWords(cinema.getName()));

        // 3. Thành phố: chuẩn hóa viết hoa chữ cái đầu
        if (cinema.getCity() == null || cinema.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập thành phố của cụm rạp!");
        }
        cinema.setCity(ValidationUtil.capitalizeWords(cinema.getCity()));

        // 4. Địa chỉ chi tiết
        if (cinema.getAddress() == null || cinema.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ cụm rạp không được để trống!");
        }
        cinema.setAddress(cinema.getAddress().trim());

        // 5. Số điện thoại (Hotline): kiểm tra số lượng chữ số hợp lệ (8 - 15 số)
        String phoneErr = ValidationUtil.getPhoneValidationError(cinema.getPhone());
        if (phoneErr != null) {
            throw new IllegalArgumentException(phoneErr);
        }
        cinema.setPhone(cinema.getPhone().trim());

        // 6. Email: nếu có nhập thì kiểm tra định dạng
        if (cinema.getEmail() != null && !cinema.getEmail().trim().isEmpty()) {
            if (!ValidationUtil.isValidEmail(cinema.getEmail().trim())) {
                throw new IllegalArgumentException("Địa chỉ email cụm rạp không đúng định dạng!");
            }
            cinema.setEmail(cinema.getEmail().trim().toLowerCase());
        }

        return cinemaDAO.insert(cinema);
    }

    @Override
    public boolean updateCinema(Cinema cinema) throws Exception {
        if (cinema == null || cinema.getId() == null) {
            throw new IllegalArgumentException("ID cụm rạp không hợp lệ!");
        }
        // 1. Mã cụm rạp
        if (cinema.getCinemaCode() == null || cinema.getCinemaCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã cụm rạp không được để trống!");
        }
        String code = cinema.getCinemaCode().trim().toUpperCase();
        if (!code.matches("^[A-Za-z0-9\\-_]{3,20}$")) {
            throw new IllegalArgumentException("Mã cụm rạp phải từ 3 đến 20 ký tự chữ và số (VD: C-HN01)!");
        }
        cinema.setCinemaCode(code);

        // 2. Tên cụm rạp
        if (cinema.getName() == null || cinema.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên cụm rạp không được để trống!");
        }
        cinema.setName(ValidationUtil.capitalizeWords(cinema.getName()));

        // 3. Thành phố
        if (cinema.getCity() == null || cinema.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập thành phố của cụm rạp!");
        }
        cinema.setCity(ValidationUtil.capitalizeWords(cinema.getCity()));

        // 4. Địa chỉ chi tiết
        if (cinema.getAddress() == null || cinema.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ cụm rạp không được để trống!");
        }
        cinema.setAddress(cinema.getAddress().trim());

        // 5. Số điện thoại
        String phoneErr = ValidationUtil.getPhoneValidationError(cinema.getPhone());
        if (phoneErr != null) {
            throw new IllegalArgumentException(phoneErr);
        }
        cinema.setPhone(cinema.getPhone().trim());

        // 6. Email
        if (cinema.getEmail() != null && !cinema.getEmail().trim().isEmpty()) {
            if (!ValidationUtil.isValidEmail(cinema.getEmail().trim())) {
                throw new IllegalArgumentException("Địa chỉ email cụm rạp không đúng định dạng!");
            }
            cinema.setEmail(cinema.getEmail().trim().toLowerCase());
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
    public String deleteCinema(User actor, Long cinemaId) {
        if (actor == null || actor.getRole() == null
                || !RoleConstant.ADMIN.equalsIgnoreCase(actor.getRole().getRoleName())) {
            return "UNAUTHORIZED";
        }
        if (cinemaId == null)
            return "NOT_FOUND";
        if (cinemaDAO.hasFutureShowtimes(cinemaId)) {
            return "ACTIVE_SHOWTIMES";
        }
        return cinemaDAO.softDelete(cinemaId) ? "OK" : "FAILED";
    }

    @Override
    public int getTotalCinemasCount() {
        return cinemaDAO.countTotalCinemas();
    }

    @Override
    public int getTotalCinemasCount(Long cinemaId) {
        return cinemaDAO.countTotalCinemas(cinemaId);
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
        room.setName(ValidationUtil.capitalizeWords(room.getName()));
        if (room.getName().length() > 100) {
            throw new IllegalArgumentException("Tên phòng chiếu không được vượt quá 100 ký tự!");
        }
        int rows = room.getTotalRows() != null ? room.getTotalRows() : 10;
        int cols = room.getTotalColumns() != null ? room.getTotalColumns() : 10;
        if (rows < 4 || rows > 26) {
            throw new IllegalArgumentException("Số hàng ghế phòng chiếu phải từ 4 đến 26 hàng (A đến Z)!");
        }
        if (cols < 4 || cols > 30) {
            throw new IllegalArgumentException("Số cột ghế phòng chiếu phải từ 4 đến 30 ghế mỗi hàng!");
        }
        room.setTotalRows(rows);
        room.setTotalColumns(cols);

        String type = room.getRoomType();
        if (type == null || (!type.equals("STANDARD_2D") && !type.equals("VIP_2D") && !type.equals("IMAX_3D") && !type.equals("4DX"))) {
            room.setRoomType("STANDARD_2D");
        }
        String status = room.getStatus();
        if (status == null || (!status.equals("ACTIVE") && !status.equals("MAINTENANCE") && !status.equals("INACTIVE"))) {
            room.setStatus("ACTIVE");
        }
        if (cinemaDAO.findById(room.getCinemaId()) == null) {
            throw new IllegalArgumentException("Cụm rạp không tồn tại hoặc đã bị xóa!");
        }
        return screeningRoomDAO.insertRoom(room);
    }

    @Override
    public boolean updateRoom(ScreeningRoom room) {
        if (room == null || room.getId() == null) {
            throw new IllegalArgumentException("ID phòng chiếu không hợp lệ!");
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng chiếu không được để trống!");
        }
        room.setName(ValidationUtil.capitalizeWords(room.getName()));
        if (room.getName().length() > 100) {
            throw new IllegalArgumentException("Tên phòng chiếu không được vượt quá 100 ký tự!");
        }
        String type = room.getRoomType();
        if (type == null || (!type.equals("STANDARD_2D") && !type.equals("VIP_2D") && !type.equals("IMAX_3D") && !type.equals("4DX"))) {
            room.setRoomType("STANDARD_2D");
        }
        String status = room.getStatus();
        if (status == null || (!status.equals("ACTIVE") && !status.equals("MAINTENANCE") && !status.equals("INACTIVE"))) {
            room.setStatus("ACTIVE");
        }
        return screeningRoomDAO.updateRoom(room);
    }

    @Override
    public boolean updateRoomStatus(Long roomId, String status) {
        return screeningRoomDAO.updateRoomStatus(roomId, status);
    }

    @Override
    public Long createRoom(User actor, ScreeningRoom room) {
        assertCanManageCinema(actor, room != null ? room.getCinemaId() : null);
        return createRoom(room);
    }

    @Override
    public boolean updateRoom(User actor, ScreeningRoom room) {
        if (room == null || room.getId() == null) {
            throw new IllegalArgumentException("ID phòng chiếu không hợp lệ!");
        }
        ScreeningRoom existing = screeningRoomDAO.findById(room.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng chiếu cần cập nhật!");
        }
        assertCanManageCinema(actor, existing.getCinemaId());
        return updateRoom(room);
    }

    @Override
    public boolean updateRoomStatus(User actor, Long roomId, String status) {
        ScreeningRoom existing = roomId != null ? screeningRoomDAO.findById(roomId) : null;
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng chiếu cần đổi trạng thái!");
        }
        assertCanManageCinema(actor, existing.getCinemaId());
        return updateRoomStatus(roomId, status);
    }

    /**
     * Branch Guard: Admin toàn quyền; Manager chỉ thao tác phòng thuộc rạp mình.
     */
    private void assertCanManageCinema(User actor, Long cinemaId) {
        if (actor == null || actor.getRole() == null) {
            throw new IllegalArgumentException("Bạn không có quyền thực hiện thao tác này!");
        }
        String role = actor.getRole().getRoleName();
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            return;
        }
        if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            if (cinemaId == null || actor.getCinemaId() == null || !actor.getCinemaId().equals(cinemaId)) {
                throw new IllegalArgumentException("Bạn chỉ được thao tác phòng chiếu thuộc chi nhánh rạp của mình!");
            }
            return;
        }
        throw new IllegalArgumentException("Bạn không có quyền thực hiện thao tác này!");
    }

    @Override
    public boolean deleteRoom(Long roomId) {
        return screeningRoomDAO.softDeleteRoom(roomId);
    }

    @Override
    public String deleteRoom(User actor, Long roomId) {
        if (actor == null || actor.getRole() == null) {
            return "UNAUTHORIZED";
        }
        if (roomId == null)
            return "NOT_FOUND";
        ScreeningRoom room = screeningRoomDAO.findById(roomId);
        if (room == null)
            return "NOT_FOUND";

        String role = actor.getRole().getRoleName();
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            // Admin có quyền xóa mọi rạp
        } else if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            // Manager chỉ được xóa phòng thuộc chi nhánh rạp của mình
            if (actor.getCinemaId() == null || !actor.getCinemaId().equals(room.getCinemaId())) {
                return "FORBIDDEN_BRANCH";
            }
        } else {
            return "UNAUTHORIZED";
        }

        if (screeningRoomDAO.hasFutureShowtimes(roomId)) {
            return "ACTIVE_SHOWTIMES";
        }
        return screeningRoomDAO.softDeleteRoom(roomId) ? "OK" : "FAILED";
    }

    @Override
    public int getTotalRoomsCount() {
        return screeningRoomDAO.countTotalRooms();
    }

    @Override
    public int getTotalRoomsCount(Long cinemaId) {
        return screeningRoomDAO.countTotalRooms(cinemaId);
    }

    @Override
    public int getTotalSeatsCount() {
        return screeningRoomDAO.countTotalSeats();
    }

    @Override
    public int getTotalSeatsCount(Long cinemaId) {
        return screeningRoomDAO.countTotalSeats(cinemaId);
    }

    // --- 3. Seat Layout Designer ---
    @Override
    public List<SeatType> getAllSeatTypes() {
        return screeningRoomDAO.findAllSeatTypes();
    }

    @Override
    public boolean updateSeatTypeSurcharge(Long seatTypeId, BigDecimal surcharge, String colorHex) {
        if (seatTypeId == null) {
            throw new IllegalArgumentException("Loại ghế không hợp lệ!");
        }
        if (surcharge != null && surcharge.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phụ thu loại ghế không được là số âm!");
        }
        if (colorHex != null && !colorHex.trim().isEmpty() && !colorHex.trim().matches("^#[0-9A-Fa-f]{6}$")) {
            colorHex = "#e50914"; // fallback màu chuẩn nếu nhập sai
        }
        return screeningRoomDAO.updateSeatTypeSurcharge(seatTypeId, surcharge, colorHex);
    }

    @Override
    public boolean regenerateRoomSeatMatrix(Long roomId, int totalRows, int totalColumns, int stdRows, int vipRows) {
        if (roomId == null) {
            throw new IllegalArgumentException("ID phòng chiếu không hợp lệ!");
        }
        if (totalRows < 4 || totalRows > 26 || totalColumns < 4 || totalColumns > 30) {
            throw new IllegalArgumentException("Kích thước ma trận không hợp lệ (Hàng từ 4 đến 26, Cột từ 4 đến 30)!");
        }
        if (stdRows < 0 || vipRows < 0) {
            throw new IllegalArgumentException("Số hàng Standard và VIP không được là số âm!");
        }
        if ((stdRows + vipRows) > totalRows) {
            throw new IllegalArgumentException("Tổng số hàng Standard (" + stdRows + ") và VIP (" + vipRows 
                    + ") không được vượt quá tổng số hàng của phòng chiếu (" + totalRows + ")!");
        }
        if (screeningRoomDAO.hasTicketsForRoom(roomId)) {
            throw new IllegalArgumentException("Phòng chiếu đã phát sinh vé, không thể tái tạo ma trận ghế. Vui lòng xử lý vé trước!");
        }
        return screeningRoomDAO.regenerateRoomSeatMatrix(roomId, totalRows, totalColumns, stdRows, vipRows);
    }

    @Override
    public boolean updateBatchSeats(Long roomId, List<Long> seatIds, Long seatTypeId, String status, Boolean isActive) {
        if (seatIds == null || seatIds.isEmpty()) {
            return false;
        }

        // 1. Kiểm tra nghiệp vụ ghế đôi Sweetbox
        if (seatTypeId != null && seatTypeId == 3L) {
            // Trường hợp 1: Chuyển các ghế được chọn thành COUPLE (Sweetbox)
            if (seatIds.size() % 2 != 0) {
                throw new IllegalArgumentException(
                        "Ghế đôi Sweetbox bắt buộc phải chọn theo số lượng chẵn (2, 4, 6... ghế; bạn đang chọn "
                                + seatIds.size() + " ghế, không hợp lệ)!");
            }

            List<Seat> targetSeats = screeningRoomDAO.findSeatsByIds(seatIds);
            if (targetSeats != null && !targetSeats.isEmpty()) {
                for (Seat s : targetSeats) {
                    s.setSeatTypeId(3L);
                    SeatType st = new SeatType();
                    st.setId(3L);
                    st.setTypeCode("COUPLE");
                    st.setName("Ghế Đôi Sweetbox");
                    s.setSeatType(st);
                }
                ValidationUtil.validateCoupleSeats(targetSeats);
            }
        }

        // Trường hợp 2: Kiểm tra toàn phòng sau khi áp dụng để đảm bảo không làm
        // rách/lẻ các cặp ghế đôi hiện có
        if (roomId != null) {
            List<Seat> allSeats = screeningRoomDAO.findSeatsByRoom(roomId);
            if (allSeats != null && !allSeats.isEmpty()) {
                Set<Long> targetIdSet = new HashSet<>(seatIds);
                List<Seat> simulatedCoupleSeats = new ArrayList<>();

                for (Seat s : allSeats) {
                    Long currentTypeId = targetIdSet.contains(s.getId()) && seatTypeId != null
                            ? seatTypeId
                            : s.getSeatTypeId();
                    if (currentTypeId != null && currentTypeId == 3L) {
                        Seat copy = new Seat();
                        copy.setId(s.getId());
                        copy.setSeatRow(s.getSeatRow());
                        copy.setSeatNumber(s.getSeatNumber());
                        copy.setSeatCode(s.getSeatCode());
                        copy.setGridRowIndex(s.getGridRowIndex());
                        copy.setGridColIndex(s.getGridColIndex());
                        copy.setSeatTypeId(3L);
                        SeatType st = new SeatType();
                        st.setId(3L);
                        st.setTypeCode("COUPLE");
                        st.setName("Ghế Đôi Sweetbox");
                        copy.setSeatType(st);
                        simulatedCoupleSeats.add(copy);
                    }
                }

                if (!simulatedCoupleSeats.isEmpty()) {
                    try {
                        ValidationUtil.validateCoupleSeats(simulatedCoupleSeats);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Vi phạm quy tắc sơ đồ ghế đôi: " + e.getMessage());
                    }
                }
            }
        }

        return screeningRoomDAO.updateBatchSeats(roomId, seatIds, seatTypeId, status, isActive);
    }

    @Override
    public boolean updateRowSeatType(Long roomId, String seatRow, Long seatTypeId) {
        if (roomId == null || seatRow == null || seatRow.trim().isEmpty()) {
            return false;
        }

        if (seatTypeId != null && seatTypeId == 3L) {
            List<Seat> allSeats = screeningRoomDAO.findSeatsByRoom(roomId);
            if (allSeats != null) {
                List<Seat> rowSeats = allSeats.stream()
                        .filter(s -> seatRow.trim().equalsIgnoreCase(s.getSeatRow()))
                        .collect(Collectors.toList());
                if (rowSeats.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy hàng ghế " + seatRow + " trong phòng chiếu này!");
                }
                if (rowSeats.size() % 2 != 0) {
                    throw new IllegalArgumentException(
                            "Không thể chuyển toàn bộ hàng " + seatRow + " thành ghế đôi Sweetbox vì hàng này có "
                                    + rowSeats.size() + " ghế (số lẻ), không thể chia đều thành các cặp đôi!");
                }
                for (Seat s : rowSeats) {
                    s.setSeatTypeId(3L);
                    SeatType st = new SeatType();
                    st.setId(3L);
                    st.setTypeCode("COUPLE");
                    st.setName("Ghế Đôi Sweetbox");
                    s.setSeatType(st);
                }
                ValidationUtil.validateCoupleSeats(rowSeats);
            }
        }

        return screeningRoomDAO.updateRowSeatType(roomId, seatRow, seatTypeId);
    }

    // =========================================================================
    // ITERATION 2: SEAT MAINTENANCE & MAINTENANCE SCHEDULES
    // =========================================================================
    /**
     * Helper tạo bản sao ghế mô phỏng sang loại COUPLE (Sweetbox) phục vụ kiểm định
     * nghiệp vụ.
     */
    private Seat createSimulatedCoupleSeat(Seat source) {
        Seat copy = new Seat();
        copy.setId(source.getId());
        copy.setSeatRow(source.getSeatRow());
        copy.setSeatNumber(source.getSeatNumber());
        copy.setSeatCode(source.getSeatCode());
        copy.setGridRowIndex(source.getGridRowIndex());
        copy.setGridColIndex(source.getGridColIndex());
        copy.setSeatTypeId(3L);
        SeatType st = new SeatType();
        st.setId(3L);
        st.setTypeCode("COUPLE");
        st.setName("Ghế Đôi Sweetbox");
        copy.setSeatType(st);
        return copy;
    }

    @Override
    public boolean toggleSeatsMaintenance(Long roomId, List<Long> seatIds, boolean underMaintenance) {
        return screeningRoomDAO.toggleSeatsMaintenance(roomId, seatIds, underMaintenance);
    }

    @Override
    public boolean restoreAllMaintenanceSeatsInRoom(Long roomId) {
        return screeningRoomDAO.restoreAllMaintenanceSeatsInRoom(roomId);
    }

    @Override
    public boolean updateSeatTypeSurcharge(User actor, Long seatTypeId, BigDecimal surcharge, String colorHex) {
        assertAdmin(actor);
        return updateSeatTypeSurcharge(seatTypeId, surcharge, colorHex);
    }

    @Override
    public boolean regenerateRoomSeatMatrix(User actor, Long roomId, int totalRows, int totalColumns, int stdRows, int vipRows) {
        assertAdmin(actor);
        return regenerateRoomSeatMatrix(roomId, totalRows, totalColumns, stdRows, vipRows);
    }

    @Override
    public boolean updateBatchSeats(User actor, Long roomId, List<Long> seatIds, Long seatTypeId, String status, Boolean isActive) {
        assertAdmin(actor);
        return updateBatchSeats(roomId, seatIds, seatTypeId, status, isActive);
    }

    @Override
    public boolean updateRowSeatType(User actor, Long roomId, String seatRow, Long seatTypeId) {
        assertAdmin(actor);
        return updateRowSeatType(roomId, seatRow, seatTypeId);
    }

    @Override
    public boolean toggleSeatsMaintenance(User actor, Long roomId, List<Long> seatIds, boolean underMaintenance) {
        assertAdmin(actor);
        return toggleSeatsMaintenance(roomId, seatIds, underMaintenance);
    }

    @Override
    public boolean restoreAllMaintenanceSeatsInRoom(User actor, Long roomId) {
        assertAdmin(actor);
        return restoreAllMaintenanceSeatsInRoom(roomId);
    }

    private void assertAdmin(User actor) {
        if (actor == null || actor.getRole() == null
                || !RoleConstant.ADMIN.equalsIgnoreCase(actor.getRole().getRoleName())) {
            throw new IllegalArgumentException("Chỉ Quản trị viên (Admin) mới có quyền chỉnh sửa sơ đồ ghế!");
        }
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
