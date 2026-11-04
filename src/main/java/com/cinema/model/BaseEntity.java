package com.cinema.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Lá»›p thá»±c thá»ƒ cÆ¡ sá»Ÿ (Base Entity) chuáº©n hÃ³a cho toÃ n bá»™ 28 báº£ng CSDL.
 * Chá»©a cÃ¡c trÆ°á»ng váº¿t há»‡ thá»‘ng (Audit Fields) vÃ  cá» tráº¡ng thÃ¡i Soft Delete.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@Getter
@Setter
@ToString
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** KhÃ³a chÃ­nh Ä‘á»‹nh danh duy nháº¥t */
    protected Long id;

    /** Thá»i Ä‘iá»ƒm khá»Ÿi táº¡o báº£n ghi */
    protected LocalDateTime createdAt;

    /** Thá»i Ä‘iá»ƒm cáº­p nháº­t cuá»‘i cÃ¹ng */
    protected LocalDateTime updatedAt;

    /** Tráº¡ng thÃ¡i kÃ­ch hoáº¡t (1: Hoáº¡t Ä‘á»™ng, 0: Táº¡m ngÆ°ng) */
    protected Boolean isActive = true;

    /** Tráº¡ng thÃ¡i xÃ³a má»m (1: ÄÃ£ xÃ³a, 0: BÃ¬nh thÆ°á»ng) */
    protected Boolean isDeleted = false;
}

