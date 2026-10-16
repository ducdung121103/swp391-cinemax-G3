<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <style>
            .dropdown-menu {
                display: block;
                opacity: 0;
                visibility: hidden;
                transform: translateY(-5px);
                transition: all 0.3s ease;
            }
            .nav-item.dropdown:hover .dropdown-menu {
                opacity: 1;
                visibility: visible;
                transform: translateY(0);
            }
        </style>
        <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
            <div class="container-fluid">
                <button class="btn btn-outline-light me-3" id="sidebarToggle">
                    <i class="fa fa-bars"></i>
                </button>
                <a class="navbar-brand" href="${pageContext.request.contextPath}/admin/dashboard">System Dashboard</a>

                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#adminNavbar">
                    <span class="navbar-toggler-icon"></span>
                </button>

                <div class="collapse navbar-collapse" id="adminNavbar">
                    <ul class="navbar-nav ms-auto mb-2 mb-lg-0">
                        <li class="nav-item">
                            <span class="nav-link text-white">
                                <i class="fa fa-user me-1"></i>
                                Welcome,
                                <c:if test="${sessionScope.user != null}">
                                    ${sessionScope.user.fullName}
                                </c:if>
                            </span>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>