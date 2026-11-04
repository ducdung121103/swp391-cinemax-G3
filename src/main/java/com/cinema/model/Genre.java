package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Danh má»¥c thá»ƒ loáº¡i phim (Báº£ng genres - Zone 3)
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Genre extends BaseEntity {
    private String name;
    private String description;

    // Helper getters cho tÆ°Æ¡ng thÃ­ch JSTL UI
    public Long getGenreId() {
        return getId();
    }

    public String getGenreName() {
        return name;
    }
}

