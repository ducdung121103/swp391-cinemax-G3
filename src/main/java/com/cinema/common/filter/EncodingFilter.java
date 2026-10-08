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
 * Filter đảm bảo mã hóa ký tự UTF-8 cho toàn bộ Request và Response trong hệ thống.
 * Không ghi đè Content-Type của các tài nguyên tĩnh (.css, .js, .png, .jpg...).
 */
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
        // Chỉ set default content-type nếu request là servlet/trang động và chưa có extension tĩnh
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
