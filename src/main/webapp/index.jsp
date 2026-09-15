<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Chuyển hướng mặc định về Trang chủ / Danh mục phim của Khách hàng
    response.sendRedirect(request.getContextPath() + "/movies");
%>
