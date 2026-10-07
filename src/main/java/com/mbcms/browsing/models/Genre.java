package com.mbcms.browsing.models;

import java.io.Serializable;

public class Genre implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer genreId;
    private String genreName;
    private String description;
    private boolean active;

    public Genre() {
    }

    public Genre(Integer genreId, String genreName, String description, boolean active) {
        this.genreId = genreId;
        this.genreName = genreName;
        this.description = description;
        this.active = active;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Genre{" +
                "genreId=" + genreId +
                ", genreName='" + genreName + '\'' +
                '}';
    }
}
