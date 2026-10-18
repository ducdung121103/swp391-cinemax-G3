package com.cinema.modules.operation.controller;
import com.cinema.modules.operation.dao.FnBItemDAO;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.*;
@WebServlet("/concession-menu") public class ConcessionMenuServlet extends HttpServlet {protected void doGet(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException{q.setAttribute("fnbItems",new FnBItemDAO().findAllActive());q.getRequestDispatcher("/WEB-INF/views/customer/catalog/concession-menu.jsp").forward(q,p);}}
