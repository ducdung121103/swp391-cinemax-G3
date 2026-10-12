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
import java.util.List;

/**
 * Controller hiển thị Trang chủ, Danh mục phim, Bộ lọc tìm kiếm và Chi tiết phim cho Khách hàng (TV 3).
 */
@WebServlet(name = "MovieServlet", urlPatterns = {"/home", "/movies", "/movie/detail", "/browse"})
public class MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 12;

    private final MovieService movieService = new MovieServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        // 1. Tuyến đường TRANG CHỦ (/home)
        if ("/home".equals(path)) {
            handleHome(req, resp);
            return;
        }

        // 2. Tuyến đường CHI TIẾT PHIM (/movie/detail)
        if ("/movie/detail".equals(path)) {
            handleMovieDetail(req, resp);
            return;
        }

        // 3. Tuyến đường DANH SÁCH & TÌM KIẾM PHIM (/movies, /browse)
        handleBrowseMovies(req, resp);
    }

    private void handleHome(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Movie> featuredMovies = movieService.getFeaturedMovies(5);
        List<Movie> nowShowingMovies = movieService.getNowShowingMovies(8);
        List<Movie> comingSoonMovies = movieService.getComingSoonMovies(8);
        List<Genre> genres = movieService.getAllGenres();

        req.setAttribute("featuredMovies", featuredMovies);
        req.setAttribute("nowShowingMovies", nowShowingMovies);
        req.setAttribute("comingSoonMovies", comingSoonMovies);
        req.setAttribute("genres", genres);
        req.setAttribute("activeMenu", "home");

        req.getRequestDispatcher("/WEB-INF/views/customer/catalog/home.jsp").forward(req, resp);
    }

    private void handleMovieDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(idStr.trim());
                Movie movie = movieService.getMovieById(id);
                if (movie != null) {
                    List<Movie> relatedMovies = movieService.getRelatedMovies(id, 4);
                    req.setAttribute("movie", movie);
                    req.setAttribute("relatedMovies", relatedMovies);
                    req.setAttribute("activeMenu", "movies");
                    req.getRequestDispatcher("/WEB-INF/views/customer/catalog/movie-detail.jsp").forward(req, resp);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/movies");
    }

    private void handleBrowseMovies(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = req.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            type = "all";
        }

        String keyword = req.getParameter("q");
        if (keyword != null) {
            keyword = keyword.trim();
        }

        String genreIdStr = req.getParameter("genreId");
        Long genreId = null;
        if (genreIdStr != null && !genreIdStr.trim().isEmpty()) {
            try {
                genreId = Long.parseLong(genreIdStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String ageRating = req.getParameter("ageRating");
        if (ageRating != null) {
            ageRating = ageRating.trim();
        }

        String sortBy = req.getParameter("sort");
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "release_desc";
        }

        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr.trim());
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {
            }
        }

        List<Movie> movies = movieService.browseMovies(type, keyword, genreId, ageRating, sortBy, page, PAGE_SIZE);
        int totalCount = movieService.countMovies(type, keyword, genreId, ageRating);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        List<Genre> genres = movieService.getAllGenres();

        req.setAttribute("movies", movies);
        req.setAttribute("genres", genres);
        req.setAttribute("totalCount", totalCount);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("currentPage", page);
        req.setAttribute("type", type);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedGenreId", genreId);
        req.setAttribute("selectedAgeRating", ageRating);
        req.setAttribute("selectedSort", sortBy);
        req.setAttribute("activeMenu", "movies");

        req.getRequestDispatcher("/WEB-INF/views/customer/catalog/movies.jsp").forward(req, resp);
    }
}
