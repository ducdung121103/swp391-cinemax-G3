package com.cinema.modules.operation.controller;

import com.cinema.model.FnBCategory;
import com.cinema.model.FnBItem;
import com.cinema.modules.operation.dao.FnBCatalogDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/admin/operation/fnb-catalog")
public class FnBCatalogServlet extends HttpServlet {
    private final FnBCatalogDAO dao = new FnBCatalogDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try { req.setAttribute("items",dao.findItems()); req.setAttribute("categories",dao.findCategories()); }
        catch(SQLException e){throw new ServletException("Không thể tải danh mục F&B",e);}
        req.getRequestDispatcher("/WEB-INF/views/admin/operation/fnb-catalog.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8"); String action=req.getParameter("action");
        try {
            if("saveItem".equals(action)){
                FnBItem i=new FnBItem(); i.setId(id(req,"id")); i.setCategoryId(requiredLong(req,"categoryId")); i.setItemCode(required(req,"itemCode")); i.setName(required(req,"name"));
                i.setPrice(new BigDecimal(required(req,"price"))); if(i.getPrice().signum()<0)throw new IllegalArgumentException("Giá phải lớn hơn hoặc bằng 0.");
                i.setImageUrl(blankToNull(req.getParameter("imageUrl"))); i.setIsCombo("true".equals(req.getParameter("isCombo"))); dao.saveItem(i);
            } else if("toggleItem".equals(action)) dao.setItemActive(requiredLong(req,"id"),"true".equals(req.getParameter("active")));
            else if("saveCategory".equals(action)){FnBCategory x=new FnBCategory();x.setId(id(req,"id"));x.setName(required(req,"name"));x.setDescription(blankToNull(req.getParameter("description")));dao.saveCategory(x);}
            else if("toggleCategory".equals(action))dao.setCategoryActive(requiredLong(req,"id"),"true".equals(req.getParameter("active")));
            else throw new IllegalArgumentException("Thao tác không hợp lệ.");
            resp.sendRedirect(req.getContextPath()+"/admin/operation/fnb-catalog?success=1");
        } catch(IllegalArgumentException e){resp.sendRedirect(req.getContextPath()+"/admin/operation/fnb-catalog?error="+java.net.URLEncoder.encode(e.getMessage(),java.nio.charset.StandardCharsets.UTF_8));}
        catch(SQLException e){if("2601".equals(e.getSQLState())||"23000".equals(e.getSQLState()))resp.sendRedirect(req.getContextPath()+"/admin/operation/fnb-catalog?error="+java.net.URLEncoder.encode("Mã sản phẩm hoặc tên danh mục đã tồn tại.",java.nio.charset.StandardCharsets.UTF_8));else throw new ServletException("Không thể cập nhật danh mục F&B",e);}
    }
    private static String required(HttpServletRequest r,String key){String v=r.getParameter(key);if(v==null||v.trim().isEmpty())throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc.");return v.trim();}
    private static Long id(HttpServletRequest r,String key){String v=r.getParameter(key);return v==null||v.isBlank()?null:Long.valueOf(v);}
    private static long requiredLong(HttpServletRequest r,String key){return Long.parseLong(required(r,key));}
    private static String blankToNull(String v){return v==null||v.isBlank()?null:v.trim();}
}
