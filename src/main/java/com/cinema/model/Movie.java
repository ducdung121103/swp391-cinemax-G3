package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity Danh mục phim chiếu rạp (Bảng movies - Zone 3)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Movie extends BaseEntity {
    private String title;
    private String originalTitle;
    private String posterUrl;
    private String trailerUrl;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private LocalDate endDate;
    private String ageRating; // P, K, T13, T16, T18, C
    private String language;
    private String director;
    private String actors;
    private String castMembers;
    private String synopsis;
    private String status; // COMING_SOON, NOW_SHOWING, ENDED
    @Builder.Default
    private Double rating = 4.8;

    // Quan hệ điều hướng
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    // =========================================================================
    // Các Helper Getters tương thích 100% với giao diện UI & Hệ thống
    // =========================================================================

    public Long getMovieId() {
        return getId();
    }

    public String getDescription() {
        return synopsis != null ? synopsis : "";
    }

    public Integer getDuration() {
        return durationMinutes != null ? durationMinutes : 0;
    }

    public String getCast() {
        if (actors != null && !actors.trim().isEmpty()) {
            return actors;
        }
        return castMembers != null ? castMembers : "";
    }

    public Double getRating() {
        return rating != null ? rating : 4.8;
    }

    /**
     * Định dạng ngày phát hành: dd/MM/yyyy
     */
    public String getFormattedReleaseDate() {
        if (releaseDate == null) {
            return "Đang cập nhật";
        }
        return releaseDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Chuỗi thể loại cách nhau bởi dấu phẩy
     */
    public String getGenreString() {
        if (genres == null || genres.isEmpty()) {
            return "Đang cập nhật";
        }
        return genres.stream()
                .map(Genre::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .collect(Collectors.joining(", "));
    }

    /**
     * Kiểm tra phim có đang chiếu hôm nay không
     */
    public boolean isNowShowing() {
        if ("NOW_SHOWING".equalsIgnoreCase(status)) {
            return true;
        }
        if (releaseDate == null) return false;
        LocalDate today = LocalDate.now();
        boolean isReleased = !releaseDate.isAfter(today);
        boolean notEnded = (endDate == null || !endDate.isBefore(today));
        return isReleased && notEnded;
    }

    /**
     * Kiểm tra phim sắp chiếu
     */
    public boolean isComingSoon() {
        if ("COMING_SOON".equalsIgnoreCase(status)) {
            return true;
        }
        if (releaseDate == null) return false;
        return releaseDate.isAfter(LocalDate.now());
    }

    /**
     * Đổi link Youtube thông thường thành link embed xem được trong modal
     */
    public String getEmbedTrailerUrl() {
        if (trailerUrl == null || trailerUrl.trim().isEmpty()) {
            return "";
        }
        String url = trailerUrl.trim();
        if (url.contains("youtube.com/embed/")) {
            return url;
        }
        if (url.contains("youtube.com/watch?v=")) {
            String videoId = url.substring(url.indexOf("v=") + 2);
            int amp = videoId.indexOf('&');
            if (amp != -1) {
                videoId = videoId.substring(0, amp);
            }
            return "https://www.youtube.com/embed/" + videoId;
        } else if (url.contains("youtu.be/")) {
            String videoId = url.substring(url.indexOf("youtu.be/") + 9);
            int q = videoId.indexOf('?');
            if (q != -1) {
                videoId = videoId.substring(0, q);
            }
            return "https://www.youtube.com/embed/" + videoId;
        }
        return url;
    }

    /**
     * Lấy class CSS badge nhãn độ tuổi
     */
    public String getAgeBadgeClass() {
        if (ageRating == null) return "badge-p";
        switch (ageRating.toUpperCase()) {
            case "P":
            case "G":
                return "badge-p";
            case "K":
            case "PG":
                return "badge-k";
            case "T13":
            case "C13":
            case "PG-13":
                return "badge-t13";
            case "T16":
            case "C16":
                return "badge-t16";
            case "T18":
            case "C18":
            case "R":
                return "badge-t18";
            default:
                return "badge-p";
        }
    }
}
