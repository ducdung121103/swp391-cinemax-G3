package com.cinema.modules.catalog.controller;

import com.cinema.model.Movie;
import com.cinema.modules.catalog.service.MovieService;
import com.cinema.modules.catalog.service.impl.MovieServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller hiển thị danh sách phim và chi tiết phim cho khách hàng (TV 3).
 */
@WebServlet(name = "MovieServlet", urlPatterns = {"/movies", "/movie/detail"})
public class MovieServlet extends HttpServlet {
    private final MovieService movieService = new MovieServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/movie/detail".equals(path)) {
            String idStr = req.getParameter("id");
            if (idStr != null) {
                try {
                    Long id = Long.parseLong(idStr);
                    Movie movie = movieService.getMovieById(id);
                    req.setAttribute("movie", movie);
                    req.getRequestDispatcher("/WEB-INF/views/customer/catalog/movie-detail.jsp").forward(req, resp);
                    return;
                } catch (NumberFormatException ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/movies");
            return;
        }

        // Mặc định là /movies
        List<Movie> nowShowing = movieService.getNowShowingMovies();
        List<Movie> comingSoon = movieService.getComingSoonMovies();
        req.setAttribute("nowShowing", nowShowing);
        req.setAttribute("comingSoon", comingSoon);
        req.getRequestDispatcher("/WEB-INF/views/customer/catalog/movies.jsp").forward(req, resp);
    }
}
