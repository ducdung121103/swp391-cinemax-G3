package models;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaBranch {
    private Integer branchId;
    private String branchName;
    private String address;
    private String phone;
    private String email;
    private Integer managerId;
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String managerName;
}