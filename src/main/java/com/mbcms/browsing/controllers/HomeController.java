package com.mbcms.browsing.controllers;

import com.mbcms.browsing.models.Genre;
import com.mbcms.browsing.models.Movie;
import com.mbcms.browsing.services.MovieService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "HomeController", urlPatterns = {"/home"})
public class HomeController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy danh sách phim nổi bật cho Banner Carousel (5 phim)
        List<Movie> featuredMovies = movieService.getFeaturedMovies(5);

        // 2. Lấy danh sách phim Đang Chiếu (Now Showing - 8 phim)
        List<Movie> nowShowingMovies = movieService.getNowShowingMovies(8);

        // 3. Lấy danh sách phim Sắp Chiếu (Coming Soon - 8 phim)
        List<Movie> comingSoonMovies = movieService.getComingSoonMovies(8);

        // 4. Lấy danh mục thể loại để hiển thị menu nhanh
        List<Genre> genres = movieService.getAllGenres();

        // Đưa dữ liệu vào request attributes
        request.setAttribute("featuredMovies", featuredMovies);
        request.setAttribute("nowShowingMovies", nowShowingMovies);
        request.setAttribute("comingSoonMovies", comingSoonMovies);
        request.setAttribute("genres", genres);
        request.setAttribute("activeMenu", "home");

        // Forward sang JSP
        request.getRequestDispatcher("/views/home.jsp").forward(request, response);
    }
}
