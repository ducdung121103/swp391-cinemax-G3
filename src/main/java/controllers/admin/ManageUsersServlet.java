package controllers.admin;

import config.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.Role;
import models.User;
import services.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/manage-users")
public class ManageUsersServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check authentication and authorization
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        // Check if user is admin
        DBContext dbContext = null;
        try {
            dbContext = new DBContext();
            Role userRole = userService.getRoleById(currentUser.getRoleId());

            if (userRole == null || !"ADMIN".equals(userRole.getRoleName())) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        } finally {
            if (dbContext != null) {
                dbContext.closeConnection();
            }
        }

        // Get filter parameters
        String roleFilter = request.getParameter("roleFilter");
        String statusFilter = request.getParameter("statusFilter");
        String searchKeyword = request.getParameter("search");

        DBContext usersDbContext = null;

        try {
            usersDbContext = new DBContext();

            // Get users based on filters
            List<User> allUsers;
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                allUsers = userService.searchUsers(searchKeyword.trim());
            } else if (roleFilter != null && !roleFilter.isEmpty()) {
                allUsers = userService.getUsersByRole(Integer.parseInt(roleFilter));
            } else if (statusFilter != null && !statusFilter.isEmpty()) {
                allUsers = userService.getUsersByStatus(statusFilter);
            } else {
                allUsers = userService.getAllUsers();
            }

            // Get all roles for filter dropdown
            List<Role> allRoles = userService.getAllRoles();

            // Create a map of roleId -> roleName for display
            Map<Integer, String> roleMap = new HashMap<>();
            for (Role role : allRoles) {
                roleMap.put(role.getRoleId(), role.getRoleName());
            }

            // ── Pagination ──────────────────────────────────────────────────
            int pageSize = 5;
            int totalUsers = allUsers.size();
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
            if (totalPages == 0)
                totalPages = 1;

            int currentPage = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                try {
                    currentPage = Integer.parseInt(pageParam);
                } catch (NumberFormatException ignored) {
                }
            }
            if (currentPage < 1)
                currentPage = 1;
            if (currentPage > totalPages)
                currentPage = totalPages;

            int fromIndex = (currentPage - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalUsers);
            List<User> users = allUsers.subList(fromIndex, toIndex);
            // ────────────────────────────────────────────────────────────────

            // Set attributes for JSP
            request.setAttribute("users", users);
            request.setAttribute("allRoles", allRoles);
            request.setAttribute("roleMap", roleMap);
            request.setAttribute("roleFilter", roleFilter);
            request.setAttribute("statusFilter", statusFilter);
            request.setAttribute("searchKeyword", searchKeyword);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalUsers", totalUsers);

            request.getRequestDispatcher("/pages/admin/user/manage-users.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading users: " + e.getMessage());
            request.getRequestDispatcher("/pages/admin/user/manage-users.jsp").forward(request, response);
        } finally {
            if (usersDbContext != null) {
                usersDbContext.closeConnection();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check authentication and authorization
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        // Check if user is admin
        DBContext dbContext = null;
        try {
            dbContext = new DBContext();
            Role userRole = userService.getRoleById(currentUser.getRoleId());

            if (userRole == null || !"ADMIN".equals(userRole.getRoleName())) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        } finally {
            if (dbContext != null) {
                dbContext.closeConnection();
            }
        }

        // Get action and userId
        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");

        if (action == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/admin/manage-users?error=Invalid request");
            return;
        }

        DBContext usersDbContext = null;
        try {
            int userId = Integer.parseInt(userIdStr);
            usersDbContext = new DBContext();
            
            userService.updateUserStatus(userId, action);
            response.sendRedirect(request.getContextPath() + "/admin/manage-users?success=User updated successfully");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/manage-users?error=Invalid user ID");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/admin/manage-users?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/manage-users?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } finally {
            if (usersDbContext != null) {
                usersDbContext.closeConnection();
            }
        }
    }
}
