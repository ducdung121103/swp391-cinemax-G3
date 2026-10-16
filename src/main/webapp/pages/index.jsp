<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>CineMax - Trang Chủ</title>
    <link href="${pageContext.request.contextPath}/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/font-awesome.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/global.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
    <style>
        :root {
            --dark: #212529;
            --orange: #d96c2c;
        }
        body {
            background: #111;
            color: #fff;
        }
        /* HERO */
        .hero {
            background: linear-gradient(120deg, #000, #212529);
            padding: 60px;
            border-radius: 12px;
            margin-bottom: 40px;
        }
        /* SECTION */
        .section-title {
            border-left: 6px solid var(--orange);
            padding-left: 12px;
            margin: 40px 0 20px;
            font-size: 26px;
            font-weight: bold;
        }

        /* BRANCH CARD STYLES */
        .branch-card {
            background: linear-gradient(135deg, var(--dark) 0%, #1a1f26 100%);
            border: 1px solid #333;
            border-left: 4px solid var(--orange);
            border-radius: 12px;
            overflow: hidden;
            transition: all 0.3s ease;
            height: 100%;
            display: flex;
            flex-direction: column;
        }
        .branch-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 30px rgba(217, 108, 44, 0.25);
            border-left-width: 6px;
        }
        .branch-header {
            background: rgba(217, 108, 44, 0.1);
            padding: 15px;
            border-bottom: 1px solid #333;
        }
        .branch-name {
            font-size: 18px;
            font-weight: 600;
            margin: 0;
            color: #fff;
        }
        .branch-content {
            padding: 20px;
            flex-grow: 1;
            display: flex;
            flex-direction: column;
        }
        .branch-info-row {
            margin-bottom: 15px;
            font-size: 14px;
        }
        .branch-info-row strong {
            display: block;
            color: var(--orange);
            font-weight: 600;
            margin-bottom: 5px;
        }
        .branch-info-row p {
            margin: 0;
            color: #ccc;
            font-size: 14px;
            line-height: 1.5;
        }
        .branch-info-row a {
            color: var(--orange);
            text-decoration: none;
            transition: color 0.3s;
        }
        .branch-info-row a:hover {
            color: #e07e3e;
            text-decoration: underline;
        }
        .branch-status {
            margin-top: auto;
            padding-top: 15px;
            border-top: 1px solid #333;
            text-align: center;
        }
    </style>
</head>

<body>

<jsp:include page="/components/layout/Header.jsp" />

<div class="container mt-4">

    <div class="hero">
        <h1>🎬 Welcome to <span style="color:#d96c2c">CineMax</span></h1>
        <p class="lead">Quản lý rạp chiếu phim. Tìm chi nhánh gần bạn. Dễ dàng và nhanh chóng.</p>
    </div>

    <c:if test="${not empty sessionScope.successMsg}">
        <div class="alert alert-success fw-semibold text-center mt-3 mb-0" style="max-width: 600px; margin: 0 auto;">
            <i class="fa fa-check-circle me-2"></i> ${sessionScope.successMsg}
        </div>
        <c:remove var="successMsg" scope="session" />
    </c:if>

    <!-- Branch Discovery Section -->
    <div class="section-title">🏢 Tìm Chi Nhánh Gần Bạn</div>
    <div class="row mb-5">
        <c:choose>
            <c:when test="${not empty branches}">
                <c:forEach var="branch" items="${branches}">
                    <div class="col-md-6 col-lg-4 mb-4">
                        <div class="branch-card">
                            <div class="branch-header">
                                <h5 class="branch-name">
                                    <i class="fa fa-map-marker-alt text-warning me-2"></i>${branch.branchName}
                                </h5>
                            </div>
                            <div class="branch-content">
                                <div class="branch-info-row">
                                    <strong>📍 Địa chỉ:</strong>
                                    <p>${branch.address != null ? branch.address : 'Chưa cập nhật'}</p>
                                </div>
                                <div class="branch-info-row">
                                    <strong>📞 Liên hệ:</strong>
                                    <p>
                                        <c:choose>
                                            <c:when test="${not empty branch.phone}">
                                                <a href="tel:${branch.phone}" class="text-warning text-decoration-none">
                                                    ${branch.phone}
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Chưa cập nhật</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <c:if test="${not empty branch.email}">
                                    <div class="branch-info-row">
                                        <strong>✉️ Email:</strong>
                                        <p>
                                            <a href="mailto:${branch.email}" class="text-warning text-decoration-none">
                                                ${branch.email}
                                            </a>
                                        </p>
                                    </div>
                                </c:if>
                                <div class="branch-status">
                                    <span class="badge bg-success">
                                        <i class="fa fa-check-circle me-1"></i>Đang hoạt động
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-12">
                    <div class="alert alert-info text-center py-4">
                        <i class="fa fa-info-circle me-2"></i>
                        <span>Chưa có chi nhánh nào được cập nhật. Vui lòng quay lại sau!</span>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<jsp:include page="/components/layout/Footer.jsp"/>

</body>
</html>

