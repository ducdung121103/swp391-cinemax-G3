package services;

import models.Role;
import models.User;
import repositories.Roles;
import repositories.Users;
import utils.Password;

import java.time.LocalDateTime;
import java.util.List;

public class UserService {

    private final Users usersDao = new Users();
    private final Roles rolesDao = new Roles();

    public void createUser(String username, String email, String password, String fullName,
            String phone, String birthdayStr, int roleId) throws Exception {

        // Check for duplicates
        if (usersDao.checkUsernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (usersDao.checkEmailExists(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (phone != null && !phone.isEmpty() && usersDao.checkPhoneExists(phone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        // Validate role is ADMIN, CINEMA_STAFF or BRANCH_MANAGER
        Role selectedRole = rolesDao.getRoleById(roleId);
        if (selectedRole == null ||
                (!selectedRole.getRoleName().equals("ADMIN") &&
                        !selectedRole.getRoleName().equals("CINEMA_STAFF") &&
                        !selectedRole.getRoleName().equals("BRANCH_MANAGER"))) {
            throw new IllegalArgumentException("Invalid role selection");
        }

        // Create new user
        User newUser = new User();
        newUser.setRoleId(roleId);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(Password.hashPassword(password)); // Hash password
        newUser.setFullName(fullName);
        newUser.setPhone(phone != null && !phone.isEmpty() ? phone : null);

        // Parse birthday if provided
        if (birthdayStr != null && !birthdayStr.trim().isEmpty()) {
            LocalDateTime birthday = null;
            try {
                birthday = LocalDateTime.parse(birthdayStr + "T00:00:00");
            } catch (Exception e) {
                // Invalid date format, skip
            }
            if (birthday != null) {
                if (birthday.isAfter(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Birthday cannot be in the future");
                }
                newUser.setBirthday(birthday);
            }
        }

        newUser.setStatus("ACTIVE");
        newUser.setPoints(0);

        // Insert user
        boolean success = usersDao.insert(newUser);
        if (!success) {
            throw new RuntimeException("Failed to create user");
        }
    }

    public List<User> searchUsers(String searchKeyword) {
        return usersDao.searchUsers(searchKeyword);
    }

    public List<User> getUsersByRole(int roleId) {
        return usersDao.getUsersByRole(roleId);
    }

    public List<User> getUsersByStatus(String status) {
        return usersDao.getUsersByStatus(status);
    }

    public List<User> getAllUsers() {
        return usersDao.getAllUsers();
    }

    public void updateUserStatus(int userId, String action) throws Exception {
        User targetUser = usersDao.getUserById(userId);
        if (targetUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        if ("lock".equals(action) || "delete".equals(action) || "deactivate".equals(action)) {
            Role targetRole = rolesDao.getRoleById(targetUser.getRoleId());
            if (targetRole != null && "ADMIN".equals(targetRole.getRoleName())) {
                throw new IllegalArgumentException("Không thể khóa hoặc xóa tài khoản Admin");
            }
        }

        boolean success = false;
        switch (action) {
            case "lock":
                success = usersDao.updateUserStatus(userId, "LOCKED");
                break;
            case "unlock":
                success = usersDao.updateUserStatus(userId, "ACTIVE");
                break;
            case "deactivate":
                success = usersDao.updateUserStatus(userId, "INACTIVE");
                break;
            case "delete":
                success = usersDao.deleteUser(userId);
                break;
            default:
                throw new IllegalArgumentException("Invalid action");
        }

        if (!success) {
            throw new RuntimeException("Failed to perform action on user");
        }
    }

    public List<Role> getAllRoles() {
        return rolesDao.getAllRoles();
    }

    public List<Role> getCreatableRoles() {
        return rolesDao.getCreatableRoles();
    }

    public Role getRoleById(int roleId) {
        return rolesDao.getRoleById(roleId);
    }
}
