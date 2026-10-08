/**
 * Movie Browsing App - Client Script
 */

document.addEventListener("DOMContentLoaded", function () {
    // 1. Khởi tạo Trailer Modal
    const trailerModal = document.getElementById("trailerModal");
    const trailerIframe = document.getElementById("trailerIframe");
    const trailerMovieTitle = document.getElementById("trailerMovieTitle");

    if (trailerModal && trailerIframe) {
        // Lắng nghe sự kiện click các nút "Xem Trailer"
        document.querySelectorAll("[data-trailer-url]").forEach(function (button) {
            button.addEventListener("click", function (e) {
                e.preventDefault();
                const rawUrl = this.getAttribute("data-trailer-url");
                const movieTitle = this.getAttribute("data-movie-title") || "Trailer";

                if (rawUrl && rawUrl.trim() !== "") {
                    // Chuyển đổi sang link Embed nếu cần
                    let embedUrl = convertToEmbedUrl(rawUrl);
                    if (embedUrl) {
                        trailerIframe.src = embedUrl + "?autoplay=1";
                        if (trailerMovieTitle) {
                            trailerMovieTitle.textContent = movieTitle + " - Official Trailer";
                        }
                        const modal = new bootstrap.Modal(trailerModal);
                        modal.show();
                    } else {
                        window.open(rawUrl, "_blank");
                    }
                } else {
                    alert("Trailer của phim này hiện đang được cập nhật!");
                }
            });
        });

        // Dừng video khi đóng modal
        trailerModal.addEventListener("hidden.bs.modal", function () {
            trailerIframe.src = "";
        });
    }

    // 2. Chuyển đổi link Youtube sang Embed URL
    function convertToEmbedUrl(url) {
        if (!url) return null;
        if (url.includes("youtube.com/embed/")) {
            return url;
        }
        if (url.includes("youtube.com/watch?v=")) {
            const videoId = url.split("v=")[1]?.split("&")[0];
            return videoId ? "https://www.youtube.com/embed/" + videoId : url;
        }
        if (url.includes("youtu.be/")) {
            const videoId = url.split("youtu.be/")[1]?.split("?")[0];
            return videoId ? "https://www.youtube.com/embed/" + videoId : url;
        }
        return url;
    }

    // 3. Tự động submit filter khi thay đổi dropdown Sort hoặc Thể loại
    const autoSubmitSelects = document.querySelectorAll(".auto-submit-filter");
    autoSubmitSelects.forEach(function (select) {
        select.addEventListener("change", function () {
            const form = this.closest("form");
            if (form) {
                form.submit();
            }
        });
    });
});
