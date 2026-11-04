package com.cinema.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter Ä‘áº£m báº£o mÃ£ hÃ³a kÃ½ tá»± UTF-8 cho toÃ n bá»™ Request vÃ  Response trong há»‡ thá»‘ng.
 * KhÃ´ng ghi Ä‘Ã¨ Content-Type cá»§a cÃ¡c tÃ i nguyÃªn tÄ©nh (.css, .js, .png, .jpg...).
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class EncodingFilter implements Filter {
    private static final String ENCODING = "UTF-8";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        req.setCharacterEncoding(ENCODING);
        res.setCharacterEncoding(ENCODING);

        String path = req.getRequestURI();
        // Chá»‰ set default content-type náº¿u request lÃ  servlet/trang Ä‘á»™ng vÃ  chÆ°a cÃ³ extension tÄ©nh
        if (!isStaticResource(path) && res.getContentType() == null) {
            res.setContentType("text/html; charset=UTF-8");
        }

        chain.doFilter(request, response);
    }

    private boolean isStaticResource(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".css") || lower.endsWith(".js") || lower.endsWith(".png")
                || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif")
                || lower.endsWith(".svg") || lower.endsWith(".ico") || lower.endsWith(".woff")
                || lower.endsWith(".woff2") || lower.endsWith(".ttf") || lower.endsWith(".map");
    }
}

