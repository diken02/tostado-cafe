package com.tostado.cafeteria.controller;

import com.tostado.cafeteria.config.Passwords;
import com.tostado.cafeteria.repository.CrudRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador principal de la aplicación interna.
 *
 * <p>Atiende el panel (dashboard), los reportes y las pantallas CRUD
 * (productos, mesas, pedidos, cuentas, etc.) delegando el acceso a datos
 * en {@link CrudRepository} y la presentación en las vistas JSP.</p>
 */
@WebServlet(urlPatterns = {"/dashboard", "/crud/*", "/mesas", "/pedidos", "/pos",
        "/reportes", "/admin/*", "/cuentas"})
public class AppServlet extends HttpServlet {

    /** Ruta base de las vistas JSP protegidas. */
    private static final String VIEWS = "/WEB-INF/views/";

    /** Ruta de la pantalla de cuentas (empleados). */
    private static final String ACCOUNTS_PATH = "/cuentas";

    /** Acceso a datos genérico para los recursos CRUD. */
    private final CrudRepository repository = new CrudRepository();

    /**
     * Muestra la vista correspondiente a la ruta solicitada.
     *
     * @param req solicitud HTTP
     * @param res respuesta HTTP
     * @throws IOException si falla la consulta a la base de datos o la escritura
     * @throws ServletException si falla el reenvío a la vista
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String path = req.getServletPath();
        try {
            if ("/dashboard".equals(path) || "/admin".equals(path)) {
                req.setAttribute("dashboard", repository.dashboard());
                forward(req, res, "dashboard.jsp");
            } else if ("/reportes".equals(path)) {
                req.setAttribute("dashboard", repository.dashboard());
                forward(req, res, "reportes.jsp");
            } else if (isCrudPath(path)) {
                String resource = resourceFor(req);
                req.setAttribute("resource", resource);
                req.setAttribute("rows", repository.list(resource));
                // En cuentas, el parámetro "edit" precarga el empleado a modificar.
                if (ACCOUNTS_PATH.equals(path) && req.getParameter("edit") != null) {
                    req.setAttribute("editRow",
                            repository.find("empleados", Long.parseLong(req.getParameter("edit"))));
                }
                forward(req, res, viewFor(path));
            } else {
                forward(req, res, "dashboard.jsp");
            }
        } catch (ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Database operation failed", e);
        }
    }

    /**
     * Crea o actualiza un registro; si llega {@code _method} se trata como borrado.
     *
     * @param req solicitud HTTP con los campos del formulario
     * @param res respuesta HTTP
     * @throws IOException si falla el guardado
     * @throws ServletException si falla el procesamiento
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        // Los formularios HTML solo envían POST, por lo que DELETE se simula con _method.
        if (req.getParameter("_method") != null) {
            doDelete(req, res);
            return;
        }
        String resource = resourceFor(req);
        Map<String, String> values = new LinkedHashMap<>();
        // Se ignoran los parámetros internos (prefijo "_") y se toma el primer valor.
        req.getParameterMap().forEach((k, v) -> {
            if (!k.startsWith("_") && v.length > 0) {
                values.put(k, v[0]);
            }
        });
        // Las contraseñas nunca se guardan en texto plano.
        if ("empleados".equals(resource) && values.containsKey("contrasena_hash")) {
            values.put("contrasena_hash", Passwords.hash(values.get("contrasena_hash")));
        }
        try {
            String rawId = req.getParameter("id");
            Long id = rawId == null || rawId.isBlank() ? null : Long.valueOf(rawId);
            repository.save(resource, values, id);
            res.sendRedirect(req.getContextPath() + redirectFor(req, resource)
                    + "?success=Datos guardados");
        } catch (Exception e) {
            throw new IOException("Save failed", e);
        }
    }

    /**
     * Elimina el registro indicado por el parámetro {@code id}.
     *
     * @param req solicitud HTTP
     * @param res respuesta HTTP
     * @throws IOException si falla el borrado
     * @throws ServletException si falla el procesamiento
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        try {
            String resource = resourceFor(req);
            long id = Long.parseLong(req.getParameter("id"));
            repository.delete(resource, id);
            res.sendRedirect(req.getContextPath() + redirectFor(req, resource)
                    + "?success=Registro eliminado");
        } catch (Exception e) {
            throw new IOException("Delete failed", e);
        }
    }

    /** Reenvía la solicitud a una vista JSP ubicada en {@code WEB-INF/views}. */
    private void forward(HttpServletRequest req, HttpServletResponse res, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEWS + view).forward(req, res);
    }

    /** Indica si la ruta corresponde a una pantalla basada en el CRUD genérico. */
    private boolean isCrudPath(String path) {
        return "/crud".equals(path) || path.startsWith("/crud/") || "/mesas".equals(path)
                || "/pedidos".equals(path) || "/pos".equals(path)
                || ACCOUNTS_PATH.equals(path) || path.startsWith("/admin/");
    }

    /** Devuelve la vista JSP que renderiza la ruta CRUD indicada. */
    private String viewFor(String path) {
        if ("/pos".equals(path) || "/pedidos".equals(path)) {
            return "pedidos.jsp";
        }
        return "/mesas".equals(path) ? "mesas.jsp" : "crud.jsp";
    }

    /** Calcula la ruta a la que se redirige tras guardar o eliminar. */
    private String redirectFor(HttpServletRequest req, String resource) {
        return ACCOUNTS_PATH.equals(req.getServletPath()) ? ACCOUNTS_PATH : "/crud/" + resource;
    }

    /**
     * Determina el recurso (tabla lógica) a partir de la URL.
     *
     * @param req solicitud HTTP
     * @return nombre del recurso; "productos" por defecto
     */
    private String resourceFor(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String servletPath = req.getServletPath();
        if (pathInfo != null && pathInfo.length() > 1) {
            return pathInfo.substring(1);
        }
        if (servletPath.startsWith("/admin/")) {
            return servletPath.substring("/admin/".length());
        }
        if (ACCOUNTS_PATH.equals(servletPath)) {
            return "empleados";
        }
        if ("/mesas".equals(servletPath)) {
            return "mesas";
        }
        if ("/pedidos".equals(servletPath) || "/pos".equals(servletPath)) {
            return "pedidos";
        }
        return "productos";
    }
}
