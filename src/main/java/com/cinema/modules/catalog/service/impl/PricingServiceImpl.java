package com.cinema.modules.catalog.service.impl;

import com.cinema.common.constant.RoleConstant;
import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.model.TicketPricing;
import com.cinema.model.User;
import com.cinema.modules.catalog.dao.TicketPricingDAO;
import com.cinema.modules.catalog.service.PricingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Cài đặt nghiệp vụ Tính giá vé (TV 3).
 */
public class PricingServiceImpl implements PricingService {
    private static final Set<String> DAY_TYPES = Set.of("WEEKDAY", "WEEKEND", "HOLIDAY");
    private static final Set<String> TIME_SLOTS = Set.of("EARLY", "STANDARD", "PRIME");
    private static final Set<String> FORMATS = Set.of("2D", "3D", "IMAX");

    private final TicketPricingDAO pricingDAO;
    private final SystemSettingDAO systemSettingDAO;

    public PricingServiceImpl() {
        this(new TicketPricingDAO(), new SystemSettingDAO());
    }

    public PricingServiceImpl(TicketPricingDAO pricingDAO) {
        this(pricingDAO, new SystemSettingDAO());
    }

    public PricingServiceImpl(TicketPricingDAO pricingDAO, SystemSettingDAO systemSettingDAO) {
        this.pricingDAO = pricingDAO;
        this.systemSettingDAO = systemSettingDAO;
    }

    @Override
    public BigDecimal calculateTicketPrice(Long showtimeId, Long seatTypeId, LocalDateTime showTime, String format) {
        BigDecimal basePrice = pricingDAO.findBasePrice(showTime, format);
        if (seatTypeId != null) {
            BigDecimal surcharge = pricingDAO.findSeatTypeSurcharge(seatTypeId);
            if (surcharge != null && surcharge.compareTo(BigDecimal.ZERO) > 0) {
                basePrice = basePrice.add(surcharge);
            }
        }
        return basePrice;
    }

    @Override
    public List<TicketPricing> listPrices() {
        return pricingDAO.findAllActive();
    }

    @Override
    public String savePrice(TicketPricing pricing, User actor) {
        if (pricing == null || actor == null || actor.getRole() == null) return "INVALID";
        String role = actor.getRole().getRoleName();
        boolean admin = RoleConstant.ADMIN.equalsIgnoreCase(role);
        boolean manager = RoleConstant.MANAGER.equalsIgnoreCase(role);
        if (!admin && !manager) return "FORBIDDEN";
        if (manager) {
            if (actor.getCinemaId() == null) return "FORBIDDEN";
            pricing.setCinemaId(actor.getCinemaId());
        }
        if (!DAY_TYPES.contains(pricing.getDayType()) || !TIME_SLOTS.contains(pricing.getTimeSlot()) || !FORMATS.contains(pricing.getExperienceFormat())) {
            return "INVALID";
        }
        if (pricing.getBasePrice() == null || pricing.getBasePrice().signum() <= 0 || pricing.getEffectiveFrom() == null) {
            return "INVALID";
        }
        if (pricing.getEffectiveTo() != null && pricing.getEffectiveTo().isBefore(pricing.getEffectiveFrom())) {
            return "INVALID";
        }
        if (pricing.getSeatTypeId() == null) pricing.setSeatTypeId(1L);
        if (pricing.getId() == null) {
            return pricingDAO.insert(pricing) ? "OK" : "FAILED";
        }
        TicketPricing current = pricingDAO.findById(pricing.getId());
        if (current == null) return "NOT_FOUND";
        if (manager && (current.getCinemaId() == null || !current.getCinemaId().equals(actor.getCinemaId()))) {
            return "FORBIDDEN";
        }
        return pricingDAO.update(pricing) ? "OK" : "FAILED";
    }

    @Override
    public String deletePrice(Long id, User actor) {
        if (id == null || actor == null || actor.getRole() == null) return "INVALID";
        TicketPricing current = pricingDAO.findById(id);
        if (current == null) return "NOT_FOUND";
        String role = actor.getRole().getRoleName();
        if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            if (current.getCinemaId() == null || actor.getCinemaId() == null || !current.getCinemaId().equals(actor.getCinemaId())) {
                return "FORBIDDEN";
            }
        } else if (!RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            return "FORBIDDEN";
        }
        return pricingDAO.softDelete(id) ? "OK" : "FAILED";
    }

    @Override
    public int getStudentDiscountPercent() {
        return systemSettingDAO.getIntSetting("STUDENT_TICKET_DISCOUNT_PERCENT", 15);
    }

    @Override
    public int getSeniorDiscountPercent() {
        return systemSettingDAO.getIntSetting("SENIOR_TICKET_DISCOUNT_PERCENT", 10);
    }

    @Override
    public String saveDiscountPercents(int studentPercent, int seniorPercent, User actor) {
        if (actor == null || actor.getRole() == null) return "FORBIDDEN";
        String role = actor.getRole().getRoleName();
        if (!RoleConstant.ADMIN.equalsIgnoreCase(role) && !RoleConstant.MANAGER.equalsIgnoreCase(role)) return "FORBIDDEN";
        if (studentPercent < 0 || studentPercent > 100 || seniorPercent < 0 || seniorPercent > 100) return "INVALID";
        Integer updatedBy = actor.getId() == null ? null : actor.getId().intValue();
        boolean student = systemSettingDAO.upsertSetting("STUDENT_TICKET_DISCOUNT_PERCENT", String.valueOf(studentPercent),
                "Phần trăm giảm vé HSSV", "OPERATIONAL", updatedBy);
        boolean senior = systemSettingDAO.upsertSetting("SENIOR_TICKET_DISCOUNT_PERCENT", String.valueOf(seniorPercent),
                "Phần trăm giảm vé người cao tuổi", "OPERATIONAL", updatedBy);
        return student && senior ? "OK" : "FAILED";
    }
}
