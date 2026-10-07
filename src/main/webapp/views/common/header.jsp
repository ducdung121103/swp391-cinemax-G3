<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty pageTitle ? pageTitle : 'MBCinema - Hệ Thống Đặt Vé Phim & Khám Phá Điện Ảnh'}</title>
    
    <!-- Google Fonts: Inter / Roboto -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Font Awesome 6 Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    
    <!-- Custom Style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Header Navbar -->
    <nav class="navbar navbar-expand-lg navbar-cinema sticky-top">
        <div class="container">
            <!-- Brand Logo -->
            <a class="navbar-brand navbar-brand-cinema" href="${pageContext.request.contextPath}/home">
                <i class="fa-solid fa-clapperboard brand-icon"></i>
                <span>MBCinema</span>
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

                <!-- Guest Action Buttons -->
                <div class="d-flex align-items-center gap-2">
                    <a href="${pageContext.request.contextPath}/movies" class="btn btn-cinema-primary btn-sm">
                        <i class="fa-solid fa-ticket"></i> Khám Phá Phim
                    </a>
                </div>
            </div>
        </div>
    </nav>
