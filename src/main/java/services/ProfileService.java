package services;

import com.cloudinary.utils.ObjectUtils;
import config.CloudinaryConfig;
import models.User;
import repositories.Users;

import java.util.Map;

public class ProfileService {
    private final Users userRepo = new Users();

    /**
     * Upload avatar lên Cloudinary và lưu URL vào DB
     * 
     * @param fileBytes byte[] của file ảnh
     * @param fileName  tên file gốc
     * @param userId    ID người dùng
     * @return URL secure mới nếu thành công, null nếu thất bại
     */
    @SuppressWarnings("unchecked")
    public String uploadAvatar(byte[] fileBytes, String fileName, int userId) {
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", CloudinaryConfig.getUploadFolder(),
                    "public_id", "user_avatar_" + userId,
                    "overwrite", true,
                    "resource_type", "image",
                    "allowed_formats", new String[] { "jpg", "jpeg", "png" });

            Map<String, Object> uploadResult = CloudinaryConfig.getCloudinary().uploader().upload(fileBytes,
                    uploadParams);
            String avatarUrl = (String) uploadResult.get("secure_url");

            boolean updated = userRepo.updateAvatarUrl(userId, avatarUrl);
            return updated ? avatarUrl : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateProfileInfo(User user) {
        return userRepo.updateProfileInfo(user);
    }

    public boolean updatePassword(User user) {
        return userRepo.updatePassword(user);
    }

}