package com.cinema.modules.identity.service;

import com.cinema.model.User;

/**
 * Service xác thực thông qua tài khoản Google OAuth2.
 * // TODO: chưa có trong Feature Tree — chỉ implement sau khi mục này được thêm vào Feature-Tree-Project.docx và thầy duyệt
 */
public interface GoogleAuthService {
    /**
     * Xác thực thông tin người dùng từ Google ID Token.
     */
    User authenticateWithGoogle(String googleIdToken);
}
