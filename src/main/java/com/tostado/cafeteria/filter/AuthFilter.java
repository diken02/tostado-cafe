package com.tostado.cafeteria.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

@WebFilter(urlPatterns={"/dashboard","/crud/*","/admin/*","/pos","/reportes","/mesas","/pedidos"})
public class AuthFilter implements Filter {
    private static final Set<String> ADMIN = Set.of("/admin/empleados","/admin/productos","/admin/proveedores","/admin/insumos");
    @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)req;
        HttpServletResponse response=(HttpServletResponse)res;
        HttpSession session=request.getSession(false);
        if(session==null || session.getAttribute("employee")==null) {
            response.sendRedirect(request.getContextPath()+"/login");
            return;
        }
        String role=(String)session.getAttribute("role");
        String path=request.getRequestURI();
        if(ADMIN.stream().anyMatch(path::contains) && !"administrador".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(req,res);
    }
}
