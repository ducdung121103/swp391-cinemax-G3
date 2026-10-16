package controllers.users;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.User;
import services.ProfileService;

import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "AvatarUpload", urlPatterns = {"/profile/avatar"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 15
)
public class AvatarUpload extends HttpServlet {
    private final ProfileService profileService = new ProfileService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập");
            return;
        }

        User user = (User) session.getAttribute("user");
        int userId = user.getUserId();

        Part filePart = request.getPart("avatarFile");
        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect(request.getContextPath() + "/profile?error=no_file");
            return;
        }

        InputStream is = filePart.getInputStream();
        byte[] bytes = is.readAllBytes();
        String fileName = filePart.getSubmittedFileName();

        String newUrl = profileService.uploadAvatar(bytes, fileName, userId);

        if (newUrl != null) {
            user.setAvatarUrl(newUrl);
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/profile?success=avatar_updated");
        } else {
            response.sendRedirect(request.getContextPath() + "/profile?error=upload_failed");
        }
    }
}