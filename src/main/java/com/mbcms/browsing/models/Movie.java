package com.mbcms.browsing.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer movieId;
    private String title;
    private String description;
    private Integer duration; // minutes
    private LocalDate releaseDate;
    private LocalDate endDate;
    private Double rating = 0.0;
    private String ageRating = "P";
    private String director;
    private String cast;
    private String posterUrl;
    private String trailerUrl;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Danh sách thể loại của phim
    private List<String> genres = new ArrayList<>();

    public Movie() {
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration != null ? duration : 0;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getRating() {
        return rating != null ? rating : 0.0;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getAgeRating() {
        return ageRating != null ? ageRating : "P";
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getPosterUrl() {
        if (posterUrl == null || posterUrl.trim().isEmpty()) {
            return "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80";
        }
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getGenres() {
        return genres != null ? genres : new ArrayList<>();
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    // =======================================================
    // Helper Methods cho View JSP
    // =======================================================

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
        return String.join(", ", genres);
    }

    /**
     * Kiểm tra phim có đang chiếu hôm nay không
     */
    public boolean isNowShowing() {
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
                return "badge-p"; // Mọi lứa tuổi (xanh lá)
            case "K":
            case "PG":
                return "badge-k"; // Dưới 13 tuổi có bảo trợ (xanh dương)
            case "T13":
            case "C13":
            case "PG-13":
                return "badge-t13"; // Trên 13 tuổi (vàng cam)
            case "T16":
            case "C16":
                return "badge-t16"; // Trên 16 tuổi (cam đỏ)
            case "T18":
            case "C18":
            case "R":
                return "badge-t18"; // Trên 18 tuổi (đỏ)
            default:
                return "badge-p";
        }
    }
}
