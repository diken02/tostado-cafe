package com.tostado.cafeteria.controller;

import com.tostado.cafeteria.model.Employee;
import com.tostado.cafeteria.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

/**
 * Gestiona el inicio y cierre de sesión de los empleados.
 */
@WebServlet(urlPatterns = {"/login", "/logout"})
public class AuthServlet extends HttpServlet {

    /** Vista del formulario de acceso. */
    private static final String LOGIN_VIEW = "/WEB-INF/views/login.jsp";

    /** Servicio que valida las credenciales. */
    private final AuthService auth = new AuthService();

    /**
     * Cierra la sesión en {@code /logout}; en {@code /login} muestra el formulario.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            res.sendRedirect(req.getContextPath() + "/login");
        } else {
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, res);
        }
    }

    /**
     * Valida las credenciales y, si son correctas, guarda el empleado y su rol
     * en la sesión antes de redirigir al panel.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            Optional<Employee> employee =
                    auth.authenticate(req.getParameter("usuario"), req.getParameter("contrasena"));
            if (employee.isEmpty()) {
                req.setAttribute("error", "Usuario o contraseña incorrectos");
                req.getRequestDispatcher(LOGIN_VIEW).forward(req, res);
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("employee", employee.get());
            session.setAttribute("role", employee.get().rol());
            res.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (Exception e) {
            req.setAttribute("error", "No se pudo conectar con MySQL: " + e.getMessage());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, res);
        }
    }
}
