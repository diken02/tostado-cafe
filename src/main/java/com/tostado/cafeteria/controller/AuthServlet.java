package com.tostado.cafeteria.controller;

import com.tostado.cafeteria.model.Employee;
import com.tostado.cafeteria.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns={"/login","/logout"})
public class AuthServlet extends HttpServlet {
    private final AuthService auth = new AuthService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        if(req.getServletPath().equals("/logout")) {
            HttpSession s=req.getSession(false);
            if(s!=null) s.invalidate();
            res.sendRedirect(req.getContextPath()+"/login");
        } else req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req,res);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        try {
            Optional<Employee> employee=auth.authenticate(req.getParameter("usuario"),req.getParameter("contrasena"));
            if(employee.isEmpty()) {
                req.setAttribute("error","Usuario o contraseña incorrectos");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req,res);
                return;
            }
            HttpSession session=req.getSession(true);
            session.setAttribute("employee",employee.get());
            session.setAttribute("role",employee.get().rol());
            res.sendRedirect(req.getContextPath()+"/dashboard");
        } catch(Exception e) {
            req.setAttribute("error","No se pudo conectar con MySQL: "+e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req,res);
        }
    }
}
