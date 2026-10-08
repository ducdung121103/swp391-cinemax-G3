package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Danh mục thể loại phim (Bảng genres - Zone 3)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Genre extends BaseEntity {
    private String name;
    private String description;

    // Helper getters cho tương thích JSTL UI
    public Long getGenreId() {
        return getId();
    }

    public String getGenreName() {
        return name;
    }
}
