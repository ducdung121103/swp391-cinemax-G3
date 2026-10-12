<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty pageTitle ? pageTitle : 'CineMax - Hệ Thống Đặt Vé Phim & Cụm Rạp Toàn Quốc'}</title>
    
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Font Awesome 6 Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    
    <!-- Custom Cinema Dark Style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>

    <!-- Header Navbar -->
    <nav class="navbar navbar-expand-lg navbar-cinema sticky-top">
        <div class="container">
            <!-- Brand Logo -->
            <a class="navbar-brand navbar-brand-cinema" href="${pageContext.request.contextPath}/home">
                <i class="fa-solid fa-clapperboard brand-icon"></i>
                <span>CineMax</span>
            </a>

            <!-- Mobile Toggler -->
            <button class="navbar-toggler text-white border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent" aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle navigation">
                <i class="fa-solid fa-bars fs-4"></i>
            </button>

            <!-- Navbar Links & Search -->
            <div class="collapse navbar-collapse" id="navbarContent">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0 ms-lg-4">
                    <li class="nav-item">
                        <a class="nav-link nav-link-cinema ${activeMenu == 'home' ? 'active' : ''}" href="${pageContext.request.contextPath}/home">
                            <i class="fa-solid fa-house me-1"></i> Trang Chủ
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link nav-link-cinema ${activeMenu == 'movies' && type == 'now_showing' ? 'active' : ''}" href="${pageContext.request.contextPath}/movies?type=now_showing">
                            <i class="fa-solid fa-fire me-1 text-danger"></i> Phim Đang Chiếu
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link nav-link-cinema ${activeMenu == 'movies' && type == 'coming_soon' ? 'active' : ''}" href="${pageContext.request.contextPath}/movies?type=coming_soon">
                            <i class="fa-solid fa-calendar-days me-1 text-info"></i> Phim Sắp Chiếu
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link nav-link-cinema ${activeMenu == 'movies' && (empty type || type == 'all') ? 'active' : ''}" href="${pageContext.request.contextPath}/movies">
                            <i class="fa-solid fa-film me-1 text-warning"></i> Toàn Bộ Phim
                        </a>
                    </li>
                </ul>

                <!-- Search Form -->
                <form class="d-flex align-items-center me-3" action="${pageContext.request.contextPath}/movies" method="GET">
                    <div class="input-group">
                        <input class="form-control nav-search-input" type="search" name="q" placeholder="Tìm tên phim, diễn viên..." value="${keyword}" aria-label="Search">
                        <button class="btn btn-outline-secondary border-0 text-white" type="submit" style="margin-left: -40px; z-index: 5;">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </button>
                    </div>
                </form>

                <!-- User Session Action Buttons -->
                <div class="d-flex align-items-center gap-2">
                    <c:choose>
                        <c:when test="${not empty sessionScope.currentUser}">
                            <div class="dropdown">
                                <button class="btn btn-outline-warning btn-sm dropdown-toggle d-flex align-items-center gap-1" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <i class="fa-solid fa-circle-user fs-6"></i>
                                    <span>${sessionScope.currentUser.fullName}</span>
                                </button>
                                <ul class="dropdown-menu dropdown-menu-dark dropdown-menu-end">
                                    <c:if test="${sessionScope.currentUser.role.roleName != 'CUSTOMER'}">
                                        <li>
                                            <a class="dropdown-item text-warning" href="${pageContext.request.contextPath}/admin/dashboard">
                                                <i class="fa-solid fa-gauge-high me-2"></i> Trang Quản Trị
                                            </a>
                                        </li>
                                        <li><hr class="dropdown-divider border-secondary"></li>
                                    </c:if>
                                    <li>
                                        <a class="dropdown-item" href="${pageContext.request.contextPath}/logout">
                                            <i class="fa-solid fa-right-from-bracket me-2 text-danger"></i> Đăng Xuất
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light btn-sm px-3">
                                <i class="fa-solid fa-arrow-right-to-bracket me-1"></i> Đăng Nhập
                            </a>
                            <a href="${pageContext.request.contextPath}/register" class="btn btn-cinema-primary btn-sm px-3">
                                <i class="fa-solid fa-user-plus me-1"></i> Đăng Ký
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </nav>
