<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<c:set var="pageTitle" value="Menu Bắp Nước & Combo | CineMax" scope="request" />
<c:set var="pageCss" value="/assets/css/modules/fnb-menu.css" scope="request" />
<c:url var="fnbFallbackUrl" value="/assets/images/fnb/default.svg" />
<jsp:include page="../../common/header.jsp" />

<main class="container fnb-menu-page">
    <header class="fnb-menu-heading">
        <span class="fnb-menu-kicker">CINEMAX CONCESSIONS</span>
        <h1>Bắp nước & combo</h1>
        <p>Chọn món yêu thích để thưởng thức trọn vẹn bộ phim.</p>
    </header>

    <c:choose>
        <c:when test="${not empty fnbItems}">
            <div class="row g-4">
                <c:forEach items="${fnbItems}" var="item">
                    <c:choose>
                        <c:when test="${empty item.imageUrl}"><c:set var="itemImageUrl" value="${fnbFallbackUrl}" /></c:when>
                        <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), 'http://') or fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), 'https://')}"><c:set var="itemImageUrl" value="${fn:trim(item.imageUrl)}" /></c:when>
                        <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), '/assets/images/fnb/') and fn:endsWith(fn:toLowerCase(fn:trim(item.imageUrl)), '.png')}"><c:url var="itemImageUrl" value="${fn:replace(fn:trim(item.imageUrl), '.png', '.svg')}" /></c:when>
                        <c:when test="${fn:startsWith(fn:trim(item.imageUrl), '/')}"><c:url var="itemImageUrl" value="${fn:trim(item.imageUrl)}" /></c:when>
                        <c:otherwise><c:set var="relativeItemImageUrl">/${fn:trim(item.imageUrl)}</c:set><c:url var="itemImageUrl" value="${relativeItemImageUrl}" /></c:otherwise>
                    </c:choose>
                    <div class="col-6 col-md-4 col-lg-3">
                        <article class="fnb-menu-card h-100">
                            <div class="fnb-menu-image-wrap">
                                <img class="fnb-menu-image" src="<c:out value='${itemImageUrl}' />" data-fallback="<c:out value='${fnbFallbackUrl}' />" onerror="this.onerror=null;this.src=this.dataset.fallback" alt="<c:out value='${item.name}' />" loading="lazy">
                                <c:if test="${item.isCombo}"><span class="fnb-menu-combo">COMBO</span></c:if>
                            </div>
                            <div class="fnb-menu-copy">
                                <span class="fnb-menu-category"><c:out value="${item.category.name}" /></span>
                                <h2><c:out value="${item.name}" /></h2>
                                <strong class="fnb-menu-price"><fmt:formatNumber value="${item.price}" pattern="#,##0" groupingUsed="true" /><span>đ</span></strong>
                            </div>
                        </article>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="fnb-menu-empty">
                <img src="<c:out value='${fnbFallbackUrl}' />" alt="">
                <strong>Menu đang được cập nhật</strong>
                <span>Sản phẩm bắp nước sẽ sớm xuất hiện tại đây.</span>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="../../common/footer.jsp" />
