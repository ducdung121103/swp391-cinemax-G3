package com.cinema.modules.operation.controller;

import com.cinema.model.FnBItem;
import com.cinema.modules.operation.dao.FnBItemDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import com.cinema.model.User;
import com.cinema.model.Showtime;
import com.cinema.model.Seat;
import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.operation.service.PosOrderService;
import com.cinema.modules.operation.service.impl.PosOrderServiceImpl;
import com.cinema.modules.operation.dao.PosSeatDAO;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller quầy bán vé & bắp nước POS cảm ứng tại rạp (TV 5).
 */
@WebServlet(name = "PosCounterServlet", urlPatterns = {"/admin/operation/pos"})
public class PosCounterServlet extends HttpServlet {
    private final FnBItemDAO fnbItemDAO = new FnBItemDAO();
    private final ShowtimeService showtimeService = new ShowtimeServiceImpl();
    private final ScreeningRoomService roomService = new ScreeningRoomServiceImpl();
    private final PricingService pricingService = new PricingServiceImpl();
    private final SystemSettingDAO settings = new SystemSettingDAO();
    private final PosOrderService posOrderService = new PosOrderServiceImpl();
    private final PosSeatDAO posSeatDAO = new PosSeatDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User staff=(User)req.getSession().getAttribute("currentUser");
        req.setAttribute("staff",staff);
        Long cinemaId=staff.getCinemaId();
        if(cinemaId==null){String selected=req.getParameter("cinemaId");List<com.cinema.model.Cinema> cinemas=new CinemaDAO().findAll();req.setAttribute("cinemas",cinemas);if(selected!=null&&!selected.isBlank())cinemaId=Long.valueOf(selected);else if(!cinemas.isEmpty())cinemaId=cinemas.get(0).getId();if(cinemaId==null)req.setAttribute("posSetupMessage","Chưa có chi nhánh. Hãy tạo chi nhánh và phòng chiếu trước khi bán vé.");}
        if(cinemaId!=null){req.setAttribute("cinemaId",cinemaId);req.setAttribute("showtimes",showtimeService.getShowtimesByCinemaAndDate(cinemaId,LocalDate.now()));req.setAttribute("fnbItems",fnbItemDAO.findAllActiveForCinema(cinemaId));}
        String selectedShowtime=req.getParameter("showtimeId");
        if(selectedShowtime!=null&&!selectedShowtime.isBlank()&&cinemaId!=null){
            try{
                Showtime st=showtimeService.getShowtimeById(Long.parseLong(selectedShowtime));
                com.cinema.model.ScreeningRoom room=st==null?null:roomService.getRoomById(st.getScreeningRoomId());
                if(st!=null&&room!=null&&Objects.equals(room.getCinemaId(),cinemaId)){
                    req.setAttribute("selectedShowtime",st.getId());
                    req.setAttribute("availableSeats",posSeatDAO.findAvailable(st.getId(),room.getId()));
                }
            }catch(NumberFormatException e){
                req.setAttribute("posError","Mã suất chiếu không hợp lệ. Hãy chọn lại suất chiếu.");
            }catch(Exception e){
                throw new ServletException("Không thể tải sơ đồ ghế của suất chiếu.",e);
            }
        }
        if(req.getAttribute("posError")==null){
            req.setAttribute("posError",req.getSession().getAttribute("posError"));
        }
        req.getSession().removeAttribute("posError");
        req.setAttribute("studentDiscount",discount("STUDENT_TICKET_DISCOUNT_PERCENT",15));
        req.setAttribute("seniorDiscount",discount("SENIOR_TICKET_DISCOUNT_PERCENT",10));
        req.setAttribute("checkoutResult",req.getSession().getAttribute("posCheckoutResult"));
        req.getSession().removeAttribute("posCheckoutResult");
        req.getRequestDispatcher("/WEB-INF/views/admin/operation/pos-counter.jsp").forward(req, resp);
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");
        try{
            User staff=(User)req.getSession().getAttribute("currentUser");
            long showtimeId=Long.parseLong(req.getParameter("showtimeId"));
            Showtime showtime=showtimeService.getShowtimeById(showtimeId);
            com.cinema.model.ScreeningRoom room=showtime==null?null:roomService.getRoomById(showtime.getScreeningRoomId());
            long expectedCinema=staff.getCinemaId()!=null?staff.getCinemaId():Long.parseLong(req.getParameter("cinemaId"));
            if(showtime==null||room==null||!Objects.equals(expectedCinema,room.getCinemaId()))throw new IllegalArgumentException("Suất chiếu không thuộc rạp đã chọn.");
            if(showtime.getStartTime()==null||!showtime.getStartTime().isAfter(java.time.LocalDateTime.now())||"CANCELLED".equalsIgnoreCase(showtime.getStatus()))throw new IllegalArgumentException("Không thể bán vé cho suất chiếu đã qua hoặc đã hủy.");
            String[] rawSeats=req.getParameterValues("seatIds");
            if(rawSeats==null||rawSeats.length==0)throw new IllegalArgumentException("Chọn ít nhất một ghế.");
            List<Long> seats=new ArrayList<>();List<BigDecimal> prices=new ArrayList<>();
            String ticketClass=req.getParameter("ticketClass");BigDecimal multiplier=BigDecimal.ONE;
            if("STUDENT".equals(ticketClass))multiplier=BigDecimal.ONE.subtract(discount("STUDENT_TICKET_DISCOUNT_PERCENT",15).movePointLeft(2));
            else if("SENIOR".equals(ticketClass))multiplier=BigDecimal.ONE.subtract(discount("SENIOR_TICKET_DISCOUNT_PERCENT",10).movePointLeft(2));
            else if(!"STANDARD".equals(ticketClass))throw new IllegalArgumentException("Loại giá vé không hợp lệ.");
            for(String raw:rawSeats){long seatId=Long.parseLong(raw);Seat seat=roomService.getSeatById(seatId);if(seat==null||!Objects.equals(seat.getScreeningRoomId(),showtime.getScreeningRoomId()))throw new IllegalArgumentException("Ghế không thuộc phòng chiếu đã chọn.");seats.add(seatId);prices.add(pricingService.calculateTicketPrice(showtimeId,seat.getSeatTypeId(),showtime.getStartTime(),showtime.getExperienceFormat()).multiply(multiplier).setScale(2,java.math.RoundingMode.HALF_UP));}
            if(new HashSet<>(seats).size()!=seats.size())throw new IllegalArgumentException("Danh sách ghế bị trùng.");
            String paymentMethod=req.getParameter("paymentMethod");if(!"CASH".equals(paymentMethod)&&!"VNPAY_POS".equals(paymentMethod))throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
            Map<Long,Integer> fnb=new LinkedHashMap<>();for(Map.Entry<String,String[]> e:req.getParameterMap().entrySet())if(e.getKey().startsWith("fnb_")){int qty=Integer.parseInt(e.getValue()[0]);if(qty>0)fnb.put(Long.parseLong(e.getKey().substring(4)),qty);}
            CreateBookingDTO dto=CreateBookingDTO.builder().staffId(staff.getId()).showtimeId(showtimeId).seatIds(seats).ticketPrices(prices).fnbItems(fnb).channel("POS").paymentMethod(paymentMethod).sessionId(req.getSession().getId()).build();
            BookingResult result=posOrderService.processCounterCheckout(dto);req.getSession().setAttribute("posCheckoutResult",result);
            resp.sendRedirect(req.getContextPath()+"/admin/operation/pos?cinemaId="+expectedCinema);
        }catch(IllegalArgumentException|IllegalStateException e){req.getSession().setAttribute("posError",e.getMessage());String selected=req.getParameter("showtimeId");String cinema=req.getParameter("cinemaId");resp.sendRedirect(req.getContextPath()+"/admin/operation/pos?cinemaId="+(cinema==null?"":cinema)+"&showtimeId="+selected);}catch(Exception e){throw new ServletException("Không thể tạo giao dịch POS",e);}
    }
    private BigDecimal discount(String key,int fallback){int pct=settings.getIntSetting(key,fallback);return BigDecimal.valueOf(Math.max(0,Math.min(100,pct)));}
}
