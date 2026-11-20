package com.tostado.cafeteria.repository;

import com.tostado.cafeteria.config.DatabaseConfig;
import java.sql.*;
import java.util.*;

/**
 * Repositorio genérico para los recursos CRUD de la cafetería.
 *
 * <p>Solo se permiten tablas y columnas de una lista blanca, lo que evita
 * inyección SQL en los nombres dinámicos; los valores siempre se enlazan
 * como parámetros.</p>
 */
public class CrudRepository {

    /** Mensaje de error para recursos no registrados. */
    private static final String UNKNOWN_RESOURCE = "Unknown resource: ";

    /** Relación entre el nombre lógico del recurso y su tabla. */
    private static final Map<String, String> TABLES = Map.ofEntries(
            Map.entry("clientes","cafeteria_cliente"),
            Map.entry("productos","cafeteria_producto"),
            Map.entry("insumos","cafeteria_insumo"),
            Map.entry("proveedores","cafeteria_proveedor"),
            Map.entry("empleados","cafeteria_empleado"),
            Map.entry("mesas","cafeteria_mesa"),
            Map.entry("pedidos","cafeteria_orden"),
            Map.entry("pagos","cafeteria_pago"),
            Map.entry("recetas","cafeteria_recetaitem"),
            Map.entry("compras","cafeteria_comprainsumo"));

