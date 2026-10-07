<%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <!-- Universal Trailer Modal -->
    <div class="modal fade" id="trailerModal" tabindex="-1" aria-labelledby="trailerMovieTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark text-white border border-secondary shadow-lg">
                <div class="modal-header border-secondary py-2 px-3">
                    <h5 class="modal-title fs-6 fw-bold text-warning" id="trailerMovieTitle">Trailer Xem Trước</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-0">
                    <div class="ratio ratio-16x9">
                        <iframe id="trailerIframe" src="" title="Trailer Video" allowfullscreen allow="autoplay; encrypted-media"></iframe>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="footer-cinema">
        <div class="container">
            <div class="row g-4 mb-4">
                <!-- Col 1: About -->
                <div class="col-lg-4 col-md-6">
                    <div class="footer-brand d-flex align-items-center gap-2">
                        <i class="fa-solid fa-clapperboard text-warning fs-4"></i>
                        <span>MBCinema</span>
                    </div>
                    <p class="footer-desc">
                        Hệ thống rạp chiếu phim hiện đại hàng đầu mang đến trải nghiệm điện ảnh đỉnh cao với âm thanh sống động và hình ảnh sắc nét. Nơi cảm xúc thăng hoa cùng từng thước phim.
                    </p>
                    <div class="d-flex gap-3 text-secondary fs-5 mt-3">
                        <a href="#" class="text-secondary hover-gold"><i class="fa-brands fa-facebook"></i></a>
                        <a href="#" class="text-secondary hover-gold"><i class="fa-brands fa-youtube"></i></a>
                        <a href="#" class="text-secondary hover-gold"><i class="fa-brands fa-tiktok"></i></a>
                        <a href="#" class="text-secondary hover-gold"><i class="fa-brands fa-instagram"></i></a>
                    </div>
                </div>

                <!-- Col 2: Quick Links -->
                <div class="col-lg-2 col-md-6">
                    <h6 class="text-white fw-bold mb-3 text-uppercase">Khám Phá</h6>
                    <a href="${pageContext.request.contextPath}/home" class="footer-link">Trang chủ</a>
                    <a href="${pageContext.request.contextPath}/movies?type=now_showing" class="footer-link">Phim đang chiếu</a>
                    <a href="${pageContext.request.contextPath}/movies?type=coming_soon" class="footer-link">Phim sắp chiếu</a>
                    <a href="${pageContext.request.contextPath}/movies" class="footer-link">Tất cả phim</a>
                </div>

                <!-- Col 3: Support & Policy -->
                <div class="col-lg-3 col-md-6">
                    <h6 class="text-white fw-bold mb-3 text-uppercase">Chính Sách & Quy Định</h6>
                    <a href="#" class="footer-link">Quy định xem phim</a>
                    <a href="#" class="footer-link">Điều khoản sử dụng</a>
                    <a href="#" class="footer-link">Chính sách bảo mật</a>
                    <a href="#" class="footer-link">Chăm sóc khách hàng</a>
                </div>

                <!-- Col 4: Contact -->
                <div class="col-lg-3 col-md-6">
                    <h6 class="text-white fw-bold mb-3 text-uppercase">Liên Hệ</h6>
                    <p class="footer-desc mb-2">
                        <i class="fa-solid fa-location-dot text-warning me-2"></i> Chi nhánh Q1: 123 FPT,Hà Nội
                    </p>
                    <p class="footer-desc mb-2">
                        <i class="fa-solid fa-phone text-warning me-2"></i> Hotline: 1900 8888 (8:00 - 22:00)
                    </p>
                    <p class="footer-desc">
                        <i class="fa-solid fa-envelope text-warning me-2"></i> Email: support@mbcinema.vn
                    </p>
                </div>
            </div>

            <div class="border-top border-secondary pt-3 text-center text-muted small">
                © 2026 MBCinema. Bản quyền thuộc về Multi-branch Cinema Management System.
            </div>
        </div>
    </footer>

    <!-- Bootstrap 5 Bundle JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Main JS -->
    <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
