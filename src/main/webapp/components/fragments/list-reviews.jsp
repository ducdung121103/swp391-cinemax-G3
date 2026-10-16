<%@ taglib prefix="c" uri="jakarta.tags.core" %>

    <c:forEach var="review" items="${reviews}">
        <div class="card bg-dark border-secondary mb-3">
            <div class="card-body">
                <div class="d-flex align-items-center mb-2">
                    <img src="${review.avatarUrl != null ? review.avatarUrl : 'https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y'}"
                        class="rounded-circle me-3" width="40" height="40">
                    <div>
                        <h6 class="mb-0 text-white">${review.username != null ? review.username : 'Unknown User'}</h6>
                        <small class="text-muted">${review.createdAtFormatted}</small>
                    </div>
                </div>
                <div class="mb-2">
                    <c:forEach begin="1" end="5" var="i">
                        <c:choose>
                            <c:when test="${i <= review.rating}">
                                <i class="fa fa-star text-warning"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa fa-star-o text-secondary"></i>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
                <div class="card-text text-light review-content">
                    <c:out value="${review.comment}" escapeXml="false" />
                </div>
            </div>
        </div>
    </c:forEach>