    /**
     * Lista los últimos 500 registros de un recurso.
     *
     * @param resource nombre lógico del recurso
     * @return filas como mapas columna-valor
     * @throws SQLException si el recurso es desconocido o falla la consulta
     */
    public List<Map<String, Object>> list(String resource) throws SQLException {
        String table = tableFor(resource);
        String sql = "SELECT * FROM " + table + " ORDER BY id DESC LIMIT 500";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rows(rs);
        }
    }

    /**
     * Busca un registro por identificador.
     *
     * @param resource nombre lógico del recurso
     * @param id identificador del registro
     * @return la fila, o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Map<String, Object> find(String resource, long id) throws SQLException {
        String table = tableFor(resource);
        String sql = "SELECT * FROM " + table + " WHERE id=? LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                return row;
            }
        }
    }

    /**
     * Inserta (id nulo) o actualiza un registro con los campos permitidos.
     *
     * @param resource nombre lógico del recurso
     * @param values valores recibidos del formulario
     * @param id identificador a actualizar, o {@code null} para insertar
     * @throws SQLException si no hay campos válidos o falla la escritura
     */
    public void save(String resource, Map<String, String> values, Long id) throws SQLException {
        String table = tableFor(resource);
        // Lista blanca de columnas editables por recurso.
        List<String> allowed;
        switch (resource) {
            case "clientes" -> allowed = List.of("nombre", "telefono", "email", "puntos", "total_compras");
            case "productos" -> allowed = List.of("nombre", "descripcion", "precio", "categoria", "disponible", "imagen");
            case "insumos" -> allowed = List.of("nombre", "cantidad", "unidad", "nivel_minimo", "costo", "activo");
            case "proveedores" -> allowed = List.of("nombre", "contacto", "telefono", "email", "activo");
            case "empleados" -> allowed = List.of("nombre", "rol", "usuario", "contrasena_hash", "turno", "activo");
            case "mesas" -> allowed = List.of("numero", "capacidad", "estado", "posicion_x", "posicion_y");
            case "pedidos" -> allowed = List.of("tipo", "estado", "nombre_cliente", "descuento_global", "subtotal",
                    "total", "numero_espera", "notas", "cliente_id", "empleado_id", "mesa_id");
            case "pagos" -> allowed = List.of("metodo", "monto", "cambio", "orden_id");
            default -> allowed = List.of();
        }
        List<String> columns = allowed.stream().filter(values::containsKey).toList();
        if (columns.isEmpty()) {
            throw new SQLException("No supported fields supplied");
        }
        StringBuilder sql = new StringBuilder();
        try (Connection c = DatabaseConfig.getConnection()) {
            if (id == null) {
                sql.append("INSERT INTO ").append(table).append(" (")
                        .append(String.join(",", columns)).append(") VALUES (")
                        .append(String.join(",", Collections.nCopies(columns.size(), "?"))).append(")");
                try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                    bind(ps, columns, values);
                    ps.executeUpdate();
                }
            } else {
                sql.append("UPDATE ").append(table).append(" SET ")
                        .append(columns.stream().map(k -> k + "=?").reduce((a, b) -> a + "," + b).orElseThrow())
                        .append(" WHERE id=?");
                try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                    bind(ps, columns, values);
                    ps.setLong(columns.size() + 1, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Enlaza los valores de las columnas, en orden, a los parámetros de la sentencia. */
    private void bind(PreparedStatement ps, List<String> columns, Map<String, String> values)
            throws SQLException {
        for (int i = 0; i < columns.size(); i++) {
            ps.setString(i + 1, values.get(columns.get(i)));
        }
    }

    /**
     * Elimina un registro por identificador.
     *
     * @param resource nombre lógico del recurso
     * @param id identificador del registro
     * @throws SQLException si falla el borrado
     */
    public void delete(String resource, long id) throws SQLException {
        String table = tableFor(resource);
        String sql = "DELETE FROM " + table + " WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Calcula los indicadores del panel principal (ventas, pedidos, inventario, etc.).
     *
     * @return indicadores por nombre
     * @throws SQLException si falla alguna consulta
     */
    public Map<String, Object> dashboard() throws SQLException {
        Map<String,Object> result = new LinkedHashMap<>();
        try (Connection c = DatabaseConfig.getConnection()) {
            result.put("ventasHoy", scalar(c, "SELECT COALESCE(SUM(total),0) FROM cafeteria_orden WHERE fecha>=CURDATE() AND estado IN ('entregado','listo')"));
            result.put("pedidosHoy", scalar(c, "SELECT COUNT(*) FROM cafeteria_orden WHERE fecha>=CURDATE()"));
            result.put("ventasAyer", scalar(c, "SELECT COALESCE(SUM(total),0) FROM cafeteria_orden WHERE fecha>=DATE_SUB(CURDATE(),INTERVAL 1 DAY) AND fecha<CURDATE() AND estado IN ('entregado','listo')"));
            result.put("ticketPromedio", scalar(c, "SELECT COALESCE(AVG(total),0) FROM cafeteria_orden WHERE fecha>=CURDATE() AND estado IN ('entregado','listo')"));
            result.put("productos", scalar(c, "SELECT COUNT(*) FROM cafeteria_producto WHERE disponible=1"));
            result.put("clientes", scalar(c, "SELECT COUNT(*) FROM cafeteria_cliente"));
            result.put("mesasOcupadas", scalar(c, "SELECT COUNT(*) FROM cafeteria_mesa WHERE estado='ocupada'"));
            result.put("alertasInventario", scalar(c, "SELECT COUNT(*) FROM cafeteria_insumo WHERE activo=1 AND cantidad<=nivel_minimo"));
            result.put("enProduccion", scalar(c, "SELECT COUNT(*) FROM cafeteria_orden WHERE estado IN ('pendiente','preparando')"));
            result.put("entregados", scalar(c, "SELECT COUNT(*) FROM cafeteria_orden WHERE fecha>=CURDATE() AND estado='entregado'"));
        }
        return result;
    }

    /** Devuelve la tabla asociada al recurso o lanza error si no está permitido. */
    private String tableFor(String resource) throws SQLException {
        String table = TABLES.get(resource);
        if (table == null) {
            throw new SQLException(UNKNOWN_RESOURCE + resource);
        }
        return table;
    }

    /** Ejecuta una consulta de un solo valor (conteo, suma, promedio). */
    private Object scalar(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getObject(1) : 0;
        }
    }

    /** Convierte un ResultSet en una lista de filas columna-valor. */
    private List<Map<String, Object>> rows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
}
