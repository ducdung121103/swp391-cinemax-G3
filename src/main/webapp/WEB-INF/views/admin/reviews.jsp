<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Review Management - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .filter-bar {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
            margin-bottom: 20px;
            background: #fff;
            padding: 16px 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
        .filter-select {
            padding: 8px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            background-color: #fff;
        }
        .btn-secondary {
            background: #4a5568;
            color: #fff;
            padding: 8px 14px;
            border: none;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .btn-secondary:hover { background: #2d3748; }
        .btn-sm {
            padding: 5px 10px;
            font-size: 0.8rem;
            border-radius: 4px;
            cursor: pointer;
            border: none;
            text-decoration: none;
            display: inline-block;
            color: #fff;
        }
        .btn-approve { background: #059669; }
        .btn-hide { background: #d97706; }
        .btn-delete { background: #dc2626; }
        .status-badge {
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.8rem;
            font-weight: 600;
            display: inline-block;
        }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-approved { background: #dcfce7; color: #166534; }
        .status-hidden { background: #e2e8f0; color: #334155; }
        .alert-toast {
            padding: 12px 18px;
            border-radius: 6px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .alert-success { background: #c6f6d5; color: #22543d; border-left: 5px solid #38a169; }
        .alert-error { background: #fed7d7; color: #742a2a; border-left: 5px solid #e53e3e; }
        .review-content {
            max-width: 340px;
            white-space: normal;
            color: #334155;
            line-height: 1.4;
        }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../common/navbar.jsp" />
        <div class="admin-content">

            <c:if test="${not empty msg}">
                <div class="alert-toast alert-success">
                    <span>
                        <i class="fa-solid fa-circle-check" style="margin-right: 6px;"></i>
                        <c:choose>
                            <c:when test="${msg == 'approved'}">Review da duoc duyet va hien thi cong khai.</c:when>
                            <c:when test="${msg == 'hidden'}">Review da duoc an khoi trang cong khai.</c:when>
                            <c:when test="${msg == 'deleted'}">Review da duoc xoa.</c:when>
                            <c:otherwise>Thao tac thanh cong.</c:otherwise>
                        </c:choose>
                    </span>
                    <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert-toast alert-error">
                    <span><i class="fa-solid fa-triangle-exclamation" style="margin-right: 6px;"></i><c:out value="${error}" /></span>
                    <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                </div>
            </c:if>

            <form method="GET" action="${pageContext.request.contextPath}/admin/reviews" class="filter-bar">
                <select name="status" class="filter-select">
                    <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>Tat ca trang thai</option>
                    <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>APPROVED</option>
                    <option value="HIDDEN" ${selectedStatus == 'HIDDEN' ? 'selected' : ''}>HIDDEN</option>
                </select>
                <button type="submit" class="btn-secondary"><i class="fa-solid fa-filter"></i> Loc</button>
                <a href="${pageContext.request.contextPath}/admin/reviews" class="btn-secondary" style="background:#718096;">
                    <i class="fa-solid fa-rotate-left"></i> Dat lai
                </a>
            </form>

            <div class="admin-card">
                <div class="admin-card-header">
                    <h3 class="admin-card-title">
                        <i class="fa-solid fa-comments"></i>
                        Review Management
                    </h3>
                    <span style="font-size: 0.85rem; color: #64748b; font-weight: 600;">Tong so: <strong>${reviews.size()}</strong> review</span>
                </div>

                <div class="table-responsive">
                    <table class="admin-table">
                        <thead>
                        <tr>
                            <th style="width: 80px;">Review ID</th>
                            <th style="width: 170px;">Customer</th>
                            <th style="width: 200px;">Movie</th>
                            <th style="width: 90px; text-align:center;">Rating</th>
                            <th>Content</th>
                            <th style="width: 110px; text-align:center;">Status</th>
                            <th style="width: 210px; text-align:center;">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${reviews}" var="r">
                            <tr>
                                <td>#${r.id}</td>
                                <td><strong><c:out value="${r.customerName}" /></strong></td>
                                <td><c:out value="${r.movieTitle}" /></td>
                                <td style="text-align:center;">
                                    <span style="color: #f59e0b; font-weight:700;">${r.ratingScore} <i class="fa-solid fa-star"></i></span>
                                </td>
                                <td class="review-content"><c:out value="${r.comment}" /></td>
                                <td style="text-align:center;">
                                    <c:choose>
                                        <c:when test="${r.status == 'PENDING'}">
                                            <span class="status-badge status-pending">${r.status}</span>
                                        </c:when>
                                        <c:when test="${r.status == 'APPROVED'}">
                                            <span class="status-badge status-approved">${r.status}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-badge status-hidden">${r.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:center;">
                                    <c:if test="${r.status == 'PENDING' || r.status == 'HIDDEN'}">
                                        <form method="POST" action="${pageContext.request.contextPath}/admin/reviews/approve" style="display:inline; margin-right:4px;">
                                            <input type="hidden" name="reviewId" value="${r.id}" />
                                            <button type="submit" class="btn-sm btn-approve">Approve</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${r.status == 'APPROVED'}">
                                        <form method="POST" action="${pageContext.request.contextPath}/admin/reviews/hide" style="display:inline; margin-right:4px;">
                                            <input type="hidden" name="reviewId" value="${r.id}" />
                                            <button type="submit" class="btn-sm btn-hide">Hide</button>
                                        </form>
                                    </c:if>
                                    <form method="POST" action="${pageContext.request.contextPath}/admin/reviews/delete" style="display:inline;"
                                          onsubmit="return confirm('Ban co chac chan muon xoa review nay?');">
                                        <input type="hidden" name="reviewId" value="${r.id}" />
                                        <button type="submit" class="btn-sm btn-delete">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty reviews}">
                            <tr>
                                <td colspan="7" style="text-align:center; color:#94a3b8; padding:24px;">Khong co review nao phu hop.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</div>
</body>
</html>


