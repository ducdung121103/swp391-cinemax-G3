package controllers.admin;

import jakarta.servlet.http.HttpSession;
import models.CinemaBranch;
import models.Role;
import models.User;
import services.BranchService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.UserService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "BranchController", urlPatterns = {"/admin/branches"})
public class Branch extends HttpServlet {

    private final BranchService branchService = new BranchService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Check authentication and authorization
        if (!checkAdminAuthorization(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "create":
                    showCreateForm(request, response);
                    break;
                case "view":
                    showViewForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteBranch(request, response);
                    break;
                default:
                    listBranches(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Check authentication and authorization
        if (!checkAdminAuthorization(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "create":
                    insertBranch(request, response);
                    break;
                case "update":
                    updateBranch(request, response);
                    break;
                default:
                    response.sendRedirect("branches");
                    break;
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            CinemaBranch b = extractBranchFromRequest(request);

            if ("update".equals(action)) {
                String idStr = request.getParameter("branchId");
                if (idStr != null && !idStr.isEmpty()) {
                    b.setBranchId(Integer.parseInt(idStr));
                }
            }
            request.setAttribute("branch", b);

            List<User> managers = branchService.getAllManagers();
            request.setAttribute("managers", managers);

            request.getRequestDispatcher("/pages/admin/branch/form.jsp").forward(request, response);
        }
    }

    /**
     * Checks if user is authenticated and authorized as admin
     * @return true if authorized, false otherwise
     */
    private boolean checkAdminAuthorization(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        User currentUser = (User) session.getAttribute("user");

        try {
            Role userRole = userService.getRoleById(currentUser.getRoleId());

            if (userRole == null || !"ADMIN".equals(userRole.getRoleName())) {
                response.sendRedirect(request.getContextPath() + "/home");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/home");
            return false;
        }

        return true;
    }

    // --- Helper Methods ---

    private void listBranches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy tham số search và filter
        String keyword = request.getParameter("keyword");
        String statusParam = request.getParameter("status");
        String pageParam = request.getParameter("page");

        // Parse status
        Boolean isActive = null;
        if (statusParam != null && !statusParam.isEmpty()) {
            isActive = "active".equals(statusParam) ? true : false;
        }

        // Parse page
        int page = 1;
        int pageSize = 10;
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        // Gọi service với tham số
        List<CinemaBranch> list = branchService.getAllBranches(keyword, isActive, page, pageSize);

        int totalRecords = branchService.countBranches(keyword, isActive);
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        // Set attributes
        request.setAttribute("branches", list);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("status", statusParam == null ? "" : statusParam);

        request.getRequestDispatcher("/pages/admin/branch/list.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<User> managers = branchService.getAllManagers();
        request.setAttribute("managers", managers);

        request.getRequestDispatcher("/pages/admin/branch/form.jsp").forward(request, response);
    }

    private void showViewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        CinemaBranch existingBranch = branchService.getBranchById(id);
        List<User> managers = branchService.getAllManagers();

        request.setAttribute("branch", existingBranch);
        request.setAttribute("managers", managers);
        request.setAttribute("isViewMode", true);
        request.getRequestDispatcher("/pages/admin/branch/form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        CinemaBranch existingBranch = branchService.getBranchById(id);
        List<User> managers = branchService.getAllManagers();

        request.setAttribute("branch", existingBranch);
        request.setAttribute("managers", managers);
        request.getRequestDispatcher("/pages/admin/branch/form.jsp").forward(request, response);
    }

    private void deleteBranch(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            branchService.deleteBranch(id);
            response.sendRedirect("branches?message=deleted");
        } catch (Exception e) {
            response.sendRedirect("branches?error=delete_failed");
        }
    }

    private void insertBranch(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            CinemaBranch newBranch = extractBranchFromRequest(request);
            branchService.createBranch(newBranch);
            response.sendRedirect("branches?message=created");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            // Giữ lại dữ liệu cũ khi lỗi
            request.setAttribute("branch", extractBranchFromRequest(request));
            showCreateForm(request, response);
        }
    }

    private void updateBranch(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            CinemaBranch branch = extractBranchFromRequest(request);
            int id = Integer.parseInt(request.getParameter("branchId"));
            branch.setBranchId(id);

            branchService.updateBranch(branch);
            response.sendRedirect("branches?message=updated");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());

            CinemaBranch b = extractBranchFromRequest(request);

            String idStr = request.getParameter("branchId");
            if (idStr != null && !idStr.isEmpty()) {
                b.setBranchId(Integer.parseInt(idStr));
            }
            request.setAttribute("branch", b);

            List<User> managers = branchService.getAllManagers();
            request.setAttribute("managers", managers);

            request.getRequestDispatcher("/pages/admin/branch/form.jsp").forward(request, response);
        }
    }

    // Hàm phụ để lấy dữ liệu từ form map vào object
    private CinemaBranch extractBranchFromRequest(HttpServletRequest request) {
        CinemaBranch b = new CinemaBranch();
        b.setBranchName(request.getParameter("branchName"));
        b.setAddress(request.getParameter("address"));
        b.setPhone(request.getParameter("phone"));
        b.setEmail(request.getParameter("email"));

        // Xử lý Manager ID (nếu input rỗng thì là null)
        String mgrIdStr = request.getParameter("managerId");
        if (mgrIdStr != null && !mgrIdStr.isEmpty()) {
            b.setManagerId(Integer.parseInt(mgrIdStr));
        } else {
            b.setManagerId(null);
        }

        // Checkbox: nếu tick -> gửi "on", không tick -> null
        b.setActive(request.getParameter("isActive") != null);
        return b;
    }
}