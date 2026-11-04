package com.tostado.cafeteria.controller;

import com.tostado.cafeteria.config.DatabaseConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns={"/api/productos","/api/pedidos"})
public class PublicOrderServlet extends HttpServlet {
    private static final Pattern PRODUCT_ITEM =
            Pattern.compile("\\{\"productoId\"\\s*:\\s*(\\d+)\\s*,\\s*\"cantidad\"\\s*:\\s*(\\d+)\\}");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        if (!"/api/productos".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id,nombre,descripcion,precio,categoria,imagen FROM cafeteria_producto WHERE disponible=1 ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                if (!first) json.append(',');
                first = false;
                json.append('{')
                        .append("\"id\":").append(resultSet.getLong("id")).append(',')
                        .append("\"nombre\":\"").append(jsonString(resultSet.getString("nombre"))).append("\",")
                        .append("\"descripcion\":\"").append(jsonString(resultSet.getString("descripcion"))).append("\",")
                        .append("\"precio\":").append(resultSet.getBigDecimal("precio").toPlainString()).append(',')
                        .append("\"categoria\":\"").append(jsonString(resultSet.getString("categoria"))).append("\",")
                        .append("\"imagen\":\"").append(jsonString(resultSet.getString("imagen"))).append("\"}");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"No fue posible consultar los productos\","
                    + "\"detalle\":\"" + jsonString(exception.getMessage()) + "\"}");
            return;
        }
        json.append(']');
        response.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        if (!"/api/pedidos".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String body = request.getReader().lines().reduce("", (left, right) -> left + right);
        String type = stringField(body, "tipo");
        String customerName = stringField(body, "nombreCliente");
        if (type == null || !(type.equals("local") || type.equals("llevar") || type.equals("delivery"))) type = "local";

        Integer mesaId = integerField(body, "mesaId");
        List<OrderItem> items = productItems(body);
        if (items.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Agrega al menos un producto\"}");
            return;
        }

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal subtotal = calculateSubtotal(connection, items);
                int waitNumber = nextWaitNumber(connection);

                long orderId;
                try (PreparedStatement orderStatement = connection.prepareStatement(
                        "INSERT INTO cafeteria_orden (tipo,estado,nombre_cliente,descuento_global,subtotal,total,numero_espera,notas,mesa_id) VALUES (?, 'pendiente', ?, 0, ?, ?, ?, '', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    orderStatement.setString(1, type);
                    orderStatement.setString(2, customerName == null ? "" : customerName);
                    orderStatement.setBigDecimal(3, subtotal);
                    orderStatement.setBigDecimal(4, subtotal);
                    orderStatement.setInt(5, waitNumber);
                    if (mesaId == null) orderStatement.setNull(6, Types.BIGINT);
                    else orderStatement.setLong(6, mesaId);
                    orderStatement.executeUpdate();
                    try (ResultSet keys = orderStatement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No se generó el identificador del pedido");
                        orderId = keys.getLong(1);
                    }
                }

                try (PreparedStatement itemStatement = connection.prepareStatement(
                        "INSERT INTO cafeteria_itemorden (cantidad,precio_unitario,descuento,notas,consumos_registrados,orden_id,producto_id) VALUES (?, ?, 0, '', 0, ?, ?)")) {
                    for (OrderItem item : items) {
                        BigDecimal price = productPrice(connection, item.productId());
                        if (price == null) throw new SQLException("Producto inexistente: " + item.productId());
                        itemStatement.setInt(1, item.quantity());
                        itemStatement.setBigDecimal(2, price);
                        itemStatement.setLong(3, orderId);
                        itemStatement.setLong(4, item.productId());
                        itemStatement.addBatch();
                    }
                    itemStatement.executeBatch();
                }
                connection.commit();
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"ordenId\":" + orderId + ",\"numeroEspera\":" + waitNumber + ",\"tipo\":\"" + type + "\"}");
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"No se pudo crear el pedido\"}");
        }
    }

    private String stringField(String body, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Integer integerField(String body, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*(null|-?\\d+)").matcher(body);
        if (!matcher.find() || "null".equals(matcher.group(1))) return null;
        return Integer.valueOf(matcher.group(1));
    }

    private List<OrderItem> productItems(String body) {
        List<OrderItem> items = new ArrayList<>();
        Matcher matcher = PRODUCT_ITEM.matcher(body);
        while (matcher.find()) {
            int quantity = Integer.parseInt(matcher.group(2));
            if (quantity > 0) items.add(new OrderItem(Long.parseLong(matcher.group(1)), quantity));
        }
        return items;
    }

    private BigDecimal calculateSubtotal(Connection connection, List<OrderItem> items) throws SQLException {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            BigDecimal price = productPrice(connection, item.productId());
            if (price == null) throw new SQLException("Producto inexistente");
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.quantity())));
        }
        return subtotal;
    }

    private BigDecimal productPrice(Connection connection, long productId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT precio FROM cafeteria_producto WHERE id=? AND disponible=1")) {
            statement.setLong(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getBigDecimal("precio") : null;
            }
        }
    }

    private int nextWaitNumber(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COALESCE(MAX(numero_espera),0)+1 FROM cafeteria_orden WHERE fecha>=CURDATE()");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private String jsonString(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private record OrderItem(long productId, int quantity) {}
}
