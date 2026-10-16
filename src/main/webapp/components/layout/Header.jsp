<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    
    <style>
        /* CSS toggle sidebar (không có overlay) */
        .sidebar {
            position: fixed;
            top: 0;
            left: -280px; /* ẩn ban đầu */
            width: 280px;
            height: 100%;
            background: #111418;
            color: white;
            transition: left 0.35s ease;
            z-index: 1050;
            overflow-y: auto;
            box-shadow: 2px 0 15px rgba(0,0,0,0.5);
        }

        body.sidebar-open .sidebar {
            left: 0;
        }

        /* Hamburger button animation */
        .hamburger {
            background: none;
            border: none;
            cursor: pointer;
            padding: 10px;
            display: flex;
            flex-direction: column;
            justify-content: space-around;
            width: 40px;
            height: 40px;
        }

        .hamburger span {
            width: 28px;
            height: 3px;
            background: white;
            border-radius: 2px;
            transition: all 0.3s;
        }

        body.sidebar-open .hamburger span:nth-child(1) {
            transform: rotate(45deg) translate(5px, 5px);
        }

        body.sidebar-open .hamburger span:nth-child(2) {
            opacity: 0;
        }

        body.sidebar-open .hamburger span:nth-child(3) {
            transform: rotate(-45deg) translate(7px, -6px);
        }

        /* Nút đăng nhập */
        .btn-login {
            background: #d96c2c;
            border: none;
            color: white;
            padding: 8px 20px;
            border-radius: 50px;
            font-weight: 600;
            transition: all 0.3s;
        }

        .btn-login:hover {
            background: #e07e3e;
            transform: translateY(-2px);
        }

        /* Nút đăng ký */
        .btn-outline-warning {
            border: 2px solid #d96c2c;
            color: white;
            padding: 8px 20px;
            border-radius: 50px;
            font-weight: 600;
            transition: all 0.3s;
        }

        .btn-outline-warning:hover {
            background: #d96c2c;
            color: white;
            transform: translateY(-2px);
        }

        /* Dropdown user */
        .dropdown-menu {
            background: #1a1a1a;
            border: 1px solid #333;
            border-radius: 8px;
        }

        .dropdown-item {
            color: #e6e6e6;
        }

        .dropdown-item:hover {
            background: #2d333b;
            color: white;
        }
    </style>
</head>
<body>

    <!-- Sidebar -->
    <jsp:include page="/components/layout/Sidebar.jsp" />

    <!-- Modal tìm kiếm -->
    <div class="modal fade" id="exampleModal2" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border-0 shadow-lg">
                <div class="modal-header border-0">
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
            </div>
        </div>
    </div>

    <!-- Navbar chính -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top shadow-sm">
        <div class="container-fluid px-3 px-md-4">

            <!-- Hamburger + Brand -->
            <div class="d-flex align-items-center">
                <button class="hamburger me-3" onclick="toggleSidebar()" aria-label="Toggle sidebar">
                    <span></span>
                    <span></span>
                    <span></span>
                </button>
                <a class="navbar-brand fw-bold fs-4 d-flex align-items-center" href="${pageContext.request.contextPath}/home">
                    <i class="fa fa-modx text-warning me-2 fs-3"></i>
                    <span class="d-none d-md-inline">My Cinema</span>
                </a>
            </div>

            <!-- Toggler mobile -->
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMain"
                    aria-controls="navbarMain" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarMain">

                <!-- Phần User / Login - góc phải -->
                <ul class="navbar-nav ms-auto align-items-center gap-2">
                    <c:choose>
                        <c:when test="${empty sessionScope.user}">
                            <li class="nav-item">
                                <a class="btn btn-login rounded-pill" href="${pageContext.request.contextPath}/login">
                                    <i class="fa fa-sign-in-alt me-2"></i> Đăng nhập
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="btn btn-outline-warning rounded-pill" href="${pageContext.request.contextPath}/register">
                                    <i class="fa fa-user-plus me-2"></i> Đăng ký
                                </a>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li class="nav-item dropdown">
                                <a class="nav-link dropdown-toggle d-flex align-items-center text-white" href="#"
                                   id="userDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <img src="https://ui-avatars.com/api/?name=${sessionScope.user.username}&background=d96c2c&color=fff&size=128"
                                         alt="Avatar" class="rounded-circle me-2" width="36" height="36">
                                    <span class="fw-medium d-none d-md-inline">${sessionScope.user.username}</span>
                                </a>
                                <ul class="dropdown-menu dropdown-menu-end shadow-lg" aria-labelledby="userDropdown">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile">
                                        <i class="fa fa-user me-2"></i> Hồ sơ
                                    </a></li>
                                    <li><hr class="dropdown-divider bg-secondary"></li>
                                    <li><a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout">
                                        <i class="fa fa-sign-out-alt me-2"></i> Đăng xuất
                                    </a></li>
                                </ul>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>
    </nav>

    <!-- JavaScript toggle sidebar (không dùng overlay) -->
    <script>
        function toggleSidebar() {
            document.body.classList.toggle('sidebar-open');
        }

        // Đóng sidebar bằng phím ESC (vẫn giữ để tiện)
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && document.body.classList.contains('sidebar-open')) {
                document.body.classList.remove('sidebar-open');
            }
        });
    </script>

</body>
</html>