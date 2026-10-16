package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    private Integer roleId;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private LocalDateTime birthday;
    private String phone;
    private String avatarUrl;
    private String status = "ACTIVE";
    private Integer points = 0;
    private Integer totalAccumulatedPoints = 0;
    private Integer tierId = 1;
    private Integer branchId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    public String getLastLoginFormatted() {
        if (lastLogin == null) {
            return null;
        }
        return lastLogin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    
}