package com.tostado.cafeteria.controller;

import com.tostado.cafeteria.config.DatabaseConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Comentarios y sugerencias de los clientes.
 * <ul>
 *   <li>{@code GET/POST /api/comentarios}: comentarios <b>públicos</b> (JSON).</li>
 *   <li>{@code POST /api/sugerencias}: envío de una sugerencia <b>privada</b>.</li>
 *   <li>{@code GET /admin/sugerencias}: listado de sugerencias, solo administradores.</li>
 * </ul>
 */
@WebServlet(urlPatterns = {"/api/comentarios", "/api/sugerencias", "/admin/sugerencias"})
public class FeedbackServlet extends HttpServlet {

    /** Longitud máxima permitida para nombre y mensaje. */
    private static final int MAX_NAME = 80;
    private static final int MAX_MESSAGE = 500;

    /** Crea las tablas si aún no existen, para no exigir una migración manual. */
    @Override
    public void init() {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS cafeteria_comentario ("
                    + "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "nombre VARCHAR(80) NOT NULL,"
                    + "mensaje VARCHAR(500) NOT NULL,"
                    + "creado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (id)) ENGINE=InnoDB");
            statement.execute("CREATE TABLE IF NOT EXISTS cafeteria_sugerencia ("
                    + "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "nombre VARCHAR(80) NOT NULL,"
                    + "mensaje VARCHAR(500) NOT NULL,"
                    + "creado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (id)) ENGINE=InnoDB");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        if ("/api/comentarios".equals(path)) {
            listComments(response);
        } else if ("/admin/sugerencias".equals(path)) {
            listSuggestions(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String table;
        switch (request.getServletPath()) {
            case "/api/comentarios":
                table = "cafeteria_comentario";
                break;
            case "/api/sugerencias":
                table = "cafeteria_sugerencia";
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
        }

        String name = clean(request.getParameter("nombre"), MAX_NAME);
        String message = clean(request.getParameter("mensaje"), MAX_MESSAGE);
        if (name.isEmpty() || message.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nombre y mensaje son obligatorios");
            return;
        }
        // El nombre de tabla proviene del switch anterior, nunca del usuario.
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + table + " (nombre, mensaje) VALUES (?, ?)")) {
            statement.setString(1, name);
            statement.setString(2, message);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    /** Devuelve los últimos 50 comentarios públicos como JSON. */
    private void listComments(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        StringBuilder json = new StringBuilder("[");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT nombre, mensaje, creado FROM cafeteria_comentario ORDER BY id DESC LIMIT 50");
             ResultSet rows = statement.executeQuery()) {
            boolean first = true;
            while (rows.next()) {
                if (!first) {
                    json.append(',');
                }
                first = false;
                json.append("{\"nombre\":\"").append(json(rows.getString("nombre")))
                        .append("\",\"mensaje\":\"").append(json(rows.getString("mensaje")))
                        .append("\",\"creado\":\"").append(rows.getTimestamp("creado")).append("\"}");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        response.getWriter().write(json.append(']').toString());
    }

    /** Muestra las sugerencias privadas; exige sesión con rol administrador. */
    private void listSuggestions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("employee") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!"administrador".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        response.setContentType("text/html;charset=UTF-8");
        StringBuilder html = new StringBuilder("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">"
                + "<title>Comentarios y sugerencias</title><link rel=\"stylesheet\" href=\""
                + request.getContextPath()
                + "/css/styles.css\"></head><body style=\"padding:24px;font-family:sans-serif\">"
                + "<h1>Comentarios y sugerencias</h1><p><a href=\"" + request.getContextPath()
                + "/dashboard\">Volver al panel</a></p>");
        try (Connection connection = DatabaseConfig.getConnection()) {
            appendList(html, connection, "Sugerencias (privadas)", "cafeteria_sugerencia",
                    "Aún no hay sugerencias.");
            appendList(html, connection, "Comentarios (públicos)", "cafeteria_comentario",
                    "Aún no hay comentarios.");
        } catch (SQLException exception) {
            exception.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        response.getWriter().write(html.append("</body></html>").toString());
    }

    /** Agrega un apartado con los registros de una tabla (nombre fijo, no proviene del usuario). */
    private void appendList(StringBuilder html, Connection connection, String title, String table,
                            String emptyText) throws SQLException {
        html.append("<h2>").append(title).append("</h2><ul>");
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT nombre, mensaje, creado FROM " + table + " ORDER BY id DESC");
             ResultSet rows = statement.executeQuery()) {
            boolean any = false;
            while (rows.next()) {
                any = true;
                html.append("<li><strong>").append(html(rows.getString("nombre"))).append("</strong> (")
                        .append(rows.getTimestamp("creado")).append("): ")
                        .append(html(rows.getString("mensaje"))).append("</li>");
            }
            if (!any) {
                html.append("<li>").append(emptyText).append("</li>");
            }
        }
        html.append("</ul>");
    }

    /** Recorta espacios y limita la longitud; devuelve cadena vacía si es nulo. */
    private static String clean(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }

    /** Escapa un texto para incrustarlo en una cadena JSON. */
    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "").replace("\t", " ");
    }

    /** Escapa un texto para incrustarlo en HTML (evita XSS). */
    private static String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
