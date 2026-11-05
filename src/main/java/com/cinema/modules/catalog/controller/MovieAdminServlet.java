package com.cinema.modules.catalog.controller;

import com.cinema.model.Genre;
import com.cinema.model.Movie;
import com.cinema.modules.catalog.service.MovieService;
import com.cinema.modules.catalog.service.impl.MovieServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller Quản lý Danh mục Phim dành riêng cho ADMIN (Movie Catalog Management - M-02.1).
 */
@WebServlet(name = "MovieAdminServlet", urlPatterns = {"/admin/catalog/movies", "/admin/movies"})
public class MovieAdminServlet extends HttpServlet {
    private final MovieService movieService = new MovieServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        String ageRating = req.getParameter("ageRating");
        String genreIdParam = req.getParameter("genreId");

        Long genreId = null;
        if (genreIdParam != null && !genreIdParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(genreIdParam)) {
            try {
                genreId = Long.parseLong(genreIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        List<Movie> movies = movieService.getAllMoviesForAdmin(keyword, status, ageRating, genreId);
        List<Genre> genres = movieService.getAllGenres();

        req.setAttribute("movies", movies);
        req.setAttribute("genres", genres);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedStatus", status);
        req.setAttribute("selectedAgeRating", ageRating);
        req.setAttribute("selectedGenreId", genreId);

        // Flash message handling
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) req.setAttribute("msg", msg);
        if (error != null) req.setAttribute("error", error);

        req.getRequestDispatcher("/WEB-INF/views/admin/catalog/movies.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        try {
            if ("create".equalsIgnoreCase(action)) {
                String title = req.getParameter("title");
                String originalTitle = req.getParameter("originalTitle");
                String durationMinutesStr = req.getParameter("durationMinutes");
                String releaseDateStr = req.getParameter("releaseDate");
                String endDateStr = req.getParameter("endDate");
                String ageRating = req.getParameter("ageRating");
                String language = req.getParameter("language");
                String director = req.getParameter("director");
                String actors = req.getParameter("actors");
                String castMembers = req.getParameter("castMembers");
                String synopsis = req.getParameter("synopsis");
                String posterUrl = req.getParameter("posterUrl");
                String trailerUrl = req.getParameter("trailerUrl");
                String status = req.getParameter("status");

                Integer durationMinutes = (durationMinutesStr != null && !durationMinutesStr.trim().isEmpty())
                        ? Integer.parseInt(durationMinutesStr.trim()) : 0;
                LocalDate releaseDate = (releaseDateStr != null && !releaseDateStr.trim().isEmpty())
                        ? LocalDate.parse(releaseDateStr.trim()) : null;
                LocalDate endDate = (endDateStr != null && !endDateStr.trim().isEmpty())
                        ? LocalDate.parse(endDateStr.trim()) : null;

                String[] genreIdArr = req.getParameterValues("genreIds");
                List<Long> genreIds = new ArrayList<>();
                if (genreIdArr != null) {
                    for (String g : genreIdArr) {
                        try {
                            genreIds.add(Long.parseLong(g.trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                Movie movie = Movie.builder()
                        .title(title)
                        .originalTitle(originalTitle)
                        .durationMinutes(durationMinutes)
                        .releaseDate(releaseDate)
                        .endDate(endDate)
                        .ageRating(ageRating)
                        .language(language)
                        .director(director)
                        .actors(actors)
                        .castMembers(castMembers)
                        .synopsis(synopsis)
                        .posterUrl(posterUrl)
                        .trailerUrl(trailerUrl)
                        .status(status)
                        .build();

                movieService.createMovie(movie, genreIds);
                resp.sendRedirect(req.getContextPath() + "/admin/catalog/movies?msg=created");
                return;

            } else if ("update".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String title = req.getParameter("title");
                String originalTitle = req.getParameter("originalTitle");
                String durationMinutesStr = req.getParameter("durationMinutes");
                String releaseDateStr = req.getParameter("releaseDate");
                String endDateStr = req.getParameter("endDate");
                String ageRating = req.getParameter("ageRating");
                String language = req.getParameter("language");
                String director = req.getParameter("director");
                String actors = req.getParameter("actors");
                String castMembers = req.getParameter("castMembers");
                String synopsis = req.getParameter("synopsis");
                String posterUrl = req.getParameter("posterUrl");
                String trailerUrl = req.getParameter("trailerUrl");
                String status = req.getParameter("status");

                Integer durationMinutes = (durationMinutesStr != null && !durationMinutesStr.trim().isEmpty())
                        ? Integer.parseInt(durationMinutesStr.trim()) : 0;
                LocalDate releaseDate = (releaseDateStr != null && !releaseDateStr.trim().isEmpty())
                        ? LocalDate.parse(releaseDateStr.trim()) : null;
                LocalDate endDate = (endDateStr != null && !endDateStr.trim().isEmpty())
                        ? LocalDate.parse(endDateStr.trim()) : null;

                String[] genreIdArr = req.getParameterValues("genreIds");
                List<Long> genreIds = new ArrayList<>();
                if (genreIdArr != null) {
                    for (String g : genreIdArr) {
                        try {
                            genreIds.add(Long.parseLong(g.trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                Movie movie = Movie.builder()
                        .title(title)
                        .originalTitle(originalTitle)
                        .durationMinutes(durationMinutes)
                        .releaseDate(releaseDate)
                        .endDate(endDate)
                        .ageRating(ageRating)
                        .language(language)
                        .director(director)
                        .actors(actors)
                        .castMembers(castMembers)
                        .synopsis(synopsis)
                        .posterUrl(posterUrl)
                        .trailerUrl(trailerUrl)
                        .status(status)
                        .build();
                movie.setId(id);

                movieService.updateMovie(movie, genreIds);
                resp.sendRedirect(req.getContextPath() + "/admin/catalog/movies?msg=updated");
                return;

            } else if ("changeStatus".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String newStatus = req.getParameter("status");
                movieService.updateMovieStatus(id, newStatus);
                resp.sendRedirect(req.getContextPath() + "/admin/catalog/movies?msg=status_updated");
                return;

            } else if ("delete".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                movieService.deleteMovie(id);
                resp.sendRedirect(req.getContextPath() + "/admin/catalog/movies?msg=deleted");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Đã có lỗi xảy ra", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/catalog/movies?error=" + errorMsg);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/catalog/movies");
    }
}
