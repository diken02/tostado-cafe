package com.tostado.cafeteria.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

/**
 * Filtro de seguridad: exige sesión iniciada para las rutas internas y
 * restringe las rutas de administración al rol "administrador".
 */
@WebFilter(urlPatterns = {"/dashboard", "/crud/*", "/admin/*", "/pos", "/reportes",
        "/mesas", "/pedidos"})
public class AuthFilter implements Filter {

    /** Rutas reservadas para administradores. */
    private static final Set<String> ADMIN = Set.of("/admin/empleados", "/admin/productos",
            "/admin/proveedores", "/admin/insumos", "/admin/sugerencias");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        // Sin sesión válida se redirige al formulario de acceso.
        if (session == null || session.getAttribute("employee") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String role = (String) session.getAttribute("role");
        String path = request.getRequestURI();
        // Un usuario sin rol de administrador no puede entrar a rutas /admin sensibles.
        if (ADMIN.stream().anyMatch(path::contains) && !"administrador".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(req, res);
    }
}
