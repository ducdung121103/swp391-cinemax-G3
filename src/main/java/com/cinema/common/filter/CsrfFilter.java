package com.cinema.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * Lớp phòng thủ CSRF nhẹ: kiểm tra Origin/Referer của mọi request POST.
 * Nếu nguồn gửi khác host/port của server thì chặn 403.
 */
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Sinh token CSRF theo session và gắn ra request cho JSP
        HttpSession session = req.getSession(true);
        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null || csrfToken.isBlank()) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
        }
        req.setAttribute("csrfToken", csrfToken);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String origin = req.getHeader("Origin");
            String referer = req.getHeader("Referer");
            String source = (origin != null && !origin.isBlank()) ? origin : referer;

            if (source != null && !source.isBlank()) {
                try {
                    URI uri = URI.create(source);
                    String host = uri.getHost();
                    int port = uri.getPort();
                    String serverHost = req.getServerName();
                    int serverPort = req.getServerPort();

                    boolean hostOk = host != null && (host.equalsIgnoreCase(serverHost)
                            || ("127.0.0.1".equals(host) && "localhost".equalsIgnoreCase(serverHost))
                            || ("localhost".equals(host) && "127.0.0.1".equals(serverHost)));

                    int effectivePort = port == -1
                            ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80)
                            : port;
                    boolean portOk = effectivePort == serverPort
                            || (effectivePort == 80 && serverPort == 80)
                            || (effectivePort == 443 && serverPort == 443);

                    if (!hostOk || !portOk) {
                        res.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Yêu cầu bị chặn do nguồn gửi không hợp lệ (CSRF)!");
                        return;
                    }
                } catch (Exception e) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Yêu cầu bị chặn do nguồn gửi không hợp lệ (CSRF)!");
                    return;
                }
            }

            // Token CSRF bắt buộc cho các form quản trị hạ tầng
            String path = req.getRequestURI().substring(req.getContextPath().length());
            if (path.startsWith("/admin/infrastructure")) {
                String submitted = req.getParameter("_csrf");
                if (submitted == null || submitted.isBlank()) {
                    submitted = req.getHeader("X-CSRF-TOKEN");
                }
                if (submitted == null || !submitted.equals(csrfToken)) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Phiên làm việc không hợp lệ hoặc thiếu CSRF token!");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}
