package com.mbcms.browsing;

import com.mbcms.browsing.config.DBContext;
import com.mbcms.browsing.dao.GenreDAO;
import com.mbcms.browsing.dao.MovieDAO;
import com.mbcms.browsing.models.Genre;
import com.mbcms.browsing.models.Movie;
import com.mbcms.browsing.services.MovieService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử module Movie Browsing (Now Showing & Coming Soon)")
public class MovieBrowsingTest {

    private static MovieService movieService;
    private static MovieDAO movieDAO;
    private static GenreDAO genreDAO;

    @BeforeAll
    static void setUp() {
        movieDAO = new MovieDAO();
        genreDAO = new GenreDAO();
        movieService = new MovieService(movieDAO, genreDAO);
    }

    @Test
    @DisplayName("1. Kiểm tra kết nối SQL Server")
    void testDatabaseConnection() {
        boolean connected = DBContext.testConnection();
        assertTrue(connected, "Kết nối tới SQL Server phải thành công!");
    }

    @Test
    @DisplayName("2. Kiểm tra danh mục thể loại (Genres)")
    void testGetAllGenres() {
        List<Genre> genres = movieService.getAllGenres();
        assertNotNull(genres, "Danh sách thể loại không được null");
        assertFalse(genres.isEmpty(), "Danh sách thể loại phải có ít nhất 1 bản ghi");
        assertTrue(genres.size() >= 5, "Danh sách thể loại mẫu có ít nhất 5 thể loại");
        System.out.println(">>> Đã tải thành công " + genres.size() + " thể loại phim.");
    }

    @Test
    @DisplayName("3. Kiểm tra danh sách Phim Đang Chiếu (Now Showing)")
    void testNowShowingMovies() {
        List<Movie> nowShowing = movieService.getNowShowingMovies(8);
        assertNotNull(nowShowing, "Danh sách phim đang chiếu không được null");
        assertFalse(nowShowing.isEmpty(), "Phải có phim đang chiếu trong hệ thống");

        LocalDate today = LocalDate.now();
        for (Movie m : nowShowing) {
            assertTrue(m.getReleaseDate().compareTo(today) <= 0,
                    "Phim đang chiếu phải có release_date <= ngày hiện tại: " + m.getTitle());
            if (m.getEndDate() != null) {
                assertTrue(m.getEndDate().compareTo(today) >= 0,
                        "Phim đang chiếu nếu có end_date thì phải >= ngày hiện tại: " + m.getTitle());
            }
            assertTrue(m.isNowShowing(), "Helper method isNowShowing() phải trả về true");
            assertNotNull(m.getTitle(), "Tên phim không được rỗng");
            assertNotNull(m.getPosterUrl(), "Poster URL không được rỗng");
            assertFalse(m.getGenres().isEmpty(), "Phim phải có ít nhất 1 thể loại");
        }
        System.out.println(">>> Phim đang chiếu: " + nowShowing.size() + " phim. Phim đầu tiên: " + nowShowing.get(0).getTitle());
    }

    @Test
    @DisplayName("4. Kiểm tra danh sách Phim Sắp Chiếu (Coming Soon)")
    void testComingSoonMovies() {
        List<Movie> comingSoon = movieService.getComingSoonMovies(8);
        assertNotNull(comingSoon, "Danh sách phim sắp chiếu không được null");
        assertFalse(comingSoon.isEmpty(), "Phải có phim sắp chiếu trong hệ thống");

        LocalDate today = LocalDate.now();
        for (Movie m : comingSoon) {
            assertTrue(m.getReleaseDate().isAfter(today),
                    "Phim sắp chiếu phải có release_date > ngày hiện tại: " + m.getTitle());
            assertTrue(m.isComingSoon(), "Helper method isComingSoon() phải trả về true");
            assertFalse(m.isNowShowing(), "Phim sắp chiếu không được đánh dấu là đang chiếu");
        }
        System.out.println(">>> Phim sắp chiếu: " + comingSoon.size() + " phim. Phim đầu tiên: " + comingSoon.get(0).getTitle());
    }

    @Test
    @DisplayName("5. Kiểm tra danh sách Phim Bom Tấn Nổi Bật (Featured Movies)")
    void testFeaturedMovies() {
        List<Movie> featured = movieService.getFeaturedMovies(5);
        assertNotNull(featured);
        assertFalse(featured.isEmpty());
        assertTrue(featured.size() <= 5);
        System.out.println(">>> Phim nổi bật cho Carousel: " + featured.size() + " phim.");
    }

    @Test
    @DisplayName("6. Kiểm tra xem chi tiết phim và phim liên quan")
    void testMovieDetailAndRelated() {
        Movie movie = movieService.getMovieById(1);
        assertNotNull(movie, "Phim với ID 1 phải tồn tại");
        assertEquals(1, movie.getMovieId());
        assertNotNull(movie.getTitle());
        assertNotNull(movie.getDescription());
        assertTrue(movie.getDuration() > 0);
        assertNotNull(movie.getDirector());
        assertNotNull(movie.getCast());
        assertFalse(movie.getGenres().isEmpty());

        // Phim liên quan cùng thể loại
        List<Movie> related = movieService.getRelatedMovies(1, 4);
        assertNotNull(related);
        for (Movie rm : related) {
            assertNotEquals(1, rm.getMovieId(), "Phim liên quan không được trùng với phim hiện tại");
        }
        System.out.println(">>> Chi tiết phim: " + movie.getTitle() + " | Số phim liên quan: " + related.size());
    }

    @Test
    @DisplayName("7. Kiểm tra tìm kiếm và lọc phim nâng cao")
    void testSearchAndFilter() {
        // Tìm kiếm theo từ khóa "Chiến Binh"
        List<Movie> searchResult = movieService.browseMovies("all", "Chiến Binh", null, null, "release_desc", 1, 10);
        assertNotNull(searchResult);
        assertFalse(searchResult.isEmpty(), "Tìm kiếm 'Chiến Binh' phải có kết quả");
        assertTrue(searchResult.get(0).getTitle().contains("Chiến Binh"));

        // Lọc theo loại "now_showing"
        List<Movie> nowShowingFilter = movieService.browseMovies("now_showing", null, null, null, "release_desc", 1, 12);
        assertNotNull(nowShowingFilter);
        assertFalse(nowShowingFilter.isEmpty());

        // Lọc theo loại "coming_soon"
        List<Movie> comingSoonFilter = movieService.browseMovies("coming_soon", null, null, null, "release_desc", 1, 12);
        assertNotNull(comingSoonFilter);
        assertFalse(comingSoonFilter.isEmpty());

        // Đếm tổng số lượng phim
        int totalNowShowing = movieService.countMovies("now_showing", null, null, null);
        int totalComingSoon = movieService.countMovies("coming_soon", null, null, null);
        assertTrue(totalNowShowing > 0);
        assertTrue(totalComingSoon > 0);

        System.out.println(">>> Đếm phim: Đang chiếu = " + totalNowShowing + ", Sắp chiếu = " + totalComingSoon);
    }
}
