package com.cinema.modules.identity.job;

import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.model.User;
import com.cinema.modules.booking.dto.ShowtimeReminderDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import com.cinema.modules.identity.dao.NotificationDAO;
import com.cinema.modules.identity.service.EmailService;
import com.cinema.modules.identity.service.UserService;
import com.cinema.modules.identity.service.impl.EmailServiceImpl;
import com.cinema.modules.identity.service.impl.UserServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Nhắc suất chiếu mỗi 5 phút. Không gửi mail khi suất đã hủy.
 */
public class ShowtimeReminderListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ShowtimeReminderListener.class.getName());
    private static final String TYPE = "SHOWTIME";
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private ScheduledExecutorService scheduler;
    private final BookingEngineService bookingEngineService = new BookingEngineServiceImpl();
    private final UserService userService = new UserServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final SystemSettingDAO systemSettingDAO = new SystemSettingDAO();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ensureReminderSetting();
        scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "showtime-reminder");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::remind, 1, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::expireVnpayBookings, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void remind() {
        try {
            int withinMinutes = systemSettingDAO.getIntSetting("SHOWTIME_REMINDER_MINUTES", 120);
            for (ShowtimeReminderDTO item : bookingEngineService.findBookingsNeedingReminder(withinMinutes)) {
                remindOne(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Job nhắc suất chiếu lỗi", e);
        }
    }

    private void expireVnpayBookings() {
        try {
            bookingEngineService.expirePendingVnpayBookings();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Job giải phóng booking VNPay quá hạn lỗi", e);
        }
    }

    private void remindOne(ShowtimeReminderDTO item) {
        if (item.getBookingId() == null || item.getUserId() == null) return;
        String referenceId = String.valueOf(item.getBookingId());
        if (notificationDAO.exists(TYPE, referenceId)) return;
        User user = userService.getUserById(item.getUserId());
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
        String when = item.getStartTime() == null ? "Đang cập nhật" : item.getStartTime().format(CLOCK);
        String place = item.getCinemaName() == null ? "" : item.getCinemaName();
        if (item.getRoomName() != null) {
            place = place.isBlank() ? item.getRoomName() : place + " · " + item.getRoomName();
        }
        String movie = item.getMovieTitle() == null ? "Phim" : item.getMovieTitle();
        emailService.sendShowtimeReminderEmail(user.getEmail(), user.getFullName(), movie, when, place);
        notificationDAO.insertIfAbsent(item.getUserId(), TYPE, referenceId, "Sắp đến giờ chiếu",
                movie + " lúc " + when + " tại " + place);
    }

    private void ensureReminderSetting() {
        if (systemSettingDAO.getSettingValue("SHOWTIME_REMINDER_MINUTES", null) != null) return;
        systemSettingDAO.upsertSetting("SHOWTIME_REMINDER_MINUTES", "120",
                "Số phút nhắc trước giờ chiếu", "OPERATIONAL", null);
    }
}
