package com.mbcms.browsing.controllers;

import com.mbcms.browsing.models.Movie;
import com.mbcms.browsing.services.MovieService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "MovieDetailController", urlPatterns = {"/movie-detail", "/movie"})
public class MovieDetailController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            idStr = request.getParameter("movieId");
        }

        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            int movieId = Integer.parseInt(idStr.trim());
            Movie movie = movieService.getMovieById(movieId);

            if (movie == null) {
                request.setAttribute("errorMessage", "Không tìm thấy thông tin bộ phim này.");
                request.getRequestDispatcher("/views/browsing.jsp").forward(request, response);
                return;
            }

            // Lấy danh sách phim liên quan (cùng thể loại)
            List<Movie> relatedMovies = movieService.getRelatedMovies(movieId, 4);

            request.setAttribute("movie", movie);
            request.setAttribute("relatedMovies", relatedMovies);
            request.setAttribute("activeMenu", "movies");

            request.getRequestDispatcher("/views/movie-detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}
