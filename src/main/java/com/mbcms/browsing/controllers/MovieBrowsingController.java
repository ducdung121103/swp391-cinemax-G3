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

@WebServlet(name = "MovieBrowsingController", urlPatterns = {"/movies", "/browse"})
public class MovieBrowsingController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE = 12;
    private final MovieService movieService = new MovieService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Nhận các tham số tìm kiếm & lọc
        String type = request.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            type = "all"; // all, now_showing, coming_soon
        }

        String keyword = request.getParameter("q");
        if (keyword != null) {
            keyword = keyword.trim();
        }

        String genreIdStr = request.getParameter("genreId");
        Integer genreId = null;
        if (genreIdStr != null && !genreIdStr.trim().isEmpty()) {
            try {
                genreId = Integer.parseInt(genreIdStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String ageRating = request.getParameter("ageRating");
        if (ageRating != null) {
            ageRating = ageRating.trim();
        }

        String sortBy = request.getParameter("sort");
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "release_desc";
        }

        String pageStr = request.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr.trim());
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {
            }
        }

        // 2. Gọi service lấy danh sách phim và đếm tổng
        List<Movie> movies = movieService.browseMovies(type, keyword, genreId, ageRating, sortBy, page, PAGE_SIZE);
        int totalCount = movieService.countMovies(type, keyword, genreId, ageRating);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        // 3. Lấy tất cả thể loại cho filter dropdown
        List<Genre> genres = movieService.getAllGenres();

        // 4. Đặt attributes cho JSP
        request.setAttribute("movies", movies);
        request.setAttribute("genres", genres);
        request.setAttribute("type", type);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedGenreId", genreId);
        request.setAttribute("selectedAgeRating", ageRating);
        request.setAttribute("selectedSort", sortBy);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("activeMenu", "movies");

        // Forward tới trang duyệt phim
        request.getRequestDispatcher("/views/browsing.jsp").forward(request, response);
    }
}
