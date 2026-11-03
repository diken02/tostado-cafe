package com.tostado.cafeteria.controller;

import com.tostado.cafeteria.repository.CrudRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns={"/dashboard","/crud/*","/mesas","/pedidos","/pos","/reportes","/admin/*"})
public class AppServlet extends HttpServlet {
    private final CrudRepository repository=new CrudRepository();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException, jakarta.servlet.ServletException {
        String path=req.getServletPath();
        try {
            if("/dashboard".equals(path) || "/admin".equals(path)) {
                req.setAttribute("dashboard",repository.dashboard());
                req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req,res);
            } else if("/reportes".equals(path)) {
                req.setAttribute("dashboard",repository.dashboard());
                req.getRequestDispatcher("/WEB-INF/views/reportes.jsp").forward(req,res);
            } else if("/crud".equals(path) || path.startsWith("/crud/") || "/mesas".equals(path) || "/pedidos".equals(path) || "/pos".equals(path) || path.startsWith("/admin/")) {
                String resource=resourceFor(req);
                req.setAttribute("resource",resource);
                req.setAttribute("rows",repository.list(resource));
                String view="/pos".equals(path)||"/pedidos".equals(path)?"/WEB-INF/views/pedidos.jsp":
                        "/mesas".equals(path)?"/WEB-INF/views/mesas.jsp":"/WEB-INF/views/crud.jsp";
                req.getRequestDispatcher(view).forward(req,res);
            } else req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req,res);
        } catch(Exception e) { throw new IOException("Database operation failed",e); }
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException,jakarta.servlet.ServletException {
        String resource=resourceFor(req);
        if(req.getParameter("_method")!=null) { doDelete(req,res); return; }
        Map<String,String> values=new LinkedHashMap<>();
        req.getParameterMap().forEach((k,v)->{ if(!k.startsWith("_")&&v.length>0) values.put(k,v[0]); });
        try {
            Long id=req.getParameter("id")==null||req.getParameter("id").isBlank()?null:Long.valueOf(req.getParameter("id"));
            repository.save(resource,values,id);
            res.sendRedirect(req.getContextPath()+"/crud/"+resource+"?success=Datos guardados");
        } catch(Exception e) { throw new IOException("Save failed",e); }
    }
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse res)throws IOException,jakarta.servlet.ServletException {
        try {
            String resource=resourceFor(req);
            long id=Long.parseLong(req.getParameter("id"));
            repository.delete(resource,id);
            res.sendRedirect(req.getContextPath()+"/crud/"+resource+"?success=Registro eliminado");
        } catch(Exception e) { throw new IOException("Delete failed",e); }
    }
    private String resourceFor(HttpServletRequest req) {
        String path=req.getPathInfo();
        if(path!=null&&path.length()>1) return path.substring(1);
        if(req.getServletPath().startsWith("/admin/")) return req.getServletPath().substring("/admin/".length());
        if("/mesas".equals(req.getServletPath())) return "mesas";
        if("/pedidos".equals(req.getServletPath())||"/pos".equals(req.getServletPath())) return "pedidos";
        return "productos";
    }
}
