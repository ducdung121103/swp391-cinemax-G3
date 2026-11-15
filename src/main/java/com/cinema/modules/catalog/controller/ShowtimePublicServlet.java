package com.cinema.modules.catalog.controller;

import com.cinema.model.Cinema;
import com.cinema.model.Movie;
import com.cinema.model.Showtime;
import com.cinema.modules.catalog.service.MovieService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.MovieServiceImpl;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lịch chiếu công khai. Khách chưa đăng nhập vẫn xem được.
 * Movie-first: group suất theo phim trước khi forward JSP.
 */
@WebServlet(name = "ShowtimePublicServlet", urlPatterns = {"/showtimes"})
public class ShowtimePublicServlet extends HttpServlet {
    private final ShowtimeService showtimeService = new ShowtimeServiceImpl();
    private final MovieService movieService = new MovieServiceImpl();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long movieId = parseLong(req.getParameter("movieId"));
        Long cinemaId = parseLong(req.getParameter("cinemaId"));
        LocalDate date = resolveDate(req.getParameter("date"), movieId, cinemaId);

        List<Movie> movies = movieService.getNowShowingMovies(100);
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<Showtime> showtimes = showtimeService.searchScheduled(movieId, cinemaId, date);
        Map<Movie, List<Showtime>> movieShowtimes = groupByMovie(showtimes);

        req.setAttribute("activeMenu", "showtimes");
        req.setAttribute("movies", movies);
        req.setAttribute("cinemas", cinemas);
        req.setAttribute("showtimes", showtimes);
        req.setAttribute("movieShowtimes", movieShowtimes);
        req.setAttribute("selectedMovieId", movieId);
        req.setAttribute("selectedCinemaId", cinemaId);
        req.setAttribute("selectedDate", date.toString());
        req.getRequestDispatcher("/WEB-INF/views/customer/catalog/showtimes.jsp").forward(req, resp);
    }

    /**
     * Group theo movieId, giữ thứ tự suất (đã sort theo giờ) và thứ tự phim theo suất sớm nhất.
     */
    private Map<Movie, List<Showtime>> groupByMovie(List<Showtime> showtimes) {
        LinkedHashMap<Long, Movie> moviesById = new LinkedHashMap<>();
        LinkedHashMap<Long, List<Showtime>> listsById = new LinkedHashMap<>();
        if (showtimes != null) {
            for (Showtime st : showtimes) {
                if (st == null || st.getMovieId() == null || st.getMovie() == null) continue;
                moviesById.putIfAbsent(st.getMovieId(), st.getMovie());
                listsById.computeIfAbsent(st.getMovieId(), id -> new ArrayList<>()).add(st);
            }
        }
        LinkedHashMap<Movie, List<Showtime>> grouped = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Showtime>> entry : listsById.entrySet()) {
            grouped.put(moviesById.get(entry.getKey()), entry.getValue());
        }
        return grouped;
    }

    private LocalDate resolveDate(String raw, Long movieId, Long cinemaId) {
        if (raw != null && !raw.isBlank()) {
            try {
                return LocalDate.parse(raw.trim());
            } catch (Exception ignored) {
                return LocalDate.now();
            }
        }
        LocalDate today = LocalDate.now();
        if (!showtimeService.searchScheduled(movieId, cinemaId, today).isEmpty()) {
            return today;
        }
        LocalDate latest = showtimeService.findLatestScheduledDate(movieId, cinemaId);
        return latest != null ? latest : today;
    }

    private Long parseLong(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
